package no.javazone.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import no.javazone.app.data.ProgramDto
import no.javazone.app.data.ProgramJson
import no.javazone.app.data.toSession
import no.javazone.app.model.Session
import no.javazone.app.resources.Res
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
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                val loaded = sessions
                if (loaded == null) {
                    CircularProgressIndicator()
                } else {
                    Text(
                        text = "JavaZone 2026",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "${loaded.size} sessions loaded",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Text(
                        text = "Your starter builds and runs. Open docs/tasks/task-1.md to begin.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp),
                    )
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
