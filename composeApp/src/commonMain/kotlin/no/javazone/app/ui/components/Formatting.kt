package no.javazone.app.ui.components

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

// Hardcoded English, 24-hour formatting on purpose: avoids platform locale
// surprises across four targets (DESIGN.md §6.1).

/** "Wed 2 Sep" */
fun LocalDate.dayLabel(): String =
    "${dayOfWeek.name.short()} $day ${month.name.short()}"

/** "09:00" */
fun LocalDateTime.timeLabel(): String =
    "${hour.pad()}:${minute.pad()}"

/** "09:00 – 09:45", or "Time TBA" when the schedule is not published. */
fun timeRangeLabel(start: LocalDateTime?, end: LocalDateTime?): String = when {
    start == null -> "Time TBA"
    end == null -> start.timeLabel()
    else -> "${start.timeLabel()} – ${end.timeLabel()}"
}

private fun String.short() = take(3).lowercase().replaceFirstChar { it.uppercase() }

private fun Int.pad() = toString().padStart(2, '0')
