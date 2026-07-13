package no.javazone.app.model

import kotlinx.datetime.LocalDateTime

enum class Format { PRESENTATION, LIGHTNING_TALK, WORKSHOP }

data class Speaker(
    val name: String,
    val bio: String? = null,
    val twitter: String? = null,
    val linkedin: String? = null,
    val bluesky: String? = null,
)

data class Session(
    val id: String,
    val title: String,
    val abstract: String,
    val format: Format,
    val lengthMinutes: Int,
    val language: String,
    val intendedAudience: String?,
    val keywords: List<String>,
    val speakers: List<Speaker>,
    val room: String?,
    val startTime: LocalDateTime?,
    val endTime: LocalDateTime?,
    val startSlot: LocalDateTime?,
    val videoId: String?,
    val workshopPrerequisites: String?,
)
