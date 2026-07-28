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

## Executive summary — fix these before the workshop, in priority order

1. **(Task 5, severity: high) The real hosted `program.json` URL breaks the
   exact Ktor `ContentNegotiation` setup the task doc and slide tell you to
   write, for every participant, on every platform, every time.**
   `raw.githubusercontent.com` serves `.json` as `Content-Type: text/plain`,
   which Ktor's default JSON converter matching doesn't accept, so
   `client.get(PROGRAM_URL).body<ProgramDto>()` throws
   `NoTransformationFoundException` on a real 200 response. The bundled
   fallback silently absorbs the failure, so **the app looks correct while
   the network path never actually works** — and Task 5's own verification
   step ("turn wifi off, relaunch → see the difference") can't show a
   difference, because online and offline behave identically. `checkpoint-5`
   has the identical bug. Fix: register `ContentType.Text.Plain` on the JSON
   converter (or fetch as text and decode manually) — see the Task 5 section
   for the verified fix and root-cause detail.
2. **(Task 1, severity: medium-high) The `@Preview` import is never shown,
   and the instinctive choice silently breaks iOS/wasm.** `androidx.compose
   .ui.tooling.preview.Preview` compiles fine on JVM/Android but doesn't
   exist on iOS/wasm with this project's dependencies; the working import
   (`org.jetbrains.compose.ui.tooling.preview.Preview`) triggers a deprecation
   warning that recommends switching back to the broken one. This can ship
   silently through Task 1–3 since nobody is required to compile for iOS
   until later. See the Task 1 section for the fix and suggested doc change.
3. **(Task 0, severity: medium) `verifySetup` doesn't cover the Web target's
   actual first-run cost.** It only compiles Kotlin/Wasm; the *real* Task 0
   command (`wasmJsBrowserDevelopmentRun`) triggers a separate, several-minute
   first-time Yarn/webpack install that `verifySetup` never touches — exactly
   the kind of thing the "do this on good wifi at home" instructions exist to
   prevent.
4. **(Task 5, severity: low) Slide claims `PROGRAM_URL` "is in the starter"
   — it isn't**; it's part of what Task 5 has you write. Combined with #1,
   presenting this slide as-is tells the room the fetch should just work when
   it won't.
5. **(Tasks 2–4, severity: low) A handful of small doc/prose gaps** that cost
   time without blocking progress: Task 2's "empty day" checklist item can't
   actually be triggered with the real data; Task 3 never specifies what the
   detail pane should contain; Task 4's `ProgramUiState` field list omits
   `activeLanguages`, which its own `ToggleLanguage` intent needs; a
   `LoadingState` composable appears in a slide as if pre-existing but is
   never asked for in any task doc. Full detail in each task's section below.

Task 6 had no issues — see its section for why.

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

## Task 3 — Adaptive layout

⚠️ **The task doc never says what to put in the "detail" pane, and that's
a bigger gap than it looks.** Step 4/5 tell you to build `ListDetailLayout`
and track a `selectedSessionId`, "so the chosen card highlights in the
two-pane view" — but nothing in task-3.md's steps or hints says what the
`detail` slot itself should render. `checkpoint-3` answers this by already
building a fairly complete `SessionDetailContent` (title, format badge,
language tag, time/room, favorite button, abstract, speakers, keywords,
workshop prerequisites — a good ~100 lines across two files) — content that
reads much more like Task 4/detail-screen material than "adaptive layout."
Since the task doc doesn't ask for any of that, a participant following
task-3.md literally would reasonably put a one-line placeholder in the
detail slot (I did — just the session title) and still satisfy every stated
"Done when" item (`selected` highlighting, two panes at ≥840dp). That's a
perfectly valid reading of the instructions, but it means their screen will
look noticeably less finished than `checkpoint-3`'s when they compare
afterward, for reasons the task doc itself never flagged. Worth either (a)
explicitly scoping the detail pane to "a placeholder — the real detail
screen is Task 4" in task-3.md, or (b) moving a one-line version of
`SessionDetailContent` into Task 3's steps if the intent really is to have
it fleshed out this early.

