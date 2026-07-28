package no.javazone.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.javazone.app.data.ProgramDto
import no.javazone.app.data.ProgramJson
import no.javazone.app.data.toSession
import no.javazone.app.model.Session
import no.javazone.app.resources.Res
import no.javazone.app.ui.components.SessionCard
import no.javazone.app.ui.theme.JavaZoneTheme

/**
 * Placeholder root composable for Task 0.
 *
 * It reads the bundled program.json through the DTO + mapping the starter
 * already ships, then shows how many sessions loaded. Over Tasks 1–6 you will
 * replace this with the real UI, state layer and data layer — this file is the
 * first thing Task 1 grows into a SessionCard-driven program list.
 */
@Composable
fun App() {
    JavaZoneTheme {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            val sessions by produceState<List<Session>?>(initialValue = null) {
                value = loadBundledSessions()
            }
            val loaded = sessions
            if (loaded == null) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                var favoriteIds by remember { mutableStateOf(setOf<String>()) }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                ) {
                    item {
                        Text(
                            text = "JavaZone 2026 — ${loaded.size} sessions loaded",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 16.dp),
                        )
                    }
                    items(loaded.take(5), key = { it.id }) { session ->
                        SessionCard(
                            session = session,
                            isFavorite = session.id in favoriteIds,
                            onClick = {},
                            onToggleFavorite = {
                                favoriteIds = if (session.id in favoriteIds) {
                                    favoriteIds - session.id
                                } else {
                                    favoriteIds + session.id
                                }
                            },
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

/** Loads and maps the bundled program.json. Task 5 replaces this with a real network fetch. */
private suspend fun loadBundledSessions(): List<Session> {
    val bytes = Res.readBytes("files/program.json")
    val dto = ProgramJson.decodeFromString(ProgramDto.serializer(), bytes.decodeToString())
    return dto.sessions.map { it.toSession() }
}
