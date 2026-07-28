package no.javazone.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.savedstate.read
import no.javazone.app.ui.AdaptiveScaffold
import no.javazone.app.ui.TopDestination
import no.javazone.app.ui.WindowWidth
import no.javazone.app.ui.currentWindowWidth
import no.javazone.app.ui.components.EmptyState
import no.javazone.app.ui.detail.SessionDetailScreen
import no.javazone.app.ui.program.ProgramIntent
import no.javazone.app.ui.program.ProgramScreen
import no.javazone.app.ui.program.ProgramViewModel
import no.javazone.app.ui.theme.JavaZoneTheme

/** Root composable: theme, one shared ViewModel, adaptive scaffold, NavHost. */
@Composable
fun App() {
    JavaZoneTheme {
        val viewModel: ProgramViewModel = viewModel { ProgramViewModel() }
        val state by viewModel.state.collectAsState()
        val navController = rememberNavController()
        val windowWidth = currentWindowWidth()
        val expanded = windowWidth == WindowWidth.Expanded
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route ?: TopDestination.Program.route

        // On expanded windows the detail is a second pane (state); elsewhere it's a pushed route.
        fun openSession(sessionId: String) {
            viewModel.onIntent(ProgramIntent.SelectSession(sessionId))
            if (!expanded) navController.navigate("session/$sessionId")
        }

        AdaptiveScaffold(
            windowWidth = windowWidth,
            currentRoute = currentRoute,
            onNavigate = { navController.navigate(it) },
        ) {
            NavHost(navController, startDestination = TopDestination.Program.route) {
                composable(TopDestination.Program.route) {
                    ProgramScreen(state, viewModel::onIntent, expanded, onOpenSession = ::openSession)
                }
                composable(TopDestination.Schedule.route) {
                    EmptyState(
                        icon = Icons.Outlined.Star,
                        title = "My schedule",
                        body = "Coming soon.",
                    )
                }
                composable(TopDestination.Info.route) {
                    EmptyState(icon = Icons.Outlined.Info, title = "Practical info", body = "Coming soon.")
                }
                composable(TopDestination.Map.route) {
                    EmptyState(icon = Icons.Outlined.Place, title = "Venue map", body = "Coming in Task 6.")
                }
                composable("session/{sessionId}") { entry ->
                    // The route argument is the source of truth: it survives Android
                    // process death, where the ViewModel's selection state does not.
                    val sessionId = entry.arguments?.read { getStringOrNull("sessionId") }
                    val session = state.session(sessionId)
                    if (session != null) {
                        SessionDetailScreen(
                            session = session,
                            isFavorite = session.id in state.favoriteIds,
                            onBack = { navController.navigateUp() },
                            onToggleFavorite = { viewModel.onIntent(ProgramIntent.ToggleFavorite(session.id)) },
                        )
                    } else {
                        EmptyState(
                            icon = Icons.Outlined.Warning,
                            title = "Session not found",
                            body = "This session is not in the current program.",
                        )
                    }
                }
            }
        }
    }
}