⚠️ **Live-resize verification (the task's own step 6 and the slide's "the
demo you'll be showing your team next week") could not be confirmed in this
environment, and it's worth knowing why before assuming it's fine
elsewhere.** I couldn't drag a window with a mouse from the CLI, so I
patched a temporary debug build of `main.kt` that (a) opened the Desktop
window at an explicit width via `WindowState(width = …)`, driven by a
`-Dtest.windowWidthDp` system property, and (b) logged
`currentWindowWidth()` on every change. Requesting **500 dp** logged
`Compact` and then, within the same run, immediately re-logged `Expanded`;
requesting **700 dp** did the exact same Compact-then-Expanded flip. Since
two different requested widths converged on the same final state, this
reads like the window manager in this sandboxed/automated environment
silently resizing or refusing to honor the small requested frame — not a
bug in `currentWindowWidth()` or `AdaptiveScaffold`, which are verbatim the
documented `material3-adaptive` pattern from the slide and match
`checkpoint-3` exactly. I'm flagging it rather than either hiding it or
claiming I verified live-resize behavior: **I could not,** in this sandbox,
confirm the breakpoint transition visually or via real window-drag
interaction — only that the code compiles identically to the reference on
all four targets and that `currentWindowWidth()` does read *some* value
without crashing. A sighted run through this exact step, on a normal
desktop session, is worth doing before trusting that this task's "resize
and watch it adapt" payoff moment works as smoothly live as the slides
promise — I have no reason to think it won't (the API is JetBrains' own,
widely used), I simply couldn't personally confirm it here.

✅ `TopDestination`, `WindowWidth`/`currentWindowWidth()`, `AdaptiveScaffold`
(bar↔rail at the 600 dp breakpoint) and `ListDetailLayout` (weighted
0.42/0.58 row at the 840 dp breakpoint) all matched Hint 2/3's shapes
closely and compiled clean on JVM/Android/wasm/iOS. The `selected` highlight
plumbed through `SessionList` → `SessionCard` (added the actual
`CardDefaults.cardColors(containerColor = surfaceContainerHighest)` visual
effect in `SessionCard`, since Task 1 only declared the `selected`
parameter without using it — task-1.md flags this as optional/stretch, and
Task 3 is correctly where it becomes load-bearing, exactly as task-3.md's
closing note says it would).

---

## Task 4 — `ProgramViewModel` + navigation

⚠️ **Step 1's `ProgramUiState` field list is missing `activeLanguages`,
which step 2's own `ToggleLanguage` intent needs to do anything.** Task 4,
step 1 says the state should hold "`sessions`, `favoriteIds`, `selectedDay`,
`activeFormats`, `searchQuery`, `selectedSessionId`" — no `activeLanguages`.
Step 2, right below it, asks for a `ToggleLanguage(val language: String)`
intent alongside `ToggleFormat`. Implemented against step 1's field list
literally, `ToggleLanguage` has nowhere to write its result — there's an
`activeFormats: Set<Format>` to toggle into, but no equivalent
`activeLanguages: Set<String>`. I added the field myself (it's the obvious,
only-sensible fix, symmetric with `activeFormats`), and confirmed
`checkpoint-4`'s real `ProgramUiState.kt` does include `activeLanguages` —
so the reference implementation needs it too, it's just missing from step
1's prose. Small thing, but it's exactly the kind of gap that costs a
participant real time mid-task wondering whether they misread something.
Same file's checklist also omits `isLoading`/`loadFailed`/`isOffline`/
`offlineBannerDismissed`, which is more defensible since those are
legitimately Task 5 concerns — but `activeLanguages` isn't forward-looking,
it's needed for a Task 4 intent to do its Task 4 job.

⚠️ **The slide "UI as functions" shows a `LoadingState` composable as if
it's existing/provided app code — it isn't, and no task doc ever asks you
to build it.** The Block 1 slide titled "UI as functions" presents
`LoadingState` (`…/ui/components/States.kt`) as "a complete, real screen
state from the app" to teach what a composable is. It reads as a tour of
existing code, the same way the "Modifiers" and "Lazy lists" slides quote
real files from the starter. But `LoadingState` is not in the README's
"provided for you" list, not in any of the six task docs' steps or hints,
and I could only find it by checking `checkpoint-4`'s `App.kt`, which calls
it for the "session route resolved but the program is still loading" edge
case in the navigation `when`. A participant who tries to build that same
edge case after seeing the slide (or after diffing against `checkpoint-4`,
which the README explicitly encourages: "compare against `checkpoint-4`")
would hit an unresolved reference with no task doc pointing at where to
add it. Minor since the task docs' own hints get by without it (a plain
`if (session != null) … else …`, no loading branch, is what I built and
it's entirely adequate for Task 4's stated scope) — but worth either adding
`LoadingState` to the provided-components list in the README/task-0.md, or
softening the slide's framing so it doesn't read as "already in your
starter." Checking further, `checkpoint-4`'s `ProgramScreen.kt` (a file I
hadn't compared against until writing this up) leans on it even more than
`App.kt` does, and also imports a sibling `ErrorState` component that has
the identical problem — not in the README's provided list, not mentioned in
any task doc, only discoverable by reading `checkpoint-4` source directly.
Same fix applies to both.

