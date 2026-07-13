# JavaZone 2026 — Compose Multiplatform Workshop

Build a real conference app for **JavaZone 2026** — the very schedule you are
attending — running from a single Kotlin codebase on **Android, iOS, Desktop and
the Web** with [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/).

Over six hands-on tasks you write the UI, the state layer and the data layer
yourself. By the end you have a complete, offline-capable app and a template you
can reuse for your own projects.

This is the starter repository for the 4-hour workshop
*"Practical Multiplatform Development with Compose and Kotlin"* by
Morten Nygaard Åsnes and Kristian Berg.

## Before the workshop (do this at home, on good wifi)

Conference wifi and Gradle are not friends. Set up and pre-download **everything**
while you still have a fast connection:

1. Read **[docs/SETUP.md](docs/SETUP.md)** and install the tools for your OS.
2. Clone this repo and run the setup check — it downloads every dependency and
   the JDK 21 toolchain so nothing has to download on the day:

   ```bash
   ./gradlew verifySetup
   ```

   Green ✅ across the board means you are ready.

## Quick start

```bash
git clone https://github.com/mortenaa/javazone-cmp-workshop.git
cd javazone-cmp-workshop
./gradlew :composeApp:run        # launches the Desktop app
```

If you see **"JavaZone 2026 — N sessions loaded"**, your environment works and
you are ready for Task 0.

## Running each target

| Target | Command / how |
| :--- | :--- |
| **Desktop** (JVM) | `./gradlew :composeApp:run` |
| **Web** (Wasm) | `./gradlew :composeApp:wasmJsBrowserDevelopmentRun` |
| **Android** | Open in Android Studio / IntelliJ and run the `composeApp` config on an emulator or device |
| **iOS** (Mac + Xcode only) | Open `iosApp/iosApp.xcodeproj` in Xcode and run, or use the KMP run configuration in the IDE |

Desktop and Web are the fastest feedback loop during the workshop — start there.

## The tasks

Work through them in order. Each has a full write-up under
[`docs/tasks/`](docs/tasks/) with progressive hints.

| # | Task | Checkpoint branch |
| :--- | :--- | :--- |
| 0 | Run the starter, tour the project | `main` |
| 1 | Build a `SessionCard` (+ `@Preview`) | `checkpoint-1` |
| 2 | The program list (day tabs, time-slot headers) | `checkpoint-2` |
| 3 | Adaptive layout (phone ↔ desktop/web) | `checkpoint-3` |
| 4 | State & navigation (ViewModel, favorites) | `checkpoint-4` |
| 5 | Fetch the program over the network (Ktor) | `checkpoint-5` |
| 6 | Persist favorites offline (SQLDelight) | `checkpoint-6` |

**Fell behind?** Every task has a checkpoint branch with the finished result.
Jump to the next one and keep up with the group — you can revisit later:

```bash
git checkout checkpoint-3
```

`checkpoint-6` is the complete app. See [docs/tasks/stretch.md](docs/tasks/stretch.md)
for extra challenges if you finish early.

## What's provided vs. what you build

**Provided for you** (so you can focus on Compose, not boilerplate):

- The full Gradle setup — all four targets, the version catalog with every
  dependency pre-declared, the JDK 21 toolchain wiring, and the iOS Xcode wrapper.
- The domain model (`model/`) and the API DTOs + mapping (`data/ProgramDto.kt`).
- The Material 3 theme with the JavaZone colours (`ui/theme/Theme.kt`).
- A few small shared pieces you'll reach for: `SampleData` (for previews),
  `EmptyState`, `StarOutlineIcon`, and date/format helpers.
- The conference program as bundled data (`composeResources/files/program.json`)
  so the app works fully offline from day one.

**You build**: the `SessionCard`, the program list, the adaptive scaffold, the
`ProgramViewModel` and navigation, the Ktor data source, and the SQLDelight
persistence layer — i.e. the app.

## The data

The bundled `program.json` (and the copy at the repo root, served over the
network in Task 5) is the JavaZone 2026 program. It is generated from the public
JavaZone program feed; the generator lives in the workshop's planning repo
(`tools/enrich-program.py`). If you regenerate it, keep the repo-root copy and
`composeApp/src/commonMain/composeResources/files/program.json` identical.

## License

[MIT](LICENSE).
