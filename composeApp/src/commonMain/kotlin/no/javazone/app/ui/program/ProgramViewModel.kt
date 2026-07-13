package no.javazone.app.ui.program

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Clock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import no.javazone.app.data.ProgramRepository
import no.javazone.app.model.Session
import no.javazone.app.model.toConferenceDays

/** Owns the program data and favorites; the UI only sends [ProgramIntent]s. */
class ProgramViewModel(
    private val repository: ProgramRepository = ProgramRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(ProgramUiState())
    val state: StateFlow<ProgramUiState> = _state.asStateFlow()

    init {
        loadProgram()
        viewModelScope.launch {
            repository.favoriteIds.collect { ids ->
                _state.update { it.copy(favoriteIds = ids) }
            }
        }
    }

    fun onIntent(intent: ProgramIntent) {
        when (intent) {
            is ProgramIntent.SelectDay -> _state.update { it.copy(selectedDay = intent.day) }
            is ProgramIntent.ToggleFormat ->
                _state.update { it.copy(activeFormats = it.activeFormats.toggle(intent.format)) }
            is ProgramIntent.ToggleLanguage ->
                _state.update { it.copy(activeLanguages = it.activeLanguages.toggle(intent.language)) }
            is ProgramIntent.Search -> _state.update { it.copy(searchQuery = intent.query) }
            ProgramIntent.ClearFilters ->
                _state.update {
                    it.copy(activeFormats = emptySet(), activeLanguages = emptySet(), searchQuery = "")
                }
            is ProgramIntent.ToggleFavorite ->
                viewModelScope.launch { repository.toggleFavorite(intent.sessionId) }
            is ProgramIntent.SelectSession -> _state.update { it.copy(selectedSessionId = intent.sessionId) }
            ProgramIntent.DismissOfflineBanner -> _state.update { it.copy(offlineBannerDismissed = true) }
            ProgramIntent.Retry -> loadProgram()
        }
    }

    private fun loadProgram() {
        viewModelScope.launch {
            // Every (re)load resets the banner dismissal: it may reappear on a
            // failed refresh and disappears for real once a fetch succeeds (§3.7).
            // The full-screen spinner only shows when there is nothing to look at;
            // a Refresh with data on screen stays in the background.
            _state.update {
                it.copy(isLoading = it.sessions.isEmpty(), loadFailed = false, offlineBannerDismissed = false)
            }
            try {
                val load = repository.loadSessions()
                _state.update {
                    it.copy(
                        isLoading = false,
                        sessions = load.sessions,
                        isOffline = load.isOffline,
                        selectedDay = it.selectedDay ?: defaultDay(load.sessions),
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Same rule as loading: never replace a visible list with the error screen.
                _state.update { it.copy(isLoading = false, loadFailed = it.sessions.isEmpty()) }
            }
        }
    }

    /** Today during the conference, otherwise the Wednesday (DESIGN.md §3.1). */
    private fun defaultDay(sessions: List<Session>): LocalDate? {
        val dates = sessions.toConferenceDays().map { it.date }
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return dates.firstOrNull { it == today }
            ?: dates.firstOrNull { it.dayOfWeek == DayOfWeek.WEDNESDAY }
            ?: dates.firstOrNull()
    }
}

private fun <T> Set<T>.toggle(value: T): Set<T> = if (value in this) this - value else this + value
