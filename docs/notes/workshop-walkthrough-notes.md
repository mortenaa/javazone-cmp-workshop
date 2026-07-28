# Workshop walkthrough notes

Notes collected while implementing every task (0–6) from scratch against the
task docs in `docs/tasks/` and the slides in
`compose-multiplatform-workshop/presentation/workshop/slides.md`, on a machine
with **no Android SDK emulator image, no full Xcode install (CLI tools only,
no simulator)**. Everything below was done from the command line rather than
an IDE, per the request driving this exercise — that substitution is called
out wherever it changes what "done" looks like.

Legend: 🐛 likely bug/error · ⚠️ unclear or could be explained better · 💡 works
as documented, minor polish suggestion · ✅ verified working as described.

---

## Methodology / environment caveats (read first)

- No Android emulator and no system images were available, so Android was
  verified by compiling (`:composeApp:compileDebugKotlinAndroid` /
  `assembleDebug`), never by actually running on a device. This is a limit of
  my environment, not a workshop defect — but it means "run on Android from
  the IDE" (Task 0 step 2) could not be literally followed; see the Task 0
  section for the CLI substitute.
- Only Xcode command line tools were present (`xcode-select -p` →
  `/Library/Developer/CommandLineTools`), no `xcrun simctl`, so the iOS
  target could only be **compiled** (`compileKotlinIosSimulatorArm64`), never
  run on a simulator.
- The sandboxed shell has no screen-recording permission, so I could not
  screenshot the Desktop app window to visually confirm "JavaZone 2026 — N
  sessions loaded". Verified instead via: the process starting with no
  exceptions in the Gradle/JVM log, and (from Task 1 onward) reading back the
  rendered composable tree isn't possible either — I relied on successful
  compilation + logic review + unit-testable pieces. Worth having a sighted
  person confirm visually before the real workshop; I'm flagging this
  explicitly so the visual-polish claims below aren't over-trusted.

---

## Task 0 — Run it and tour the project

🐛 **`verifySetup` doesn't pre-download what the Web target actually needs.**
The task doc and README both say `./gradlew verifySetup` "downloads every
dependency ... so nothing has to download on the day," and explicitly tells
people to run it on good wifi *before* the workshop. But `verifySetup` (see
root `build.gradle.kts`) only depends on `compileKotlinWasmJs` for the web
target — compiling Kotlin down to `.wasm`. It does **not** run
`wasmJsBrowserDevelopmentRun`, which is the command Task 0 (and the README)
tells participants to actually use to see the app in a browser. That task
triggers a **separate**, first-time Yarn/webpack-dev-server npm install
(`kotlinWasmNpmInstall`, `kotlinWasmToolingSetup`, plus first-run webpack
compilation) that is not covered by `verifySetup` at all. In my run this
install alone took several minutes over a normal connection; on the
"conference wifi and Gradle are not friends" network this doc keeps warning
about, this is exactly the kind of thing that will stall someone during the
live Task 0/2 web demo. **Suggested fix:** either add
`wasmJsBrowserDevelopmentRun` (or at least the npm/yarn setup tasks) to
`verifySetup`'s dependency list, or explicitly warn in SETUP.md / task-0.md
that the *first* `wasmJsBrowserDevelopmentRun` run needs its own one-time
download separate from `verifySetup`.

⚠️ **"Run on Android from the IDE" has no CLI equivalent documented.** Task 0
step 2 and the README's target table both point Android exclusively at
"Android Studio / IntelliJ + emulator." There's no mention anywhere in
task-0.md or SETUP.md of the command-line equivalent
(`./gradlew :composeApp:installDebug` + `adb shell am start`, or just
`assembleDebug` to prove it builds) for anyone who wants to sanity check
Android without opening an IDE, e.g. on CI or from a terminal-only setup.
Given the whole task series is otherwise so command-line-friendly (every
other target has a `./gradlew` one-liner), this is a small inconsistency —
not wrong, just less self-service than the rest of the doc.

