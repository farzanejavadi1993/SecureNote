package com.farzane.securenote.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.farzane.securenote.data.AndroidKeyValueStorage
import com.farzane.securenote.data.local.database.AppDatabase
import com.farzane.securenote.data.repository.AndroidNoteExporter
import com.farzane.securenote.domain.manager.KeyValueStorage
import com.farzane.securenote.domain.repository.NoteExporter
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    throw NotImplementedError("Not used directly on Android in this setup")
}
actual val platformModule = module {
    single<AppDatabase> {
        val context = androidContext()
        val dbFile = context.getDatabasePath("note_database.db")
        Room.databaseBuilder<AppDatabase>(
            context = context,
            name = dbFile.absolutePath
        ).build()
    }
    single<NoteExporter> { AndroidNoteExporter(androidContext()) }
    single<KeyValueStorage> { AndroidKeyValueStorage(androidContext()) }
}

