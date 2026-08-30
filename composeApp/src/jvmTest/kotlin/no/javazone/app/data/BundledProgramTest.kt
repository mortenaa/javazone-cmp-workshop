package no.javazone.app.data

import kotlinx.coroutines.test.runTest
import no.javazone.app.model.toConferenceDays
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Validates the real bundled program.json produced by tools/enrich-program.py. */
class BundledProgramTest {

    @Test
    fun bundledProgramDecodesAndIsFullyScheduled() = runTest {
        val sessions = ProgramApi().bundledProgram().sessions.map { it.toSession() }

        assertTrue(sessions.size > 100, "expected a full program, got ${sessions.size} sessions")
        assertTrue(sessions.all { it.room != null && it.startTime != null && it.startSlot != null })
        assertEquals(3, sessions.toConferenceDays().size) // workshop day + two conference days
    }
}
