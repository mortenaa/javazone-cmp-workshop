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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import no.javazone.app.model.Session
import no.javazone.app.model.toConferenceDays
import no.javazone.app.ui.components.DayTabRow
import no.javazone.app.ui.components.EmptyState
import no.javazone.app.ui.components.SessionList

/**
 * Task 2 program screen: day tabs over a sticky-header session list.
 *
 * State (selected day, favorites) lives right here in `remember` for now. That
 * gets awkward fast — which is exactly the motivation for the ViewModel in
 * Task 4.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramScreen(sessions: List<Session>) {
    val days = remember(sessions) { sessions.toConferenceDays() }
    var selectedDay by remember(days) { mutableStateOf(days.firstOrNull()?.date) }
    var favoriteIds by remember { mutableStateOf(emptySet<String>()) }

    Scaffold(topBar = { TopAppBar(title = { Text("JavaZone 2026") }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            DayTabRow(days.map { it.date }, selectedDay) { selectedDay = it }

            val slots = remember(days, selectedDay) {
                days.firstOrNull { it.date == selectedDay }?.slots.orEmpty()
            }
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
                    onSessionClick = {},
                    onToggleFavorite = { id ->
                        favoriteIds = if (id in favoriteIds) favoriteIds - id else favoriteIds + id
                    },
                )
            }
        }
    }
}
