package no.javazone.app

import android.app.Application
import android.content.Context

/** Holds the application context needed by the Android SQLite driver. */
class JavaZoneApp : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    companion object {
        lateinit var appContext: Context
            private set
    }
}
