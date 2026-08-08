package com.mattschoe.apptemplate.data.local

import androidx.room.TypeConverter

/**
 * Room type converters. Nothing interesting in here yet — the template ships it
 * because every project grows one, and `@TypeConverters` is already wired onto
 * [AppDatabase] so adding the first real converter is a one-line change.
 */
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString(SEPARATOR)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split(SEPARATOR)

    private companion object {
        /** Unit separator (U+001F) — will not collide with user-entered text. */
        const val SEPARATOR = "\u001F"
    }
}
