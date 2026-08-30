package no.javazone.app.ui.components

import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate

/** One tab per conference day ("Tue 1 Sep" …). */
@Composable
fun DayTabRow(days: List<LocalDate>, selected: LocalDate?, onSelect: (LocalDate) -> Unit) {
    if (days.isEmpty()) return
    val selectedIndex = days.indexOf(selected).coerceAtLeast(0)
    PrimaryTabRow(selectedTabIndex = selectedIndex) {
        days.forEach { day ->
            Tab(
                selected = day == selected,
                onClick = { onSelect(day) },
                text = { Text(day.dayLabel(), style = MaterialTheme.typography.titleSmall, maxLines = 1) },
            )
        }
    }
}
