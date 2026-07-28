package no.javazone.app.ui.program

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
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
import no.javazone.app.ui.components.SessionList
import no.javazone.app.ui.detail.SessionDetailContent

/**
 * Screen is now a pure function of `(state, onIntent)` — Task 2/3 kept
 * selected day, favorites and selection in local `remember`; all of that now
 * lives in [ProgramViewModel]/[ProgramUiState].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramScreen(
    state: ProgramUiState,
    onIntent: (ProgramIntent) -> Unit,
    expanded: Boolean,
    onOpenSession: (String) -> Unit,
) {
    Scaffold(topBar = { TopAppBar(title = { Text("JavaZone 2026") }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            DayTabRow(state.dayTabs, state.selectedDay) { onIntent(ProgramIntent.SelectDay(it)) }

            val slots = remember(state) { state.daySlots(state.selectedDay) }
            ListDetailLayout(
                expanded = expanded,
                list = {
                    if (slots.isEmpty()) {
                        EmptyState(
                            icon = Icons.Outlined.DateRange,
                            title = "No sessions match",
                            body = "Try clearing your search or filters.",
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        SessionList(
                            slots = slots,
                            favoriteIds = state.favoriteIds,
                            onSessionClick = onOpenSession,
                            onToggleFavorite = { onIntent(ProgramIntent.ToggleFavorite(it)) },
                            selectedSessionId = if (expanded) state.selectedSessionId else null,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                },
                detail = {
                    val session = state.session(state.selectedSessionId)
                    if (session == null) {
                        EmptyState(
                            icon = Icons.Outlined.DateRange,
                            title = "Select a session",
                            body = "Pick a session from the list to see its details.",
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        SessionDetailContent(
                            session = session,
                            isFavorite = session.id in state.favoriteIds,
                            onToggleFavorite = { onIntent(ProgramIntent.ToggleFavorite(session.id)) },
                        )
                    }
                },
            )
        }
    }
}
