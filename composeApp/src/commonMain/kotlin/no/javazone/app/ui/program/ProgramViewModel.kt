package no.javazone.app.ui.program

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import no.javazone.app.data.ProgramDto
import no.javazone.app.data.ProgramJson
import no.javazone.app.data.toSession
import no.javazone.app.model.Session
import no.javazone.app.model.toConferenceDays
import no.javazone.app.resources.Res

/**
 * Owns the program data and favorites; the UI only sends [ProgramIntent]s.
 *
 * Task 4 version: the program is loaded straight from the bundled resource and
 * favorites are an in-memory [Set] that vanishes on restart. Task 5 swaps the
 * load for a Ktor fetch behind a repository, and Task 6 gives favorites a real
 * persistent home — neither of which changes this class's public surface.
 */
class ProgramViewModel : ViewModel() {

    private val _state = MutableStateFlow(ProgramUiState())
    val state: StateFlow<ProgramUiState> = _state.asStateFlow()

    init {
        loadProgram()
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
                _state.update { it.copy(favoriteIds = it.favoriteIds.toggle(intent.sessionId)) }
            is ProgramIntent.SelectSession -> _state.update { it.copy(selectedSessionId = intent.sessionId) }
            ProgramIntent.DismissOfflineBanner -> _state.update { it.copy(offlineBannerDismissed = true) }
            ProgramIntent.Retry -> loadProgram()
        }
    }

    private fun loadProgram() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = it.sessions.isEmpty(), loadFailed = false) }
            val sessions = loadBundledSessions()
            _state.update {
                it.copy(
                    isLoading = false,
                    sessions = sessions,
                    selectedDay = it.selectedDay ?: defaultDay(sessions),
                )
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

/** Loads and maps the bundled program.json. Task 5 replaces this with a real network fetch. */
private suspend fun loadBundledSessions(): List<Session> {
    val bytes = Res.readBytes("files/program.json")
    val dto = ProgramJson.decodeFromString(ProgramDto.serializer(), bytes.decodeToString())
    return dto.sessions.map { it.toSession() }
}

private fun <T> Set<T>.toggle(value: T): Set<T> = if (value in this) this - value else this + value
