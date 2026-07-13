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
    /** Tabs always show all days, even when filters empty one of them. Lazy: computed once per state instance. */
    val dayTabs: List<LocalDate> by lazy { sessions.toConferenceDays().map { it.date } }

    val hasActiveFilters: Boolean
        get() = activeFormats.isNotEmpty() || activeLanguages.isNotEmpty() || searchQuery.isNotBlank()

    val showOfflineBanner: Boolean get() = isOffline && !offlineBannerDismissed

    fun session(id: String?): Session? = sessions.firstOrNull { it.id == id }

    /** The selected day's slots with format/language filters applied (Program tab). */
    fun daySlots(day: LocalDate?): List<TimeSlot> =
        sessions.filter { it.matchesFilters() }.slotsFor(day)

    /** The selected day's favorited sessions, unfiltered (My Schedule tab). */
    fun favoriteSlots(day: LocalDate?): List<TimeSlot> =
        sessions.filter { it.id in favoriteIds }.slotsFor(day)

    private fun Session.matchesFilters(): Boolean =
        (activeFormats.isEmpty() || format in activeFormats) &&
            (activeLanguages.isEmpty() || language in activeLanguages) &&
            matchesSearch()

    /** Case-insensitive substring match on title and speaker names (DESIGN.md §3.1). */
    private fun Session.matchesSearch(): Boolean =
        searchQuery.isBlank() ||
            title.contains(searchQuery, ignoreCase = true) ||
            speakers.any { it.name.contains(searchQuery, ignoreCase = true) }

    private fun List<Session>.slotsFor(day: LocalDate?): List<TimeSlot> =
        toConferenceDays().firstOrNull { it.date == day }?.slots.orEmpty()
}
