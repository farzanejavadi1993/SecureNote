package com.farzane.securenote.di
import androidx.room.RoomDatabase
import com.farzane.securenote.domain.manager.DesktopKeyValueStorage
import com.farzane.securenote.data.local.database.AppDatabase
import com.farzane.securenote.domain.repository.DesktopNoteExporter
import com.farzane.securenote.domain.manager.KeyValueStorage
import com.farzane.securenote.domain.repository.NoteExporter
import org.koin.dsl.module

actual val platformModule = module {
  /*  single<RoomDatabase.Builder<AppDatabase>> {
        val dbFile = File(System.getProperty("java.io.tmpdir"), "note_database.db")
        Room.databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath,
        )
    }*/

    single<RoomDatabase.Builder<AppDatabase>> { getDatabaseBuilder() }

    single<NoteExporter> { DesktopNoteExporter() }

    single<KeyValueStorage> { DesktopKeyValueStorage() }

}
