import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.sqldelight) apply false
}

// ---------------------------------------------------------------------------
// ./gradlew verifySetup
//
// Run this at home, on good wifi, BEFORE the workshop. It checks your
// environment and — more importantly — downloads every dependency and the
// JDK 21 toolchain, so nothing has to download over conference wifi.
// ---------------------------------------------------------------------------

val androidSdkDir: String? = run {
    val localProps = File(rootDir, "local.properties")
    val fromFile = if (localProps.exists()) {
        Properties().apply { localProps.inputStream().use { load(it) } }.getProperty("sdk.dir")
    } else null
    fromFile ?: System.getenv("ANDROID_HOME") ?: run {
        val default = File(System.getProperty("user.home"), "Library/Android/sdk")
        if (default.isDirectory) default.absolutePath else null
    }
}
val isMac = System.getProperty("os.name").contains("Mac", ignoreCase = true)

tasks.register("verifySetup") {
    group = "workshop"
    description = "Checks your environment and pre-downloads everything the workshop needs."

    // Compiling these targets resolves all common/jvm/wasm dependencies and
    // triggers the JDK 21 toolchain download (via the foojay resolver).
    dependsOn(":composeApp:compileKotlinJvm", ":composeApp:compileKotlinWasmJs", ":composeApp:jvmTest")
    if (androidSdkDir != null) dependsOn(":composeApp:compileDebugKotlinAndroid")
    if (isMac) dependsOn(":composeApp:compileKotlinIosSimulatorArm64")

    doLast {
        fun check(ok: Boolean, label: String, hint: String = "") =
            println((if (ok) "  ✅ " else "  ❌ ") + label + if (!ok && hint.isNotEmpty()) "\n       -> $hint" else "")

        println()
        println("JavaZone workshop setup check")
        println("-----------------------------")
        check(true, "JDK for Gradle: ${System.getProperty("java.version")} (toolchain 21 auto-provisioned)")
        check(
            androidSdkDir != null,
            if (androidSdkDir != null) "Android SDK: $androidSdkDir" else "Android SDK not found",
            "Install Android Studio (or the SDK) and/or add sdk.dir to local.properties — see docs/SETUP.md",
        )
        if (isMac) {
            val xcode = try {
                val process = ProcessBuilder("xcode-select", "-p").start()
                process.waitFor() == 0
            } catch (e: Exception) {
                false
            }
            check(xcode, "Xcode command line tools", "Run: xcode-select --install (full Xcode needed to run the iOS app)")
        } else {
            println("  ℹ️  Not a Mac: the iOS target is unavailable here — Android, Desktop and Web are plenty.")
        }
        check(true, "Dependencies + JDK 21 toolchain downloaded (this build proved it)")
        println()
        println("If everything above is green you are ready. See docs/SETUP.md for fixes.")
        println()
    }
}
