package com.quecomemo.app.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quecomemo.app.domain.model.StartDestination
import com.quecomemo.app.domain.usecase.GetStartDestinationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getStartDestinationUseCase: GetStartDestinationUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = when (getStartDestinationUseCase()) {
                StartDestination.HOME -> SplashUiState.NavigateToHome
                StartDestination.LOGIN -> SplashUiState.NavigateToLogin
            }
        }
    }
}
