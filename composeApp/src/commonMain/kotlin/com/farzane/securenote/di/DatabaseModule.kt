package com.farzane.securenote.di

import androidx.room.RoomDatabase
import com.farzane.securenote.data.local.database.AppDatabase
import org.koin.core.module.Module
expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>
expect val platformModule: Module
