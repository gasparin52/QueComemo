package com.quecomemo.app.domain.usecase

import com.quecomemo.app.domain.model.AssistantMessage
import com.quecomemo.app.domain.model.AssistantMessageAuthor
import com.quecomemo.app.domain.model.Recipe
import com.quecomemo.app.domain.repository.AssistantRepository
import javax.inject.Inject

class AskRecipeAssistantUseCase @Inject constructor(
    private val assistantRepository: AssistantRepository
) {
    suspend operator fun invoke(
        recipe: Recipe,
        history: List<AssistantMessage>,
        question: String
    ): Result<String> {
        val trimmedQuestion = question.trim()
        if (trimmedQuestion.isEmpty()) {
            return Result.failure(IllegalArgumentException("La pregunta esta vacia."))
        }
        return assistantRepository.generateReply(buildPrompt(recipe, history, trimmedQuestion))
    }

    internal fun buildPrompt(
        recipe: Recipe,
        history: List<AssistantMessage>,
        question: String
    ): String = buildString {
        appendLine("Sos un asistente de cocina amigable.")
        appendLine("Tengo la siguiente receta: ${recipe.name}.")
        appendLine("Ingredientes: ${recipe.ingredients.joinToString(separator = ", ")}.")
        appendLine("Pasos: ${recipe.steps.joinToString(separator = " ")}")

        val recentHistory = history.takeLast(MAX_HISTORY_MESSAGES)
        if (recentHistory.isNotEmpty()) {
            appendLine("Conversacion previa:")
            recentHistory.forEach { message ->
                val author = when (message.author) {
                    AssistantMessageAuthor.USER -> "Usuario"
                    AssistantMessageAuthor.ASSISTANT -> "Asistente"
                }
                appendLine("$author: ${message.text}")
            }
        }

        appendLine("El usuario pregunta: $question")
        append("Respondele de forma clara, breve y en espanol argentino, sin usar formato markdown.")
    }

    private companion object {
        const val MAX_HISTORY_MESSAGES = 6
    }
}
