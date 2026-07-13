package no.javazone.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * The Task 3 punchline: on expanded windows "detail" is a second pane, not a
 * navigation destination. Compact/medium render only the list; the detail is
 * a pushed route there.
 */
@Composable
fun ListDetailLayout(
    expanded: Boolean,
    list: @Composable () -> Unit,
    detail: @Composable () -> Unit,
) {
    if (expanded) {
        Row(Modifier.fillMaxSize()) {
            Box(Modifier.weight(0.42f)) { list() }
            Box(Modifier.weight(0.58f)) { detail() }
        }
    } else {
        list()
    }
}
