package no.javazone.app.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/** One sticky-header group; a null [start] means the schedule is not published ("Time TBA"). */
data class TimeSlot(
    val start: LocalDateTime?,
    val end: LocalDateTime?,
    val sessions: List<Session>,
)

data class ConferenceDay(
    val date: LocalDate,
    val slots: List<TimeSlot>,
)

/**
 * Groups sessions into days and start slots, ready for the program screen.
 * Sessions without a published time land in a trailing "Time TBA" slot on the
 * conference Wednesday — the UI must survive an unpublished schedule (DESIGN §6.3).
 */
fun List<Session>.toConferenceDays(): List<ConferenceDay> {
    val (scheduled, unscheduled) = partition { it.startSlot != null }
    val byDate = scheduled.groupBy { it.startSlot!!.date }
    val tbaDate = if (unscheduled.isEmpty()) null else fallbackDate(byDate.keys)
    val dates = (byDate.keys + setOfNotNull(tbaDate)).sorted()
    return dates.map { date ->
        val slots = byDate[date].orEmpty()
            .groupBy { it.startSlot!! }
            .map { (start, sessions) ->
                TimeSlot(
                    start = start,
                    end = sessions.mapNotNull { it.endTime }.maxOrNull(),
                    sessions = sessions.sortedWith(compareBy({ it.startTime }, { it.room })),
                )
            }
            .sortedBy { it.start }
        val tbaSlot = if (date == tbaDate) {
            listOf(TimeSlot(start = null, end = null, sessions = unscheduled.sortedBy { it.title }))
        } else {
            emptyList()
        }
        ConferenceDay(date, slots + tbaSlot)
    }
}

private fun fallbackDate(dates: Set<LocalDate>): LocalDate =
    dates.firstOrNull { it.dayOfWeek == DayOfWeek.WEDNESDAY }
        ?: dates.minOrNull()
        ?: LocalDate(2026, 9, 2)
