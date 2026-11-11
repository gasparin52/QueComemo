package com.quecomemo.app.domain.repository

interface AssistantRepository {
    suspend fun generateReply(prompt: String): Result<String>
}
