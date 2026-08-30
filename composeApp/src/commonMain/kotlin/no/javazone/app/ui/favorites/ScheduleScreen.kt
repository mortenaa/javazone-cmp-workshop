package no.javazone.app.ui.favorites

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import no.javazone.app.ui.components.DayTabRow
import no.javazone.app.ui.components.EmptyState
import no.javazone.app.ui.components.ListDetailLayout
import no.javazone.app.ui.components.LoadingState
import no.javazone.app.ui.components.OfflineBanner
import no.javazone.app.ui.components.SessionList
import no.javazone.app.ui.components.StarOutline
import no.javazone.app.ui.detail.SessionDetailPane
import no.javazone.app.ui.program.ProgramIntent
import no.javazone.app.ui.program.ProgramUiState

/** My Schedule: the program list filtered to favorites. Overlaps simply share a slot header. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    state: ProgramUiState,
    onIntent: (ProgramIntent) -> Unit,
    expanded: Boolean,
    onOpenSession: (String) -> Unit,
    onBrowseProgram: () -> Unit,
) {
    Scaffold(topBar = { TopAppBar(title = { Text("My schedule") }) }) { padding ->
        Column(Modifier.padding(padding)) {
            if (state.isLoading) {
                LoadingState()
                return@Column
            }
            if (state.favoriteIds.isEmpty()) {
                EmptyState(
                    icon = StarOutline,
                    title = "Nothing here yet",
                    body = "Tap the star on any session to build your personal schedule.",
                    actionLabel = "Browse the program",
                    onAction = onBrowseProgram,
                )
                return@Column
            }
            DayTabRow(state.dayTabs, state.selectedDay) { onIntent(ProgramIntent.SelectDay(it)) }
            if (state.showOfflineBanner) {
                OfflineBanner { onIntent(ProgramIntent.DismissOfflineBanner) }
            }
            ListDetailLayout(
                expanded = expanded,
                list = { FavoritesList(state, onIntent, expanded, onOpenSession) },
                detail = { SessionDetailPane(state, onIntent) },
            )
        }
    }
}

@Composable
private fun FavoritesList(
    state: ProgramUiState,
    onIntent: (ProgramIntent) -> Unit,
    expanded: Boolean,
    onOpenSession: (String) -> Unit,
) {
    // remember: recompute the grouping when the state changes, not on every recomposition.
    val slots = remember(state) { state.favoriteSlots(state.selectedDay) }
    if (slots.isEmpty()) {
        EmptyState(
            icon = StarOutline,
            title = "Nothing on this day",
            body = "Your favorites are on another day.",
        )
    } else {
        SessionList(
            slots = slots,
            favoriteIds = state.favoriteIds,
            onSessionClick = onOpenSession,
            onToggleFavorite = { onIntent(ProgramIntent.ToggleFavorite(it)) },
            selectedSessionId = if (expanded) state.selectedSessionId else null,
        )
    }
}
