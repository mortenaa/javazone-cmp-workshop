package no.javazone.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.window.core.layout.WindowSizeClass
import no.javazone.app.ui.components.StarOutline

enum class WindowWidth { Compact, Medium, Expanded }

@Composable
fun currentWindowWidth(): WindowWidth {
    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return when {
        sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> WindowWidth.Expanded
        sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> WindowWidth.Medium
        else -> WindowWidth.Compact
    }
}

enum class TopDestination(
    val route: String,
    val label: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
) {
    Program("program", "Program", Icons.Filled.DateRange, Icons.Outlined.DateRange),
    Schedule("schedule", "My schedule", Icons.Filled.Star, StarOutline),
    Info("info", "Info", Icons.Filled.Info, Icons.Outlined.Info),
    Map("map", "Map", Icons.Filled.Place, Icons.Outlined.Place),
}

@Composable
fun AdaptiveScaffold(
    windowWidth: WindowWidth,
    currentRoute: String,
    onNavigate: (String) -> Unit,
    content: @Composable () -> Unit,
) {
    if (windowWidth == WindowWidth.Compact) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    TopDestination.entries.forEach { destination ->
                        val selected = currentRoute == destination.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { onNavigate(destination.route) },
                            icon = {
                                Icon(
                                    if (selected) destination.filledIcon else destination.outlinedIcon,
                                    contentDescription = null,
                                )
                            },
                            label = { Text(destination.label) },
                        )
                    }
                }
            },
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) { content() }
        }
    } else {
        Row(Modifier.fillMaxSize()) {
            NavigationRail {
                TopDestination.entries.forEach { destination ->
                    val selected = currentRoute == destination.route
                    NavigationRailItem(
                        selected = selected,
                        onClick = { onNavigate(destination.route) },
                        icon = {
                            Icon(
                                if (selected) destination.filledIcon else destination.outlinedIcon,
                                contentDescription = null,
                            )
                        },
                        label = { Text(destination.label) },
                    )
                }
            }
            Box(Modifier.weight(1f)) { content() }
        }
    }
}
