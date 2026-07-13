# Stretch tasks

For the fast, the curious, and the flight home. Two flavours: some have a full
reference implementation (in `checkpoint-6` or a dedicated branch), some are
genuinely open.

## With a reference implementation

### 🗺 Venue map

A zoomable, pannable venue map in **pure Compose** — `Image` +
`Modifier.graphicsLayer` transforms, with tappable room markers overlaid. No
native map SDK, so it's 100% common code. Even if you build nothing, read
`ui/map/MapScreen.kt` and `MapTransform.kt`: pinch-zoom, scroll-wheel zoom and
tappable markers in ~180 lines is a nice existence proof that "custom" doesn't
mean leaving Compose.

*Reference:* `ui/map/` in `checkpoint-6` (`MapScreen`, `MapTransform`,
`VenueMarkers`, and the `venue_map` drawable).

### 🔍 Search polish

An app-bar search field that drives `ProgramIntent.Search`, toggled open/closed
with `rememberSaveable`. Note the deliberate teaching wart: the *open* flag is
saveable (survives process death) but the *query* lives in the ViewModel (does
not) — a documented lesson about the three tiers of state survival.

*Reference:* `SearchField` in `ui/program/ProgramScreen.kt` in `checkpoint-4`+.

### 🌓 Dark-theme toggle

`JavaZoneTheme` already takes a `darkTheme: Boolean`. Add a manual override (a
switch in Info, say) instead of only following the system setting. Hoist the
choice into state and pass it to the theme.

*Reference:* `ui/theme/Theme.kt` (the `darkTheme` parameter is there from `main`).

### 🌊 Nautical animated background

Give the app JavaZone's underwater feel: a full-screen ocean behind the Info
screen, with a wavy surface near the top, bubbles drifting upward and dissolving
as they reach the surface, and the content floating over it on translucent cards.
It is **100% shared Compose drawing and animation** — no platform code — so the
one implementation runs identically on Android, iOS, Desktop and Web. This is the
graphics layer the core tasks deliberately skip, and a great tour of it: `Canvas`,
`Path`, `Brush` and `clipPath` for the water, a per-frame clock (`withFrameNanos`)
for the bubbles, and an `rememberInfiniteTransition` for the wave. Good knobs to
play with: `surfaceFraction` (waterline height), `bubbleCount`, and the fade band.

*Reference:* a dedicated branch — `git checkout stretch/nautical-animation`, then
see `ui/components/NauticalBackground.kt` and how `ui/info/InfoScreen.kt` layers
content over it. Kept off the main line because it is pure decoration.

## Open-ended — no solution provided

### 👤 Speaker list

A proper new feature: a new `TopDestination` + route + screen that groups sessions
by speaker (a speaker can appear in several). Model it, add the route, build the
screen. A good first solo flight without a checkpoint to lean on.

### 🔄 Pull-to-refresh

Add pull-to-refresh on the program list, wired to the existing `Retry` intent /
`loadSessions()`. Look at Material 3's `PullToRefreshBox`.

---

Have fun — and remember the whole finished app is a legitimate template. Fork it,
gut the conference content, keep the skeleton.
