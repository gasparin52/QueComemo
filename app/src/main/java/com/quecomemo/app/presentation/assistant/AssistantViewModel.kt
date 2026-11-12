package com.quecomemo.app.presentation.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quecomemo.app.domain.model.AssistantMessage
import com.quecomemo.app.domain.model.AssistantMessageAuthor
import com.quecomemo.app.domain.model.Recipe
import com.quecomemo.app.domain.usecase.AskRecipeAssistantUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val askRecipeAssistantUseCase: AskRecipeAssistantUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    fun startConversation(recipe: Recipe) {
        if (_uiState.value.recipe?.id == recipe.id) return
        _uiState.value = AssistantUiState(recipe = recipe)
    }

    fun updateInput(value: String) {
        _uiState.value = _uiState.value.copy(input = value)
    }

    fun sendMessage() {
        val current = _uiState.value
        if (!current.canSend) return
        val question = current.input.trim()
        _uiState.value = current.copy(
            messages = current.messages + AssistantMessage(
                text = question,
                author = AssistantMessageAuthor.USER,
                sentAtMillis = System.currentTimeMillis()
            ),
            input = ""
        )
        requestReply(question)
    }

    fun retry() {
        val question = _uiState.value.pendingRetryQuestion ?: return
        if (_uiState.value.isSending) return
        requestReply(question)
    }

    private fun requestReply(question: String) {
        val recipe = _uiState.value.recipe ?: return
        // La ultima entrada del historial es la propia pregunta; se manda aparte.
        val history = _uiState.value.messages.dropLast(1)
        _uiState.value = _uiState.value.copy(
            isSending = true,
            errorMessage = null,
            pendingRetryQuestion = null
        )

        viewModelScope.launch {
            askRecipeAssistantUseCase(recipe, history, question)
                .onSuccess { reply ->
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        messages = _uiState.value.messages + AssistantMessage(
                            text = reply,
                            author = AssistantMessageAuthor.ASSISTANT,
                            sentAtMillis = System.currentTimeMillis()
                        )
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        errorMessage = error.message ?: "No pudimos responder.",
                        pendingRetryQuestion = question
                    )
                }
        }
    }
}
