package no.javazone.app.data

import kotlinx.datetime.LocalDateTime
import no.javazone.app.model.Format
import no.javazone.app.model.toConferenceDays
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProgramMappingTest {

    // Real sleepingpill shape, including fields we don't model (they must be ignored).
    private val programJson = """
        {
          "sessions": [
            {
              "id": "a1", "sessionId": "a1", "conferenceId": "c1",
              "title": "  Kotlin everywhere  ",
              "abstract": "A talk.",
              "format": "lightning-talk", "length": "20", "language": "en",
              "suggestedKeywords": "kotlin, multiplatform, ",
              "speakers": [{"name": "Ada", "bio": "Bio", "twitter": "@ada", "bluesky": "ignored"}],
              "room": "Room VI",
              "startTime": "2026-09-02T10:20", "endTime": "2026-09-02T10:40",
              "startSlot": "2026-09-02T10:20", "startSlotZulu": "2026-09-02T08:20:00Z"
            },
            {
              "id": "b2", "sessionId": "b2",
              "title": "Unscheduled workshop",
              "abstract": "Hands-on.",
              "format": "workshop", "length": "120", "language": "no",
              "workshopPrerequisites": "A laptop."
            }
          ]
        }
    """

    @Test
    fun mapsDtoToDomain() {
        val session = ProgramJson.decodeFromString(ProgramDto.serializer(), programJson)
            .sessions.first().toSession()

        assertEquals("Kotlin everywhere", session.title)
        assertEquals(Format.LIGHTNING_TALK, session.format)
        assertEquals(20, session.lengthMinutes)
        assertEquals(listOf("kotlin", "multiplatform"), session.keywords)
        assertEquals("Ada", session.speakers.single().name)
        assertEquals("Room VI", session.room)
        assertEquals(LocalDateTime(2026, 9, 2, 10, 20), session.startTime)
    }

    @Test
    fun unscheduledSessionMapsToNullSchedule() {
        val session = ProgramJson.decodeFromString(ProgramDto.serializer(), programJson)
            .sessions.last().toSession()

        assertEquals(Format.WORKSHOP, session.format)
        assertEquals("A laptop.", session.workshopPrerequisites)
        assertNull(session.room)
        assertNull(session.startTime)
    }

    @Test
    fun groupsScheduledSessionsIntoDays() {
        val sessions = ProgramJson.decodeFromString(ProgramDto.serializer(), programJson)
            .sessions.map { it.toSession() }

        val days = sessions.toConferenceDays()

        assertEquals(1, days.size)
        // Timed slot first, then the "Time TBA" pseudo-slot with the unscheduled workshop.
        assertEquals(2, days.single().slots.size)
        assertEquals(LocalDateTime(2026, 9, 2, 10, 20), days.single().slots.first().start)
        assertNull(days.single().slots.last().start)
        assertEquals("Unscheduled workshop", days.single().slots.last().sessions.single().title)
    }
}
