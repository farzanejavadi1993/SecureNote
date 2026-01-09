package com.farzane.securenote.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.farzane.securenote.data.local.database.AppDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "note_database.db")
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath,
    )
}