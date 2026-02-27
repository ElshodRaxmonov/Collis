package com.example.collis.presentation.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collis.data.local.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Onboarding ViewModel
 *
 * RESPONSIBILITIES:
 * - Manage onboarding page state
 * - Handle skip/next/complete actions
 * - Mark onboarding as completed
 *
 * LEARNING: Onboarding is a one-time flow
 * After completion, user never sees it again
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    /**
     * Complete onboarding
     *
     * Saves flag to DataStore
     * Navigation handled by screen
     */
    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesManager.setOnboardingCompleted()
        }
    }
}