package no.javazone.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
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
import no.javazone.app.ui.components.LoadingState
import no.javazone.app.ui.detail.SessionDetailScreen
import no.javazone.app.ui.favorites.ScheduleScreen
import no.javazone.app.ui.info.InfoScreen
import no.javazone.app.ui.program.ProgramIntent
import no.javazone.app.ui.program.ProgramScreen
import no.javazone.app.ui.program.ProgramViewModel
import no.javazone.app.ui.theme.JavaZoneTheme

/** Root composable: theme, one shared ViewModel (manual wiring, no DI), adaptive scaffold, NavHost. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun App() {
    JavaZoneTheme {
        val viewModel: ProgramViewModel = viewModel { ProgramViewModel() }
        val state by viewModel.state.collectAsState()
        val navController = rememberNavController()
        val windowWidth = currentWindowWidth()
        val expanded = windowWidth == WindowWidth.Expanded
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        // Standard M3 top-level navigation: tabs keep their state, back returns to Program.
        fun navigateTopLevel(route: String) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }

        // On expanded windows the detail is the second pane — state, not navigation (§2.3).
        fun openSession(sessionId: String) {
            viewModel.onIntent(ProgramIntent.SelectSession(sessionId))
            if (!expanded) navController.navigate("session/$sessionId")
        }

        // The detail pane only exists on the Program and My Schedule tabs.
        val onListDetailTab = currentRoute == TopDestination.Program.route ||
            currentRoute == TopDestination.Schedule.route

        // Back/Esc on expanded windows clears the pane selection first (§1.3) —
        // but only where the pane is actually visible.
        BackHandler(enabled = expanded && onListDetailTab && state.selectedSessionId != null) {
            viewModel.onIntent(ProgramIntent.SelectSession(null))
        }

        // Window resized across the 840 dp breakpoint: keep the open detail visible (§2.3).
        var wasExpanded by remember { mutableStateOf(expanded) }
        LaunchedEffect(expanded) {
            val route = navController.currentBackStackEntry?.destination?.route
            val onDetailRoute = route?.startsWith("session") == true
            val onPaneTab = route == TopDestination.Program.route ||
                route == TopDestination.Schedule.route
            when {
                expanded && !wasExpanded && onDetailRoute ->
                    navController.navigateUp() // the pane takes over via selectedSessionId
                !expanded && wasExpanded && onPaneTab && state.selectedSessionId != null ->
                    navController.navigate("session/${state.selectedSessionId}")
            }
            wasExpanded = expanded
        }

        AdaptiveScaffold(windowWidth, currentRoute, onNavigate = ::navigateTopLevel) {
            NavHost(navController, startDestination = TopDestination.Program.route) {
                composable(TopDestination.Program.route) {
                    ProgramScreen(state, viewModel::onIntent, expanded, onOpenSession = ::openSession)
                }
                composable(TopDestination.Schedule.route) {
                    ScheduleScreen(
                        state = state,
                        onIntent = viewModel::onIntent,
                        expanded = expanded,
                        onOpenSession = ::openSession,
                        onBrowseProgram = { navigateTopLevel(TopDestination.Program.route) },
                    )
                }
                composable(TopDestination.Info.route) { InfoScreen() }
                composable(TopDestination.Map.route) {
                    // The venue map is a Task 6 stretch — placeholder until then.
                    EmptyState(
                        icon = Icons.Outlined.Place,
                        title = "Venue map",
                        body = "Coming in Task 6.",
                    )
                }
                composable("session/{sessionId}") { entry ->
                    // The route argument is the source of truth: it survives Android
                    // process death, where the ViewModel's selection state does not.
                    val sessionId = entry.arguments?.read { getStringOrNull("sessionId") }
                    val session = state.session(sessionId)
                    when {
                        session != null -> SessionDetailScreen(
                            session = session,
                            isFavorite = session.id in state.favoriteIds,
                            onBack = { navController.navigateUp() },
                            onToggleFavorite = { viewModel.onIntent(ProgramIntent.ToggleFavorite(session.id)) },
                        )
                        state.isLoading -> LoadingState()
                        else -> EmptyState(
                            icon = Icons.Outlined.Warning,
                            title = "Session not found",
                            body = "This session is not in the current program.",
                            actionLabel = "Back to program",
                            onAction = { navigateTopLevel(TopDestination.Program.route) },
                        )
                    }
                }
            }
        }
    }
}
