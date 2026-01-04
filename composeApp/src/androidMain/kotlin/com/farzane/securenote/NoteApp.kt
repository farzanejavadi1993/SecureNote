package com.farzane.securenote

import android.app.Application
import com.farzane.securenote.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

/**
 * The custom Application class for the Android app.
 * This is the entry point where we initialize Koin.
 */
class NoteApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Call the shared initKoin function from commonMain.
        initKoin {
            // Provide Android-specific dependencies to Koin.
            // 1. androidLogger: Use Android's Logcat for Koin's internal logging.
            androidLogger()
            // 2. androidContext: This makes the Android Application Context available to your modules.
            androidContext(this@NoteApp)
        }
    }
}