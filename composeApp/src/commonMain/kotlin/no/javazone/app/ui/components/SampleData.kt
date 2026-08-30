package no.javazone.app.ui.components

import kotlinx.datetime.LocalDateTime
import no.javazone.app.model.Format
import no.javazone.app.model.Session
import no.javazone.app.model.Speaker

/** Fixture for previews and workshop Task 1 — realistic, not real. */
val sampleSession = Session(
    id = "sample-1",
    title = "Kodearkeologer på legacy-eventyr: fra Java 8 til 25 uten nedetid",
    abstract = "Har du noen gang arvet kode som gjorde deg motløs og sinna?\n\n" +
        "Bli med på utgravning i programvare med røtter tilbake til 2002, " +
        "og se hvordan vi fant motivasjon, trygghet og momentum.",
    format = Format.PRESENTATION,
    lengthMinutes = 45,
    language = "no",
    intendedAudience = "Utviklere som har arvet programvare",
    keywords = listOf("legacy", "migrering", "arkitektur"),
    speakers = listOf(
        Speaker("Anders Norås", bio = "Developer and frequent JavaZone speaker."),
        Speaker("Kari Nordmann", bio = "Tech lead, digs old code for fun."),
    ),
    room = "Room I",
    startTime = LocalDateTime(2026, 9, 2, 9, 0),
    endTime = LocalDateTime(2026, 9, 2, 9, 45),
    startSlot = LocalDateTime(2026, 9, 2, 9, 0),
    videoId = null,
    workshopPrerequisites = null,
)
