package com.mattschoe.apptemplate.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters

/**
 * Bumping `version` REQUIRES adding a migration — schemas are exported to
 * `shared/schemas/` so the diff is reviewable. Destructive fallback is not
 * configured anywhere on purpose.
 */
@Database(
    entities = [ItemEntity::class],
    version = 1
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}

@Suppress("KotlinNoActualForExpect") // The Room compiler generates the `actual` implementations.
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

internal const val DATABASE_FILE_NAME = "apptemplate.db"
