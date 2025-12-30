package com.farzane.securenote.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.farzane.securenote.data.local.database.AppDatabase
import com.farzane.securenote.data.repository.NoteRepositoryImpl
import com.farzane.securenote.domain.repository.NoteRepository
import com.farzane.securenote.domain.usecase.AddNoteUseCase
import com.farzane.securenote.domain.usecase.DeleteNoteUseCase
import com.farzane.securenote.domain.usecase.GetNoteByIdUseCase
import com.farzane.securenote.domain.usecase.GetNotesUseCase
import kotlinx.coroutines.Dispatchers
import org.koin.core.KoinApplication
import org.koin.dsl.module

val appModule = module {

    single { get<AppDatabase>().noteDao() }

    single<NoteRepository> { NoteRepositoryImpl(get()) }

    factory { GetNotesUseCase(get()) }
    factory { AddNoteUseCase(get()) }
    factory { DeleteNoteUseCase(get()) }
    factory { GetNoteByIdUseCase(get()) }

    single<AppDatabase> {
        val builder = get<RoomDatabase.Builder<AppDatabase>>()
        builder
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

}

fun initKoin(config: (KoinApplication.() -> Unit)? = null) {
    org.koin.core.context.startKoin {
        config?.invoke(this)
        modules(appModule, platformModule)
    }
}
