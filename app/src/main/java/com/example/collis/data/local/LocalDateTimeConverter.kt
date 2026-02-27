package com.example.collis.data.local

import androidx.room.TypeConverter
import java.time.LocalDateTime

import java.time.format.DateTimeFormatter

/**
 * Room Type Converter for LocalDateTime
 *
 * WHY WE NEED THIS:
 * - Room doesn't natively support Java 8 time classes
 * - We need to convert LocalDateTime ↔ String for database storage
 * - This ensures proper date/time handling across the app
 *
 * LEARNING POINT:
 * @TypeConverter tells Room how to convert complex types to/from database-compatible types
 * Room only supports primitives (Int, String, etc.) natively
 */
class LocalDateTimeConverter {

    // ISO_DATE_TIME format: "2026-02-13T14:30:00"
    // This is the industry standard for date-time storage
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    /**
     * Converts LocalDateTime to String for database storage
     * Called automatically by Room when saving data
     */
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(formatter)
    }

    /**
     * Converts String from database back to LocalDateTime
     * Called automatically by Room when reading data
     */
    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let {
            LocalDateTime.parse(it, formatter)
        }
    }
}

/**
 * ALTERNATIVE APPROACH (using timestamps):
 * Some developers prefer storing as Long (Unix timestamp)
 *
 * Pros: More compact, easier to query by range
 * Cons: Less readable in database, timezone complexity
 *
 * Our approach (ISO String):
 * Pros: Human-readable, timezone-aware, easier debugging
 * Cons: Slightly more storage space
 */