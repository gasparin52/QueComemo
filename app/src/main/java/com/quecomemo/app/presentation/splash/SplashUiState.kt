package com.quecomemo.app.presentation.splash

sealed interface SplashUiState {
    data object Loading : SplashUiState
    data object NavigateToLogin : SplashUiState
    data object NavigateToHome : SplashUiState
}
