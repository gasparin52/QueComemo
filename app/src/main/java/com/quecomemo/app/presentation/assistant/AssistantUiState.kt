package com.quecomemo.app.presentation.assistant

import com.quecomemo.app.domain.model.AssistantMessage
import com.quecomemo.app.domain.model.Recipe

data class AssistantUiState(
    val recipe: Recipe? = null,
    val messages: List<AssistantMessage> = emptyList(),
    val input: String = "",
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val pendingRetryQuestion: String? = null
) {
    val canSend: Boolean
        get() = recipe != null && input.isNotBlank() && !isSending
}
