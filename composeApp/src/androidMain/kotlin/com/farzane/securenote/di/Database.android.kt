package com.farzane.securenote.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.farzane.securenote.data.local.database.AppDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("note_database.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