✅ Implemented `ProgramUiState` (with the `activeLanguages` fix),
`ProgramIntent`, and `ProgramViewModel` (private `MutableStateFlow` / public
`StateFlow`, exhaustive `when` in `onIntent`) matching Hint 2's shape.
Rewrote `ProgramScreen` as a pure function of `(state, onIntent, expanded,
onOpenSession)`, wired `App()` with `viewModel { ProgramViewModel() }`,
`collectAsState()`, a real `NavHost` with the `session/{sessionId}` route
reading the argument via `entry.arguments?.read { getStringOrNull(...) }`,
and navigation from `SessionCard.onClick`. Compiled clean on all four
targets and ran on Desktop with no exceptions — search/filter logic lives
as pure functions on `ProgramUiState` per the task's instruction, not in
composables. I deliberately did **not** replicate `checkpoint-4`'s extra
polish (the `BackHandler` pane-vs-route sync, live-resize migration between
pane and pushed route, `ScheduleScreen`/`InfoScreen`, `OfflineBanner`) since
none of it is asked for by task-4.md's steps or "Done when" list — that
material belongs to the slide's "Detail: pane or route?" walkthrough and to
Tasks 5/6, not to what a participant following task-4.md alone would be
expected to produce.

---

## Task 5 — Fetch the program

🐛🐛🐛 **The real, hosted `program.json` URL breaks the exact `ContentNegotiation`
setup task-5.md and the slide instruct you to write — every participant on
every platform will hit this, and it defeats the task's own "online vs
offline" demo.** `raw.githubusercontent.com` serves `.json` files with
`Content-Type: text/plain; charset=utf-8` (confirmed independently with
`curl -sSD - -o /dev/null https://raw.githubusercontent.com/mortenaa/javazone-cmp-workshop/main/program.json`
→ `content-type: text/plain; charset=utf-8`, plus `x-content-type-options:
nosniff` — this is GitHub raw's standard behavior for every file it serves,
not a fluke of this particular repo). Ktor's `ContentNegotiation` +
`kotlinx.serialization` `json()` plugin, exactly as task-5.md's Hint 2
instructs (`install(ContentNegotiation) { json(ProgramJson) }`), only
auto-deserializes a response whose `Content-Type` matches
`application/json` by default. Point `client.get(PROGRAM_URL).body<ProgramDto>()`
— literally `task-5.md`'s own Hint 2 code — at the real URL and every single
fetch throws:

