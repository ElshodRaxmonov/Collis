package com.example.collis.presentation.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.collis.data.local.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Splash Screen ViewModel
 *
 * Checks authentication and onboarding status to determine the first
 * screen the user should see. Shows the splash for a minimum duration
 * to ensure branding visibility.
 *
 * NAVIGATION LOGIC:
 * 1. Not onboarded -> Onboarding
 * 2. Not logged in -> Login
 * 3. Logged in -> Student Home
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    /** All possible splash outcomes */
    sealed class SplashState {
        data object Loading : SplashState()
        data object NavigateToOnboarding : SplashState()
        data object NavigateToLogin : SplashState()
        data object NavigateToStudentHome : SplashState()
    }

    /**
     * UI State Flow
     */
    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state.asStateFlow()

    /**
     * Minimum splash duration ensures branding is visible.
     * 1.5 seconds is a good balance between visibility and responsiveness.
     */
    private val MINIMUM_SPLASH_DURATION = 1500L

    init {
        checkAuthenticationStatus()
    }

    /**
     * Checks onboarding, login, and user type in parallel with a minimum
     * splash duration timer, then navigates to the appropriate screen.
     */
    private fun checkAuthenticationStatus() {
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()

            // Gather all stored preferences in one shot
            combine(
                preferencesManager.isOnboardingCompleted,
                preferencesManager.isLoggedIn,
                preferencesManager.userType
            ) { onboardingCompleted, isLoggedIn, _ ->
                Pair(onboardingCompleted, isLoggedIn)
            }
                .first()
                .let { (onboardingCompleted, isLoggedIn) ->

                    // Determine destination based on stored state
                    val destination = when {
                        !onboardingCompleted -> SplashState.NavigateToOnboarding
                        !isLoggedIn -> SplashState.NavigateToLogin
                        else -> SplashState.NavigateToStudentHome
                    }

                    // Wait for minimum splash duration before navigating
                    val elapsedTime = System.currentTimeMillis() - startTime
                    val remainingTime = (MINIMUM_SPLASH_DURATION - elapsedTime).coerceAtLeast(0)
                    if (remainingTime > 0) {
                        delay(remainingTime)
                    }

                    _state.value = destination
                }
        }
    }
}