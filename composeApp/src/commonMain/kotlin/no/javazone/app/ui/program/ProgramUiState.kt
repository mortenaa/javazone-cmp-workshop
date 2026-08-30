package no.javazone.app.ui.program

import kotlinx.datetime.LocalDate
import no.javazone.app.model.Format
import no.javazone.app.model.Session
import no.javazone.app.model.TimeSlot

/**
 * Single immutable snapshot of everything the program screens render.
 *
 * Task 4 starting point: the fields are all here so the provided screens
 * compile — your job is to fill out the TODO bodies (the filtering logic)
 * and build the ViewModel that owns this state.
 */
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
    /** Tabs always show all days, even when filters empty one of them. */
    val dayTabs: List<LocalDate> by lazy { TODO("Task 4: every conference day, from sessions.toConferenceDays()") }

    val hasActiveFilters: Boolean
        get() = TODO("Task 4: any format/language filter or search active?")

    val showOfflineBanner: Boolean
        get() = TODO("Task 4: offline and not dismissed")

    fun session(id: String?): Session? = TODO("Task 4: look the session up by id")

    /** The selected day's slots with format/language filters applied (Program tab). */
    fun daySlots(day: LocalDate?): List<TimeSlot> = TODO("Task 4: filter sessions, then group into the day's slots")

    /** The selected day's favorited sessions, unfiltered (My Schedule tab). */
    fun favoriteSlots(day: LocalDate?): List<TimeSlot> = TODO("Task 4: favorites only, grouped into the day's slots")
}
