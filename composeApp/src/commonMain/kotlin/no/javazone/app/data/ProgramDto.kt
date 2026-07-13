package no.javazone.app.data

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import no.javazone.app.model.Format
import no.javazone.app.model.Session
import no.javazone.app.model.Speaker

/**
 * Shared JSON parser for the program feed. The real API sends fields we don't
 * model and the occasional stray control character, so we parse leniently.
 */
val ProgramJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/**
 * DTOs mirroring the JavaZone (sleepingpill) program API.
 * Kept separate from the domain models in [no.javazone.app.model]:
 * the wire format is not ours to change, the domain model is.
 */
@Serializable
data class ProgramDto(
    val sessions: List<SessionDto>,
)

@Serializable
data class SessionDto(
    val id: String,
    val sessionId: String? = null,
    val conferenceId: String? = null,
    val title: String,
    val abstract: String = "",
    val format: String,
    val length: String,
    val language: String = "en",
    val intendedAudience: String? = null,
    val suggestedKeywords: String? = null,
    val speakers: List<SpeakerDto> = emptyList(),
    val room: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val startSlot: String? = null,
    val video: String? = null,
    val workshopPrerequisites: String? = null,
)

@Serializable
data class SpeakerDto(
    val name: String,
    val bio: String? = null,
    val twitter: String? = null,
    val linkedin: String? = null,
    val bluesky: String? = null,
)

fun SessionDto.toSession() = Session(
    id = id,
    title = title.trim(),
    abstract = abstract.trim(),
    format = when (format) {
        "lightning-talk" -> Format.LIGHTNING_TALK
        "workshop" -> Format.WORKSHOP
        else -> Format.PRESENTATION
    },
    lengthMinutes = length.toIntOrNull() ?: 45,
    language = language,
    intendedAudience = intendedAudience,
    keywords = suggestedKeywords.orEmpty().split(",").map { it.trim() }.filter { it.isNotEmpty() },
    speakers = speakers.map { Speaker(it.name.trim(), it.bio, it.twitter, it.linkedin, it.bluesky) },
    room = room,
    startTime = startTime?.let(LocalDateTime::parse),
    endTime = endTime?.let(LocalDateTime::parse),
    startSlot = startSlot?.let(LocalDateTime::parse),
    videoId = video,
    workshopPrerequisites = workshopPrerequisites,
)
