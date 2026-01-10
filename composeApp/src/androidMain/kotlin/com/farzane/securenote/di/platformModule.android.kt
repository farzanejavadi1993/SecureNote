package com.farzane.securenote.di

import androidx.room.RoomDatabase
import com.farzane.securenote.domain.manager.AndroidKeyValueStorage
import com.farzane.securenote.data.local.database.AppDatabase
import com.farzane.securenote.domain.repository.AndroidNoteExporter
import com.farzane.securenote.domain.manager.KeyValueStorage
import com.farzane.securenote.domain.repository.NoteExporter
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext

actual val platformModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        getDatabaseBuilder(androidContext())
    }
    single<NoteExporter> { AndroidNoteExporter(androidContext()) }
    single<KeyValueStorage> { AndroidKeyValueStorage(androidContext()) }
}

