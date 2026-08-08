package com.mattschoe.apptemplate.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

/** Everything lands under the per-user app-data dir so desktop runs don't litter CWD. */
private fun appDataDir(): File {
    val os = System.getProperty("os.name").lowercase()
    val base = when {
        os.contains("win") -> System.getenv("APPDATA")
            ?: System.getProperty("user.home")
        os.contains("mac") -> System.getProperty("user.home") + "/Library/Application Support"
        else -> System.getenv("XDG_DATA_HOME")
            ?: (System.getProperty("user.home") + "/.local/share")
    }
    return File(base, "AppTemplate").also { it.mkdirs() }
}

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(appDataDir(), DATABASE_FILE_NAME)
    return Room.databaseBuilder<AppDatabase>(name = dbFile.absolutePath)
}

fun createDataStore(): DataStore<Preferences> = createDataStore(
    producePath = { File(appDataDir(), DATA_STORE_FILE_NAME).absolutePath }
)
