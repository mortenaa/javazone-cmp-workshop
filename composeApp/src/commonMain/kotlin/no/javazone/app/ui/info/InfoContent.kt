package no.javazone.app.ui.info

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

/** Content as data, UI as a loop — the warm-up teaching beat (DESIGN.md §3.5). */
data class InfoSection(
    val icon: ImageVector,
    val title: String,
    val body: String,
    val emphasis: String? = null,
)

// ASSUMPTION: placeholder copy based on previous JavaZones — replace with the
// official 2026 practical info when javaBin publishes it.
val infoSections = listOf(
    InfoSection(
        icon = Icons.Outlined.Place,
        title = "Venue",
        body = "Oslo Spektrum, Sonja Henies plass 2, Oslo. Doors open at 08:00 both conference days. " +
            "Workshops on Tuesday run at the conference hotel.",
    ),
    InfoSection(
        icon = Icons.Outlined.AccountBox,
        title = "Registration",
        body = "Badge pickup at the main entrance from 07:30. Bring your ticket QR code. " +
            "Your badge is your access to all sessions, food and the party.",
    ),
    InfoSection(
        icon = Icons.Outlined.ShoppingCart,
        title = "Food",
        body = "Continuous food service from the stands on the expo floor throughout the day — " +
            "no fixed lunch break. Look for the allergy labels at every stand.",
    ),
    InfoSection(
        icon = Icons.Outlined.Favorite,
        title = "AweZone",
        body = "The legendary conference party, Wednesday evening at Oslo Spektrum. " +
            "Concerts, food and good company. Included in your ticket.",
    ),
    InfoSection(
        icon = Icons.Outlined.Person,
        title = "Partner area",
        body = "The expo floor hosts all partner stands. Say hi, learn what they build, " +
            "and yes — there is swag.",
    ),
    InfoSection(
        icon = Icons.Outlined.Settings,
        title = "Wifi",
        body = "Free conference wifi for all attendees.",
        emphasis = "Network: JavaZone · Password: duke2026",
    ),
    InfoSection(
        icon = Icons.Outlined.Info,
        title = "Code of conduct",
        body = "JavaZone is dedicated to a harassment-free conference experience for everyone. " +
            "Report issues to any crew member or javabin@java.no.",
    ),
)
