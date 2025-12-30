package com.farzane.securenote.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.farzane.securenote.data.local.database.AppDatabase
import org.koin.dsl.module
import java.io.File

val jvmModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        val dbFile = File(System.getProperty("java.io.tmpdir"), "my_room.db")
        Room.databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath,
        )
    }
}
