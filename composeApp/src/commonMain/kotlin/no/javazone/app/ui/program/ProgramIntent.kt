package no.javazone.app.ui.program

import kotlinx.datetime.LocalDate
import no.javazone.app.model.Format

/** Everything the user can do on the program screens, as data (MVI-flavored MVVM). */
sealed interface ProgramIntent {
    data class SelectDay(val day: LocalDate) : ProgramIntent
    data class ToggleFormat(val format: Format) : ProgramIntent
    data class ToggleLanguage(val language: String) : ProgramIntent
    data class Search(val query: String) : ProgramIntent
    data object ClearFilters : ProgramIntent
    data class ToggleFavorite(val sessionId: String) : ProgramIntent
    data class SelectSession(val sessionId: String?) : ProgramIntent
    data object DismissOfflineBanner : ProgramIntent
    data object Retry : ProgramIntent
}
