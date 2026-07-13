package no.javazone.app.ui.map

/**
 * Room markers in normalized (0..1) coordinates relative to the map image, so
 * swapping in a real Oslo Spektrum floor plan only means new numbers here.
 * ASSUMPTION: room layout based on previous JavaZones (DESIGN.md §3.6).
 */
data class VenueMarker(
    val label: String,
    val room: String,
    val hint: String,
    val x: Float,
    val y: Float,
)

val venueMarkers = listOf(
    VenueMarker("I", "Room I", "Upper level, north-west corner", 0.16f, 0.22f),
    VenueMarker("II", "Room II", "Upper level, north side", 0.38f, 0.16f),
    VenueMarker("III", "Room III", "Upper level, north side", 0.62f, 0.16f),
    VenueMarker("IV", "Room IV", "Upper level, north-east corner", 0.84f, 0.22f),
    VenueMarker("V", "Room V", "Upper level, south-east corner", 0.84f, 0.72f),
    VenueMarker("VI", "Room VI", "Lightning talks — south side", 0.62f, 0.84f),
    VenueMarker("VII", "Room VII", "Upper level, south-west corner", 0.16f, 0.72f),
    VenueMarker("E", "Expo", "Arena floor — partners and food", 0.5f, 0.5f),
)
