package no.javazone.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import no.javazone.app.data.ProgramDto
import no.javazone.app.data.ProgramJson
import no.javazone.app.data.toSession
import no.javazone.app.model.Session
import no.javazone.app.resources.Res
import no.javazone.app.ui.program.ProgramScreen
import no.javazone.app.ui.theme.JavaZoneTheme

/**
 * Task 2 root: load the bundled program, then show the day-tabbed [ProgramScreen].
 *
 * Task 3 wraps this in an adaptive scaffold; Task 4 replaces the bundled load
 * with a ViewModel and Task 5 with a real network fetch.
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
                ProgramScreen(loaded)
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
