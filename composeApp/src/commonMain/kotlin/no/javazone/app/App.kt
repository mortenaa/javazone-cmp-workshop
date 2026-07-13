package no.javazone.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import no.javazone.app.data.ProgramDto
import no.javazone.app.data.ProgramJson
import no.javazone.app.data.toSession
import no.javazone.app.model.Session
import no.javazone.app.resources.Res
import no.javazone.app.ui.AdaptiveScaffold
import no.javazone.app.ui.TopDestination
import no.javazone.app.ui.WindowWidth
import no.javazone.app.ui.currentWindowWidth
import no.javazone.app.ui.components.EmptyState
import no.javazone.app.ui.program.ProgramScreen
import no.javazone.app.ui.theme.JavaZoneTheme

/**
 * Task 3 root: an [AdaptiveScaffold] (bottom bar ↔ nav rail at 600 dp) hosting
 * the four top-level destinations. The Program screen adds its own list-detail
 * split at 840 dp.
 *
 * Navigation is a plain `remember`-ed route for now; Task 4 swaps it for
 * navigation-compose and a real ViewModel, and the Info/Map/Schedule tabs get
 * real content in Tasks 4 and 6.
 */
@Composable
fun App() {
    JavaZoneTheme {
        val sessions by produceState<List<Session>?>(initialValue = null) {
            value = loadBundledSessions()
        }
        val windowWidth = currentWindowWidth()
        val expanded = windowWidth == WindowWidth.Expanded
        var currentRoute by remember { mutableStateOf(TopDestination.Program.route) }

        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            AdaptiveScaffold(
                windowWidth = windowWidth,
                currentRoute = currentRoute,
                onNavigate = { currentRoute = it },
            ) {
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
                    when (currentRoute) {
                        TopDestination.Program.route -> ProgramScreen(loaded, expanded)
                        TopDestination.Schedule.route -> EmptyState(
                            icon = Icons.Outlined.Star,
                            title = "My schedule",
                            body = "Coming in Task 4 — mark sessions with the star.",
                        )
                        TopDestination.Info.route -> EmptyState(
                            icon = Icons.Outlined.Info,
                            title = "Practical info",
                            body = "Coming in Task 4.",
                        )
                        TopDestination.Map.route -> EmptyState(
                            icon = Icons.Outlined.Place,
                            title = "Venue map",
                            body = "Coming in Task 6.",
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
