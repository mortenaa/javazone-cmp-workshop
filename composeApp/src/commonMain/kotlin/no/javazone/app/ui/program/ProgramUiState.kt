package no.javazone.app.ui.program

import kotlinx.datetime.LocalDate
import no.javazone.app.model.Format
import no.javazone.app.model.Session
import no.javazone.app.model.TimeSlot
import no.javazone.app.model.toConferenceDays

/** Single immutable snapshot of everything the program screens render. */
data class ProgramUiState(
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val isOffline: Boolean = false,
    val offlineBannerDismissed: Boolean = false,
    val sessions: List<Session> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val selectedDay: LocalDate? = null,
    val activeFormats: Set<Format> = emptySet(),
    val activeLanguages: Set<String> = emptySet(),
    val searchQuery: String = "",
    val selectedSessionId: String? = null,
) {
    val dayTabs: List<LocalDate> get() = sessions.toConferenceDays().map { it.date }

    val showOfflineBanner: Boolean get() = isOffline && !offlineBannerDismissed

    fun session(id: String?): Session? = sessions.firstOrNull { it.id == id }

    /** The selected day's slots with format/language/search filters applied. */
    fun daySlots(day: LocalDate?): List<TimeSlot> =
        sessions.filter { it.matchesFilters() }.toConferenceDays()
            .firstOrNull { it.date == day }?.slots.orEmpty()

    private fun Session.matchesFilters(): Boolean =
        (activeFormats.isEmpty() || format in activeFormats) &&
            (activeLanguages.isEmpty() || language in activeLanguages) &&
            (searchQuery.isBlank() || title.contains(searchQuery, ignoreCase = true) ||
                speakers.any { it.name.contains(searchQuery, ignoreCase = true) })
}
