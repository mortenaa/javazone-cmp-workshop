package no.javazone.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
 * Task 1 root: a scrollable column of [SessionCard]s.
 *
 * Task 2 turns this into a real day-tabbed program list with sticky headers;
 * for now it just proves the card renders against real data. Favorites are a
 * throwaway in-memory Set — the state layer arrives in Task 4.
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
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                var favoriteIds by remember { mutableStateOf(emptySet<String>()) }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "JavaZone 2026",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    loaded.take(12).forEach { session ->
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
