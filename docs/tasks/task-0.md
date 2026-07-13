# Task 0 — Run it and tour the project

**Goal:** get the starter app running on at least two targets, and find your way
around the project structure.

**Time box:** ~10 min · **Compare against:** `main` (this *is* checkpoint 0)

## Theory recap

See the *Block 0 — Why multiplatform?* slides: one Kotlin codebase, five source
sets (`commonMain` + one per platform), and one `App()` composable hosted by each
platform. Almost everything you write today lands in `commonMain`.

## Steps

1. **Desktop first** — it's the fastest, no emulator needed:
   ```bash
   ./gradlew :composeApp:run
   ```
   You should see a window: **"JavaZone 2026 — N sessions loaded"**.
2. **Run on Android** from the IDE: pick the `composeApp` run configuration and an
   emulator, then run.
3. **Web:**
   ```bash
   ./gradlew :composeApp:wasmJsBrowserDevelopmentRun
   ```
4. **Mac users:** run the iOS app via the `iosApp` run configuration (or open
   `iosApp/iosApp.xcodeproj` in Xcode).
5. **Tour the source.** Open `composeApp/src/` and find the five source sets:
   `commonMain`, `androidMain`, `iosMain`, `jvmMain`, `wasmJsMain`. Then open
   `commonMain/kotlin/no/javazone/app/App.kt` — this is what Task 1 grows into a
   real UI.

## Where things live

- `model/` — the domain model (`Session`, `Speaker`, `Format`, `TimeSlot`).
- `data/ProgramDto.kt` — the API DTOs, the lenient JSON parser, and the
  DTO→domain mapping. **Provided** — you won't touch the wire format.
- `ui/theme/Theme.kt` — the JavaZone Material 3 colours.
- `ui/components/` — small provided helpers: `SampleData`, `EmptyState`,
  `StarOutlineIcon`, date/format helpers.
- `composeResources/files/program.json` — the bundled program, so the app works
  offline from day one.

## Done when…

- [ ] The Desktop app runs and shows "N sessions loaded".
- [ ] You've run **one more** target (Android, Web, or iOS on a Mac).
- [ ] You can point at `commonMain` and at least one platform source set.

## Expected result

A window (or emulator/browser) showing the JavaZone title, the number of loaded
sessions, and a hint pointing at `docs/tasks/task-1.md`. Nothing interactive yet —
that's what you build next.

## Troubleshooting

Anything failing here (Android SDK, emulator, Xcode tools, slow Gradle) is
covered in **[../SETUP.md](../SETUP.md)** §4. Grab one of us if you're stuck.
