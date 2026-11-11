package com.quecomemo.app.data.remote.ai

import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig
import com.quecomemo.app.BuildConfig
import com.quecomemo.app.domain.repository.AssistantRepository
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Gemini via Firebase AI Logic: la API key la administra Firebase, no viaja en el APK.
 * Requiere habilitar "AI Logic" (Gemini Developer API) en la consola de Firebase.
 */
class GeminiAssistantRepository @Inject constructor() : AssistantRepository {
    private val model: GenerativeModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = BuildConfig.GEMINI_MODEL,
            generationConfig = generationConfig {
                temperature = 0.6f
                maxOutputTokens = 600
            }
        )
    }

    override suspend fun generateReply(prompt: String): Result<String> {
        return try {
            val text = model.generateContent(prompt).text?.trim()
            if (text.isNullOrEmpty()) {
                Result.failure(AssistantUnavailableException("La IA no devolvio respuesta."))
            } else {
                Result.success(text)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: IOException) {
            Result.failure(AssistantUnavailableException("Sin conexion a internet.", error))
        } catch (error: Exception) {
            Result.failure(AssistantUnavailableException("El asistente no esta disponible ahora.", error))
        }
    }
}

class AssistantUnavailableException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
