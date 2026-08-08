package com.mattschoe.apptemplate.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Every Room entity in the app lives in this one file — it makes the schema
 * readable at a glance and keeps `AppDatabase`'s `entities = [...]` list honest.
 */

@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long
)
