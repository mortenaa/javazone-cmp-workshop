package no.javazone.app.ui.program

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import no.javazone.app.model.Session
import no.javazone.app.model.toConferenceDays
import no.javazone.app.ui.components.DayTabRow
import no.javazone.app.ui.components.EmptyState
import no.javazone.app.ui.components.ListDetailLayout
import no.javazone.app.ui.components.SessionList
import no.javazone.app.ui.detail.SessionDetailContent

/**
 * Task 3 program screen: the Task 2 list, now inside a [ListDetailLayout].
 *
 * On expanded windows the tapped session becomes the right-hand pane (state),
 * not a navigation destination. Selection still lives in `remember` here — the
 * awkward part that Task 4 moves into the ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramScreen(sessions: List<Session>, expanded: Boolean) {
    val days = remember(sessions) { sessions.toConferenceDays() }
    var selectedDay by remember(days) { mutableStateOf(days.firstOrNull()?.date) }
    var favoriteIds by remember { mutableStateOf(emptySet<String>()) }
    var selectedSessionId by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("JavaZone 2026") }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            DayTabRow(days.map { it.date }, selectedDay) { selectedDay = it }

            val slots = remember(days, selectedDay) {
                days.firstOrNull { it.date == selectedDay }?.slots.orEmpty()
            }
            ListDetailLayout(
                expanded = expanded,
                list = {
                    if (slots.isEmpty()) {
                        EmptyState(
                            icon = Icons.Outlined.DateRange,
                            title = "No sessions",
                            body = "There are no sessions on this day.",
                        )
                    } else {
                        SessionList(
                            slots = slots,
                            favoriteIds = favoriteIds,
                            onSessionClick = { selectedSessionId = it },
                            onToggleFavorite = { id ->
                                favoriteIds =
                                    if (id in favoriteIds) favoriteIds - id else favoriteIds + id
                            },
                            selectedSessionId = if (expanded) selectedSessionId else null,
                        )
                    }
                },
                detail = {
                    val session = sessions.firstOrNull { it.id == selectedSessionId }
                    if (session == null) {
                        EmptyState(
                            icon = Icons.Outlined.DateRange,
                            title = "Select a session",
                            body = "Pick a session from the list to see its details.",
                        )
                    } else {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            SessionDetailContent(
                                session = session,
                                isFavorite = session.id in favoriteIds,
                                onToggleFavorite = {
                                    favoriteIds = if (session.id in favoriteIds) {
                                        favoriteIds - session.id
                                    } else {
                                        favoriteIds + session.id
                                    }
                                },
                            )
                        }
                    }
                },
            )
        }
    }
}
