# Setup — do this at home, on good wifi

> **Conference wifi and Gradle are not friends.** Install everything and run
> `./gradlew verifySetup` **before** you arrive. That one command downloads every
> dependency *and* the JDK 21 toolchain, so nothing has to download on the day.

This workshop builds one Kotlin app that runs on **Android, iOS, Desktop (JVM)
and the Web (Wasm)**. You do not need every target working — you need **at least
Desktop plus one of Android/Web**. Desktop is the fastest feedback loop and what
we use for most of the day, so get that going first.

---

## 1. What you need, per operating system

| Target | macOS | Windows | Linux |
| :--- | :---: | :---: | :---: |
| **Desktop (JVM)** | ✅ | ✅ | ✅ |
| **Android** | ✅ | ✅ | ✅ |
| **Web (Wasm)** | ✅ | ✅ | ✅ |
| **iOS** | ✅ | ❌ (needs a Mac) | ❌ (needs a Mac) |

iOS requires a Mac with Xcode — Apple's tooling only runs on macOS. If you are on
Windows or Linux this is expected and completely fine: **Android + Desktop + Web
covers every task**, and we demo iOS from the stage.

### Required for everyone

- **A JDK** — any recent version, just to launch Gradle (your IDE bundles one).
  You do *not* need to install JDK 21 by hand: the build provisions the JDK 21
  toolchain automatically (part of what `verifySetup` triggers). See the note
  below on *why 21 specifically* and the launcher-vs-toolchain distinction.
- **IntelliJ IDEA** (2025.1+) **or Android Studio** (latest stable), with the
  **Kotlin Multiplatform plugin** installed (Settings → Plugins → Marketplace →
  search "Kotlin Multiplatform").
- **Android SDK** + one **emulator** image (comes with Android Studio).
- **Git**, and a modern **browser** (Chrome, Firefox or Safari) for the Web target.

### Mac only (for iOS)

- **Xcode** (latest stable) from the App Store.
- **Xcode command line tools**: `xcode-select --install`.
- An iOS **Simulator** (installed with Xcode).

---

## 2. Install steps

1. **Make sure a JDK is available to launch Gradle** — *any* recent version.
   IntelliJ IDEA and Android Studio bundle one, so you can usually skip a manual
   install. If you'd rather have one on the command line, install JDK 21 (macOS:
   `brew install openjdk@21`; Windows/Linux: [Adoptium Temurin 21](https://adoptium.net/))
   and confirm with `java -version`. Installing *exactly* 21 is optional — this JDK
   only starts Gradle; see the note below on the toolchain that builds the app.
2. **Install IntelliJ IDEA or Android Studio**, then add the **Kotlin
   Multiplatform plugin** and restart the IDE.
3. **Install the Android SDK.** Easiest via Android Studio:
   *Settings → Languages & Frameworks → Android SDK* → install the latest SDK
   platform + build tools, and accept the licenses.
4. **Create one Android emulator**: *Device Manager → Create device* → pick any
   recent phone + a recent system image.
5. **(Mac) Install Xcode** and run `xcode-select --install`.
6. **Clone the repo and verify** (see below).

The JDK from step 1 only *launches* Gradle. The **JDK 21 toolchain** that
actually compiles and runs the app is a separate thing: the build downloads it
automatically via the [foojay resolver](https://github.com/gradle/foojay-toolchains)
if you don't already have a JDK 21 — you never install or manage that toolchain
by hand.

---

## 3. Clone, verify, run

```bash
git clone https://github.com/mortenaa/javazone-cmp-workshop.git
cd javazone-cmp-workshop

# The important one — run at home, on good wifi.
# Downloads all dependencies + the JDK 21 toolchain and prints a ✅/❌ summary.
./gradlew verifySetup
```

Then smoke-test the Desktop app:

```bash
./gradlew :composeApp:run
```

You should see a window: **"JavaZone 2026 — N sessions loaded"**. That means your
environment works and you are ready for Task 0.

**Running the other targets:**

| Target | How |
| :--- | :--- |
| **Desktop** | `./gradlew :composeApp:run` |
| **Web** | `./gradlew :composeApp:wasmJsBrowserDevelopmentRun` (opens a browser) |
| **Android** | Open the project in the IDE, pick the `composeApp` run config, choose your emulator, run |
| **iOS** (Mac) | Open `iosApp/iosApp.xcodeproj` in Xcode and run, or use the IDE's iOS run configuration |

> **`sdk.dir` note:** the IDE writes a `local.properties` with your Android SDK
> path automatically. If a command-line build can't find the SDK, create
> `local.properties` in the repo root with `sdk.dir=/path/to/Android/sdk`. This
> file is git-ignored and must never be committed.

---

## 4. Troubleshooting

These are the real issues we hit building and dry-running this project.

### "JBR is required to run this build" / a JBR linkage error on startup

Compose Hot Reload runs the desktop app on the **JetBrains Runtime**, and the
whole build is pinned to a **JDK 21 toolchain** (`jvmToolchain(21)` in
`composeApp/build.gradle.kts`) for exactly this reason. **Newer JDKs (22+) cause
a JBR linkage error.** You should not hit this because the build auto-provisions
JDK 21 via the foojay resolver — but if you have forced a newer JDK via
`org.gradle.java.home` or a `JAVA_HOME` override, remove that override and let
the toolchain do its job. Running `./gradlew verifySetup` once (on good wifi)
downloads the correct toolchain.

### Android build fails with an AGP / KMP compatibility error

This project uses **Android Gradle Plugin 8.13.x on purpose**. AGP 9 cannot build
an Android *application* together with a Kotlin Multiplatform module in a single
Gradle module — the combination we use here. Do not bump AGP. All versions are
pinned in `gradle/libs.versions.toml` and frozen for the workshop; please don't
change them.

### Gradle sync is slow / times out on conference wifi

Don't fight it on the day. Run `./gradlew verifySetup` at home, on a fast
connection — it resolves and caches **everything**, including the JDK 21
toolchain. If you're already at the venue and stuck, pair up with someone whose
sync finished, or check out a checkpoint branch and keep moving with the group.

### Android SDK not found / licenses not accepted

- Install the SDK via Android Studio (Section 2).
- Accept licenses: `sdkmanager --licenses` (or Android Studio does it for you).
- Ensure `local.properties` has the right `sdk.dir`, or set `ANDROID_HOME`.

### iOS: "xcrun: command line tools not found"

Run `xcode-select --install`, then open Xcode once to finish its first-run
setup. You need the *full* Xcode (not just the command line tools) to actually
run the app on a simulator.

### Diagnose a broken KMP setup with `kdoctor`

[`kdoctor`](https://github.com/Kotlin/kdoctor) checks your whole Kotlin
Multiplatform environment (JDK, Android SDK, Xcode, CocoaPods) and tells you
exactly what's missing:

```bash
brew install kdoctor   # macOS
kdoctor
```

### Still stuck?

Check out the checkpoint branch for the task you're on (e.g.
`git checkout checkpoint-3`) so a broken environment doesn't block you, and grab
one of us during the hands-on blocks — that's what we're walking the room for.
