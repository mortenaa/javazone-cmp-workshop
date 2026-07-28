package no.javazone.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** On expanded windows "detail" is a second pane (state); elsewhere it's just the list. */
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
