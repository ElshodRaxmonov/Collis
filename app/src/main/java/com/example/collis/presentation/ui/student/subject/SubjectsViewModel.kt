package com.example.collis.presentation.ui.student.subject

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collis.data.network.NetworkResult
import com.example.collis.domain.model.LessonModel
import com.example.collis.domain.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class SubjectsViewModel @Inject constructor(
    private val lessonRepository: LessonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SubjectsUiState>(SubjectsUiState.Loading)
    val uiState: StateFlow<SubjectsUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentWeekStart = MutableStateFlow(
        LocalDate.now().with(DayOfWeek.MONDAY)
    )

    init {
        loadWeekLessons()
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        updateSelectedDateLessons()
    }

    fun previousWeek() {
        _currentWeekStart.update { it.minusWeeks(1) }
        _selectedDate.update { it.minusWeeks(1) }
        loadWeekLessons()
    }

    fun nextWeek() {
        _currentWeekStart.update { it.plusWeeks(1) }
        _selectedDate.update { it.plusWeeks(1) }
        loadWeekLessons()
    }

    fun goToToday() {
        _currentWeekStart.value = LocalDate.now().with(DayOfWeek.MONDAY)
        _selectedDate.value = LocalDate.now()
        loadWeekLessons()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                lessonRepository.refreshLessons()
            } catch (_: Exception) { }
            loadWeekLessons()
            _isRefreshing.value = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadWeekLessons() {
        viewModelScope.launch {
            val weekStart = _currentWeekStart.value
            val weekEnd = weekStart.plusDays(6)

            lessonRepository.getLessonsForDateRange(weekStart, weekEnd).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.value = SubjectsUiState.Loading
                    }
                    is NetworkResult.Success -> {
                        val lessons = result.data ?: emptyList()
                        val weekLessons = lessons.groupBy { it.date }

                        val allSubjects = lessons
                            .groupBy { it.courseCode }
                            .map { (code, courseLessons) ->
                                val first = courseLessons.first()
                                SubjectInfo(
                                    courseCode = code,
                                    courseTitle = first.courseTitle,
                                    lecturerName = first.lecturerName,
                                    totalLessons = courseLessons.size
                                )
                            }
                            .sortedBy { it.courseCode }

                        val selectedDateLessons = weekLessons[_selectedDate.value]
                            ?.sortedBy { it.startTime }
                            ?: emptyList()

                        _uiState.value = SubjectsUiState.Success(
                            selectedDateLessons = selectedDateLessons,
                            weekLessons = weekLessons,
                            selectedDate = _selectedDate.value,
                            currentWeekStart = weekStart,
                            allSubjects = allSubjects
                        )
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = SubjectsUiState.Error(
                            result.message ?: "Failed to load schedule"
                        )
                    }
                }
            }
        }
    }

    private fun updateSelectedDateLessons() {
        val current = _uiState.value
        if (current is SubjectsUiState.Success) {
            val selectedDateLessons = current.weekLessons[_selectedDate.value]
                ?.sortedBy { it.startTime }
                ?: emptyList()

            _uiState.value = current.copy(
                selectedDateLessons = selectedDateLessons,
                selectedDate = _selectedDate.value
            )
        }
    }
}
