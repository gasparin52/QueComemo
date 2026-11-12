package com.quecomemo.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quecomemo.app.domain.usecase.SignInWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            val result = signInWithGoogleUseCase(idToken)
            _uiState.value = result.fold(
                onSuccess = { AuthUiState(isAuthenticated = true) },
                onFailure = {
                    AuthUiState(
                        errorMessage = it.message ?: "No se pudo iniciar sesion con Google."
                    )
                }
            )
        }
    }

    fun showExternalError(message: String) {
        _uiState.value = AuthUiState(errorMessage = message)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun consumeAuthenticationSuccess() {
        _uiState.value = _uiState.value.copy(
            isAuthenticated = false,
            isLoading = false
        )
    }
}
