package com.farzane.securenote.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.farzane.securenote.data.local.database.AppDatabase
import com.farzane.securenote.data.repository.NoteRepositoryImpl
import com.farzane.securenote.domain.manager.AuthManager
import com.farzane.securenote.domain.repository.NoteRepository
import com.farzane.securenote.domain.usecase.AddNoteUseCase
import com.farzane.securenote.domain.usecase.DeleteNoteUseCase
import com.farzane.securenote.domain.usecase.GetNoteByIdUseCase
import com.farzane.securenote.domain.usecase.GetAllNotesUseCase
import kotlinx.coroutines.Dispatchers
import org.koin.core.KoinApplication
import org.koin.dsl.module

/**
 * This is the main Koin module for the shared `commonMain` code.
 * It defines how to create (provide) the core components of the app,
 * like Use Cases, Repositories, and the Database.
 */

val appModule = module {

    /**
     * Provides the fully built Room database instance.
     * It's a `single` so there's only one database instance for the whole app.
     * It depends on a platform-specific `RoomDatabase.Builder` from `platformModule`.
     */
    single<AppDatabase> {
        val builder = get<RoomDatabase.Builder<AppDatabase>>()
        builder
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    /**
     * Provides the NoteDao (Data Access Object) from the database.
     * Koin gets the `AppDatabase` singleton and calls `.noteDao()` on it.
     */
    single { get<AppDatabase>().noteDao() }

    /**
     * Provides the AuthManager for handling the PIN lock and security logic.
     * It's a `single` to ensure the app lock state and timer are shared globally.
     */
    single { AuthManager(get()) }


    /**
     * Provides the implementation for the NoteRepository.
     * It's a `single` because we usually only need one repository instance.
     * Koin automatically finds and provides the `NoteDao` dependency with `get()`.
     */
    single<NoteRepository> { NoteRepositoryImpl(get()) }


    /**
     * Provides the Use Cases.
     * These are defined as `factory` because they are simple, stateless classes.
     * A new instance will be created every time one is requested.
     */
    factory { GetAllNotesUseCase(get()) }
    factory { AddNoteUseCase(get()) }
    factory { DeleteNoteUseCase(get()) }
    factory { GetNoteByIdUseCase(get()) }



}


/**
 * A helper function to initialize Koin from the platform-specific entry points
 * (e.g., from `MainActivity` on Android).
 */
fun initKoin(config: (KoinApplication.() -> Unit)? = null) {
    org.koin.core.context.startKoin {
        config?.invoke(this)
        modules(appModule, platformModule)
    }
}