💡 The `local.properties` / `sdk.dir` note in SETUP.md §3 is genuinely helpful
and accurate — following it (`echo "sdk.dir=$HOME/Library/Android/sdk" >
local.properties`) was all that was needed to get
`compileDebugKotlinAndroid` working from the command line once an SDK was
present. Good, no notes.

✅ `./gradlew :composeApp:run` (Desktop) launches cleanly with no exceptions;
`compileKotlinJvm`, `compileKotlinWasmJs`, `compileDebugKotlinAndroid` all
build clean out of the box on a fresh checkout. `verifySetup` itself
succeeds once `local.properties` is in place (fails loudly and clearly
otherwise — good error message, points at the right fix).

---

## Task 1 — `SessionCard`

🐛🐛 **The `@Preview` import is never shown, and the obvious/Android-instinct
import silently breaks non-JVM/non-Android targets.** Neither `task-1.md` nor
the "Previews" slide show an `import` line for `@Preview` — both just show
the annotated function. There are (at least) two candidate imports on the
classpath once you wire up the starter's dependencies:

- `androidx.compose.ui.tooling.preview.Preview` — what every Android/Jetpack
  Compose developer types from muscle memory, and what most IDEs will offer
  first since it's on the JVM/Desktop and Android classpaths (transitively,
  via other Compose dependencies).
- `org.jetbrains.compose.ui.tooling.preview.Preview` — the one actually
  wired up as a *common* (all-targets) klib by the starter's
  `implementation(compose.components.uiToolingPreview)` line in
  `composeApp/build.gradle.kts`, and the one `checkpoint-1` actually uses.

I wrote `SessionCard.kt` with the first import. It compiled fine for
`compileKotlinJvm` — no error, no warning, looked completely done. It was
only when I additionally compiled `compileKotlinIosSimulatorArm64` (to check
the file across all targets, since this is a multiplatform workshop) that it
failed with `Unresolved reference 'tooling'` / `Unresolved reference
'Preview'`. Unzipping the resolved `iosSimulatorArm64` klib confirmed why:
the artifact this project actually depends on only publishes the annotation
under `org.jetbrains.compose.ui.tooling.preview` for non-Android/non-desktop
targets — `androidx.compose.ui.tooling.preview.Preview` isn't reachable from
common code on iOS/wasm with the dependencies as currently declared.

To make it more confusing: once you *do* use the correct
`org.jetbrains.compose.ui.tooling.preview.Preview` import, the compiler prints
a **deprecation warning on every target** telling you to switch to
`androidx.compose.ui.tooling.preview.Preview` instead ("Use
androidx.compose.ui.tooling.preview.Preview from
org.jetbrains.compose.ui:ui-tooling-preview module instead") — i.e. the
compiler actively nudges you toward the import that breaks the iOS/wasm
build with this project's current dependency wiring. Following the warning
is the trap.

**Why this matters for the workshop:** Task 1 only asks people to build and
preview the card — nobody is required to compile for iOS at that point, and
Windows/Linux attendees can't anyway. This means a room full of people could
walk away from Task 1 with a `SessionCard.kt` that looks 100% correct (it
compiles, Desktop/Android previews presumably render — I could not visually
confirm previews from the CLI, only compilation), passes every visual check
in the task doc, and **silently fails to build for iOS** — the one target
most likely to only get checked once, live, on a Mac, later in the day. Given
this is exactly the kind of "N-1 targets" trap the slides spend Block 3
teaching (`expect`/`actual`, "a library covers N-1 of your targets"), it's a
shame the very first task plants an unrelated version of that same trap by
accident.

**Suggested fix:** show the import explicitly in task-1.md's Hint 3 (and
ideally the slide code block too), and/or add a one-line callout: "IDE
autocomplete may offer `androidx.compose.ui.tooling.preview.Preview` first —
use `org.jetbrains.compose.ui.tooling.preview.Preview` instead, the only one
that's common to all four targets with this project's dependencies." Longer
term, this whole ambiguity is a version-catalog/Gradle-DSL problem
(`compose.components.uiToolingPreview` vs. whatever accessor would pull the
androidx-multiplatform artifact in) that's a bit out of scope for
participants to fix themselves, but is worth the workshop authors resolving
upstream so the deprecation warning stops pointing at a dead end.

