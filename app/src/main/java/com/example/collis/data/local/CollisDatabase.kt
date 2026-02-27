package com.example.collis.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.collis.data.local.dao.TaskDao
import com.example.collis.data.local.entity.TaskEntity

/**
 * Room Database for Collis App
 *
 * Currently handles:
 * - Task management (student-only feature)
 *
 * Future enhancements:
 * - Cache for subjects
 * - Offline announcements
 * - Draft announcements (lecturer)
 */
@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(LocalDateTimeConverter::class)
abstract class CollisDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        const val DATABASE_NAME = "collis_database"
    }
}