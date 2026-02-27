package com.example.collis.presentation.ui.student.subject

import com.example.collis.domain.model.LessonModel

sealed class SubjectsUiState {
    data object Loading : SubjectsUiState()

    data class Success(
        val selectedDateLessons: List<LessonModel>,
        val weekLessons: Map<java.time.LocalDate, List<LessonModel>>,
        val selectedDate: java.time.LocalDate,
        val currentWeekStart: java.time.LocalDate,
        val allSubjects: List<SubjectInfo>
    ) : SubjectsUiState()

    data class Error(val message: String) : SubjectsUiState()
}

data class SubjectInfo(
    val courseCode: String,
    val courseTitle: String,
    val lecturerName: String,
    val totalLessons: Int
)
