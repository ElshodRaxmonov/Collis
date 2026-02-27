package com.example.collis.domain.usecase.lesson

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.collis.data.network.NetworkResult
import com.example.collis.domain.model.LessonModel
import com.example.collis.domain.repository.LessonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Get Today's Lessons Use Case
 *
 * BUSINESS LOGIC:
 * - Fetch today's lessons
 * - Filter out past lessons (optional)
 * - Sort by time
 * - Mark live lessons
 */
class GetTodayLessonsUseCase @Inject constructor(
    private val lessonRepository: LessonRepository
) {
    /**
     * Get today's lessons with live status
     *
     * ENHANCEMENTS:
     * - Automatically marks lessons as "LIVE" if happening now
     * - Filters out completed lessons (optional)
     * - Sorts by start time
     *
     * @param includeCompleted Whether to include past lessons
     * @return Flow of lessons for today
     */
    @RequiresApi(Build.VERSION_CODES.O)
    operator fun invoke(
        includeCompleted: Boolean = true
    ): Flow<NetworkResult<List<LessonModel>>> {
        return lessonRepository.getTodayLessons().map { result ->
            when (result) {
                is NetworkResult.Success -> {
                    val lessons = result.data ?: emptyList()

                    /**
                     * Filter completed lessons if requested
                     */
                    val filteredLessons = if (!includeCompleted) {
                        lessons.filter { it.getStatus() != com.example.collis.domain.model.LessonStatus.COMPLETED }
                    } else {
                        lessons
                    }

                    /**
                     * Sort by time
                     * Live lessons first, then upcoming, then completed
                     */
                    val sortedLessons = filteredLessons.sortedWith(
                        compareBy(
                            { lesson ->
                                when (lesson.getStatus()) {
                                    com.example.collis.domain.model.LessonStatus.LIVE -> 0
                                    com.example.collis.domain.model.LessonStatus.UPCOMING -> 1
                                    com.example.collis.domain.model.LessonStatus.COMPLETED -> 2
                                    com.example.collis.domain.model.LessonStatus.CANCELLED -> 3
                                }
                            },
                            { it.startTime }
                        )
                    )

                    NetworkResult.Success(sortedLessons)
                }

                is NetworkResult.Error -> result
                is NetworkResult.Loading -> result
            }
        }
    }
}