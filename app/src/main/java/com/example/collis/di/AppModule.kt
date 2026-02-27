package com.example.collis.di

import android.content.Context
import androidx.room.Room
import mr.dev.collis.BuildConfig
import com.example.collis.data.local.CollisDatabase
import com.example.collis.data.local.dao.TaskDao
import com.example.collis.data.local.preferences.PreferencesManager
import com.example.collis.data.remote.api.CollisApiService
import com.example.collis.data.remote.interceptor.AuthInterceptor
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import java.util.concurrent.TimeUnit

/**
 * Dagger Hilt Dependency Injection Module
 *
 * WHAT IS DEPENDENCY INJECTION?
 * Instead of creating dependencies inside classes:
 * ❌ val repository = TaskRepositoryImpl(TaskDao())
 * ✅ constructor(private val repository: TaskRepository)
 *
 * BENEFITS:
 * ✅ Testability: Easy to mock dependencies
 * ✅ Flexibility: Swap implementations easily
 * ✅ Decoupling: Classes don't know about implementation details
 * ✅ Singleton management: Framework handles lifecycle
 *
 * HILT ANNOTATIONS:
 * @Module: Tells Hilt this class provides dependencies
 * @InstallIn: Defines lifecycle (SingletonComponent = app lifetime)
 * @Provides: Method that creates a dependency
 * @Singleton: Only one instance across app
 *
 * LEARNING: Hilt is built on top of Dagger 2
 * Simpler API, designed specifically for Android
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provide Room Database
     *
     * @ApplicationContext: Hilt provides app context
     * @Singleton: Only one database instance
     *
     * Room.databaseBuilder:
     * - Requires Context, Database class, DB name
     * - .fallbackToDestructiveMigration(): Delete & recreate on schema change
     *   (For production, use proper migrations)
     * - .build(): Creates database instance
     */
    @Provides
    @Singleton
    fun provideCollisDatabase(
        @ApplicationContext context: Context
    ): CollisDatabase {
        return Room.databaseBuilder(
            context,
            CollisDatabase::class.java,
            CollisDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // For development
            // For production, implement proper migrations:
            // .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    /**
     * Provide TaskDao
     *
     * PATTERN: Database provides DAO
     * No need for @Singleton - Database is singleton
     */
    @Provides
    fun provideTaskDao(database: CollisDatabase): TaskDao {
        return database.taskDao()
    }

    /**
     * Provide PreferencesManager
     *
     * DataStore for storing user preferences and auth token
     */
    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager(context)
    }

    /**
     * Provide Gson instance for JSON parsing
     */
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    /**
     * Provide OkHttpClient with interceptors
     *
     * INTERCEPTORS EXPLAINED:
     * - Run on every HTTP request/response
     * - Can modify requests (add headers)
     * - Can log network traffic
     * - Can handle authentication
     *
     * HttpLoggingInterceptor:
     * - Logs request/response in Logcat
     * - BODY level: Logs full request/response (use in debug only!)
     * - Helps debugging API issues
     *
     * AuthInterceptor:
     * - Adds "Authorization: Bearer {token}" to every request
     * - Reads token from DataStore
     * - Automatic authentication
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // Must be before logging
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Provide Retrofit instance
     *
     * RETROFIT:
     * - Type-safe HTTP client
     * - Converts API interface to actual network calls
     * - Handles JSON parsing automatically
     *
     * GsonConverterFactory:
     * - Converts JSON ↔ Kotlin data classes
     * - Uses @SerializedName annotations in DTOs
     *
     * BASE_URL from BuildConfig:
     * - Different URLs for debug/release builds
     * - Configured in build.gradle
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provide API Service
     *
     * Retrofit.create():
     * - Generates implementation of API interface
     * - All methods become actual network calls
     * - Type-safe API calls
     */
    @Provides
    @Singleton
    fun provideCollisApiService(retrofit: Retrofit): CollisApiService {
        return retrofit.create(CollisApiService::class.java)
    }

}

/**
 * HOW HILT WIRES EVERYTHING:
 *
 * 1. App starts
 * 2. Hilt creates SingletonComponent
 * 3. Provides Context (from Android)
 * 4. Calls provideCollisDatabase(context) → Database created
 * 5. Calls provideTaskDao(database) → DAO created
 * 6. Calls provideTaskRepository(taskDao) → Repository created
 * 7. ViewModel requests TaskRepository
 * 8. Hilt injects the singleton repository instance
 *
 * DEPENDENCY GRAPH:
 * Context → Database → DAO → Repository → ViewModel
 *
 * All automatic! No manual instantiation needed.
 */
