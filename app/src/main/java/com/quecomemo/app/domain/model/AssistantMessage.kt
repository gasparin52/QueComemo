package com.quecomemo.app.domain.model

data class AssistantMessage(
    val text: String,
    val author: AssistantMessageAuthor,
    val sentAtMillis: Long
)

enum class AssistantMessageAuthor {
    USER,
    ASSISTANT
}
