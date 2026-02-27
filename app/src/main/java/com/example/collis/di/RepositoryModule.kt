package com.example.collis.di



import com.example.collis.data.repository.AuthRepositoryImpl
import com.example.collis.data.repository.LessonRepositoryImpl
import com.example.collis.data.repository.NotificationRepositoryImpl
import com.example.collis.data.repository.TaskRepositoryImpl
import com.example.collis.domain.repository.AuthRepository
import com.example.collis.domain.repository.LessonRepository
import com.example.collis.domain.repository.NotificationRepository
import com.example.collis.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository Module
 *
 * DEPENDENCY INJECTION:
 * Binds repository interfaces to implementations
 *
 * @Binds vs @Provides:
 * - @Binds: For interface → implementation binding (preferred, more efficient)
 * - @Provides: For complex object creation
 *
 * LEARNING: @Binds generates less code than @Provides
 * Use @Binds whenever possible for better performance
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Bind Auth Repository
     *
     * Hilt will automatically inject AuthRepositoryImpl
     * when AuthRepository is requested
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    /**
     * Bind Lesson Repository
     */
    @Binds
    @Singleton
    abstract fun bindLessonRepository(
        lessonRepositoryImpl: LessonRepositoryImpl
    ): LessonRepository

    /**
     * Bind Notification Repository
     */
    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    /**
     * Bind Task Repository (local Room database)
     */
    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository
}

/**
 * DEPENDENCY GRAPH:
 *
 * ViewModel → Repository Interface → Repository Implementation → API Service + DAO
 *
 * Example: LoginViewModel
 * 1. ViewModel requests AuthRepository
 * 2. Hilt provides AuthRepositoryImpl
 * 3. AuthRepositoryImpl needs CollisApiService + PreferencesManager
 * 4. Hilt provides both from AppModule
 * 5. All dependencies wired automatically!
 */