```
io.ktor.client.call.NoTransformationFoundException: Expected response body of the
type 'class no.javazone.app.data.ProgramDto' but was 'class
io.ktor.utils.io.SourceByteReadChannel'. In response from
`https://raw.githubusercontent.com/mortenaa/javazone-cmp-workshop/main/program.json`.
Response status `200`. Response header `ContentType: text/plain; charset=utf-8`.
```

I verified this two ways: (1) a scratch JVM test calling
`ProgramApi().fetchProgram()` directly against the real URL — reproduced the
exact exception above on the first try; (2) `curl` against the same URL,
independent of any Kotlin/Ktor code, confirming the `text/plain` header is
real and not a client-side artifact. **This means Task 5's own verification
step — "turn wifi off, relaunch → real data with offline banner" — cannot
show what it claims to.** With the code built exactly as instructed, the
network fetch fails *every time*, wifi on or off, so a participant doing
that test would see the offline banner and bundled data in both cases and
have no way to tell "it worked online" from "it's broken" — the entire
point of the exercise (see the network path actually being live) silently
never happens. Every one of the 156 sessions still loads (via the bundled
fallback), so **nothing in the UI signals that anything is wrong** — the app
looks and behaves exactly as the task doc says it should, which makes this
particularly nasty to notice without either reading Ktor exception logs or
independently checking the response headers, as I did here.

I confirmed the reference solution has the identical bug: `checkpoint-5`'s
`ProgramApi.kt` is byte-for-byte the same `install(ContentNegotiation) {
json(ProgramJson) }` with no content-type override. My best guess for how
this shipped unnoticed: the workshop's own automated tests (per the Block 4
slide) exercise the repository via Ktor's `MockEngine`, which returns
whatever content type the test tells it to (`application/json` in the
slide's own test example) — so the test suite would never touch the real
server's actual headers, and a manual smoke test against the live URL is
easy to skip once the bundled-fallback path makes the app look fine
regardless.

**Fix I applied** (and verified resolves it — same scratch test then
reported success and `sessions=156` via the live fetch): register the
`text/plain` content type against the same JSON converter, since the
`ContentNegotiation` DSL supports registering one converter for multiple
content types:

```kotlin
install(ContentNegotiation) {
    json(ProgramJson)
    // raw.githubusercontent.com serves .json as text/plain — accept that too.
    json(ProgramJson, contentType = ContentType.Text.Plain)
}
```

An equally valid alternative (more defensive against any future header
change, at the cost of bypassing `ContentNegotiation` for this one call) is
fetching as text and decoding manually, the same pattern `bundledProgram()`
already uses: `ProgramJson.decodeFromString(ProgramDto.serializer(),
client.get(PROGRAM_URL).bodyAsText())`. Either belongs in task-5.md itself —
right now nothing in the task doc, its hints, or the slide's "Real-world
JSON is messy" section (which only covers `ignoreUnknownKeys`/`isLenient` —
JSON-syntax leniency, not HTTP content-type negotiation) prepares a
participant for this, and it isn't something Task 5's own troubleshooting
notes ("watch for: forgetting the timeout... swallowing
CancellationException") mention either.

⚠️ **The slide claims `PROGRAM_URL` "is in the starter" — it is not.** The
Task 5 slide's speaker notes say: "Shorter task, mostly assembling pieces
you've now seen. The URL constant is in the starter." I grepped the entire
repository at `main` (the actual starter state) for `PROGRAM_URL` and
`raw.githubusercontent` and found nothing — the constant first appears in
`checkpoint-5`'s `ProgramApi.kt`, i.e. it's part of what Task 5 has you
write, not something already provided. task-5.md itself gets this right (it
gives you the literal URL string to use in your own `const val`), so the
inconsistency is specifically in the slide's speaker notes, not the task
doc. Small thing on its own, but combined with the content-type bug above,
anyone stage-presenting from the slide as written would tell a room full of
people "the constant's already there" and then everyone's fetch fails
anyway — worth fixing both while in there.

✅ Once the content-type fix was in place: `ProgramApi`, `ProgramRepository`
(network → cache → bundled, `CancellationException` rethrown before the
broad catch, exactly as Hint 1's "two gotchas" warns), the `ProgramUiState`
additions (`isLoading`/`loadFailed`/`isOffline`/`offlineBannerDismissed`/
`showOfflineBanner`), `ProgramViewModel`'s `Retry`/`DismissOfflineBanner`
handling, and `OfflineBanner` all matched the task doc and `checkpoint-5`
closely and compiled clean on all four targets. Verified both branches
directly against `ProgramRepository` (bypassing the UI, in the spirit of the
Block 4 testing slide): a real fetch against the live URL now succeeds
(`sessions=156`, `isOffline=false`), and a `MockEngine` configured to throw
falls back to bundled data with `isOffline=true` — the fallback chain
itself is correct.

---

## Task 6 — Persist favorites

✅ This task was the smoothest of the six — everything in task-6.md's steps
and hints matched the reference and worked first try. `Favorites.sq` /
`ProgramCache.sq` under `commonMain/sqldelight/...`, the generated
`favoritesQueries`/`programCacheQueries`, `FavoritesStore`/`ProgramCache` as
plain interfaces with `expect fun createFavoritesStore()`/
`createProgramCache()`, `SqlFavoritesStore`/`SqlProgramCache` wrapping
`asFlow().mapToList(...)`, and the four `actual`s (JDBC on JVM,
`AndroidSqliteDriver` + the starter's already-provided `JavaZoneApp.appContext`
on Android, `NativeSqliteDriver` on iOS, `localStorage` on wasmJs) all
compiled clean on every target on the first attempt — including iOS, which
I could only compile (no simulator here), and wasmJs, where there genuinely
is no SQLite driver, exactly as the task doc says.

✅ **Verified actual disk persistence, not just "it compiles."** Rather than
trust that `SqlFavoritesStore` writes through, I toggled a favorite via a
scratch JVM test calling `createFavoritesStore().setFavorite(id, true)`,
then read the database file back with a **completely separate process** —
the `sqlite3` CLI, not any code sharing memory with the test — pointed at
`~/.javazone2026/javazone.db`: `SELECT * FROM favorite;` returned the row.
That's about as close to "kill the app and relaunch" as I could get without
a GUI to click a star and restart a window, and it confirms the SQLite path
genuinely persists to disk rather than just satisfying the in-process
`Flow`. I also ran the full `:composeApp:run` afterward and confirmed the
same `javazone.db` file gets created on ordinary app startup with no
exceptions.

✅ The one thing I couldn't verify end-to-end is exactly the thing the task
doc itself flags as out of reach for some attendees: real Android/iOS
persistence via `AndroidSqliteDriver`/`NativeSqliteDriver` on an actual
device or simulator (no emulator image, no full Xcode here) — the README's
"Windows/Linux note" about skipping the iOS `actual` describes a version of
this same limitation, just for a different reason (no Mac vs. no
emulator/simulator here). This is squarely an environment gap on my end, not
a doc issue — the code is identical to `checkpoint-6` and compiles for both
targets.

💡 No notes on unclear or wrong instructions for this task — task-6.md's
Hint 2/3 code blocks are complete and accurate enough to build the whole
feature from without needing to consult `checkpoint-6` at all, which was
a nice contrast after Tasks 4 and 5.

