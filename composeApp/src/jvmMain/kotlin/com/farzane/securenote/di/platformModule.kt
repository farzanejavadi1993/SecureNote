package com.farzane.securenote.di
import androidx.room.Room
import androidx.room.RoomDatabase
import com.farzane.securenote.data.DesktopKeyValueStorage
import com.farzane.securenote.data.local.database.AppDatabase
import com.farzane.securenote.data.repository.DesktopNoteExporter
import com.farzane.securenote.domain.manager.KeyValueStorage
import com.farzane.securenote.domain.repository.NoteExporter
import org.koin.dsl.module
import java.io.File

actual val platformModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        val dbFile = File(System.getProperty("java.io.tmpdir"), "note_database.db")
        Room.databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath,
        )
    }

    single<NoteExporter> { DesktopNoteExporter() }

    single<KeyValueStorage> { DesktopKeyValueStorage() }

}