💡 Everything else in Task 1 matched the reference closely once the import
was fixed: building `SessionCard` from Hint 3's shape, extracting
`FormatBadge` and a favorite `IconButton`, and wiring `sampleSession` into two
`@Preview`s all worked exactly as described and matched `checkpoint-1`'s
structure (my format-badge text and the reference's differ cosmetically —
"Lightning" vs "Lightning talk" — a non-issue, the task doc doesn't mandate
exact copy).

💡 Small, harmless ambiguity: task-1.md step 3 and Hint 2 don't say whether
`FormatBadge`/`FavoriteIconButton` should be separate files or private
composables inside `SessionCard.kt`. Hint 3 says "as their own small
composables (as in `checkpoint-1`) is the cleaner version — extract them
once the card works," which is enough guidance, just flagging that a
newcomer might wonder whether they're doing it "wrong" by inlining them
first. No change needed, this is intentionally left as taste.

---

## Task 2 — Program list

⚠️ **The "empty day shows `EmptyState`" done-when item can't actually be
triggered with the real, provided data — worth a line in the doc.** Task
2's "Done when" list and the slide's Task 2 recap both include an empty-day
check ("When a day has no sessions, show the provided `EmptyState`" /
"Empty day? Use `EmptyState`"). I built the screen exactly as the hints
describe (`days.firstOrNull { it.date == selectedDay }?.slots.orEmpty()`,
falling back to `EmptyState` when that's empty) and then went looking for a
day to actually see it on. There isn't one: `toConferenceDays()`
(`model/Schedule.kt`) only ever produces a `ConferenceDay` for a date that
already has at least one session grouped into it — `dates` is built from
`byDate.keys` (dates with scheduled sessions) plus, optionally, a
`tbaDate` that is only added when there's at least one unscheduled session
to put in it. So **every `ConferenceDay` the function returns is
structurally guaranteed to have a non-empty `slots` list.** I confirmed
against the actual bundled `program.json`: all 156 sessions have a non-null
`startSlot`, spread across exactly three dates (2026-09-01/02/03), so there
is no "Time TBA" day either. With the screen built exactly as instructed,
the only way the `EmptyState` branch runs is a transient state that can't
happen in this simple (non-filtered) Task 2 screen — `selectedDay` pointing
at a date that isn't in `days`, which never occurs since both are derived
together. Net effect: this checklist item is currently unverifiable by
clicking around the finished Task 2 app with the real data; you'd have to
temporarily hack in a fake empty day to see it fire at all. Not a bug in
the code — the `EmptyState` wiring is correct and will matter once Task 4
adds search/filters that *can* legitimately produce zero results for a
day — but as a Task 2 acceptance criterion on its own it's checking
something the task's own data model can't produce yet. Worth either
dropping it from Task 2's checklist (and introducing it properly once
filters exist in Task 4) or adding a one-line aside acknowledging you can't
actually see it fire until then.

✅ Everything else matched the reference exactly in shape: `SessionList`
(`LazyColumn` + `stickyHeader` + stable `key`s), `TimeSlotHeader` (including
the `semantics { heading() }` accessibility detail from the slide, which
the task doc itself doesn't mention — only the slide's "Modifiers" section
does, so it's easy to miss if someone only reads the task doc and skips that
slide), and `DayTabRow` built on `PrimaryTabRow` all worked first try and
compiled clean across JVM/Android/wasm/iOS. The Desktop app ran with day
tabs and sticky headers with no runtime exceptions.

---
