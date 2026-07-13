package no.javazone.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.javazone.app.model.TimeSlot

/** The day's sessions, grouped under sticky time-slot headers. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SessionList(
    slots: List<TimeSlot>,
    favoriteIds: Set<String>,
    onSessionClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    selectedSessionId: String? = null,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
    ) {
        slots.forEach { slot ->
            stickyHeader(key = "header-${slot.start}") {
                TimeSlotHeader(slot.start, slot.end)
            }
            items(slot.sessions, key = { it.id }) { session ->
                SessionCard(
                    session = session,
                    isFavorite = session.id in favoriteIds,
                    onClick = { onSessionClick(session.id) },
                    onToggleFavorite = { onToggleFavorite(session.id) },
                    selected = session.id == selectedSessionId,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        }
    }
}
