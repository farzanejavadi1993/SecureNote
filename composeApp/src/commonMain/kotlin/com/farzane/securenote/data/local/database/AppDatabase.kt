package com.farzane.securenote.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.farzane.securenote.data.local.dao.NoteDao
import com.farzane.securenote.data.local.entity.NoteEntity

@Database(entities = [NoteEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}