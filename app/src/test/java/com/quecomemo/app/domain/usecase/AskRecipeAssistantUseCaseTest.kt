package com.quecomemo.app.domain.usecase

import com.quecomemo.app.domain.model.AssistantMessage
import com.quecomemo.app.domain.model.AssistantMessageAuthor
import com.quecomemo.app.domain.repository.AssistantRepository
import com.quecomemo.app.testutil.testRecipe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AskRecipeAssistantUseCaseTest {
    private val repository = mockk<AssistantRepository>()
    private val useCase = AskRecipeAssistantUseCase(repository)
    private val recipe = testRecipe(
        id = "mondongo",
        name = "Mondongo a la espanola",
        ingredients = listOf("mondongo", "chorizo colorado"),
        steps = listOf("Hervir el mondongo.", "Guisar con el chorizo.")
    )

    @Test
    fun `arma el prompt con la receta y la pregunta y devuelve la respuesta`() = runTest {
        val prompt = slot<String>()
        coEvery { repository.generateReply(capture(prompt)) } returns Result.success("Hervilo 1 hora.")

        val result = useCase(recipe, emptyList(), "  como lo ablando?  ")

        assertEquals("Hervilo 1 hora.", result.getOrNull())
        assertTrue(prompt.captured.startsWith("Sos un asistente de cocina amigable."))
        assertTrue(prompt.captured.contains("Tengo la siguiente receta: Mondongo a la espanola."))
        assertTrue(prompt.captured.contains("Ingredientes: mondongo, chorizo colorado."))
        assertTrue(prompt.captured.contains("Pasos: Hervir el mondongo. Guisar con el chorizo."))
        assertTrue(prompt.captured.contains("El usuario pregunta: como lo ablando?\n"))
        assertTrue(prompt.captured.contains("espanol argentino"))
    }

    @Test
    fun `pregunta vacia falla sin llamar a la IA`() = runTest {
        val result = useCase(recipe, emptyList(), "   ")

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.generateReply(any()) }
    }

    @Test
    fun `incluye solo los ultimos mensajes del historial`() {
        val history = (1..10).map { index ->
            AssistantMessage(
                text = "mensaje $index",
                author = if (index % 2 == 0) AssistantMessageAuthor.ASSISTANT else AssistantMessageAuthor.USER,
                sentAtMillis = index.toLong()
            )
        }

        val prompt = useCase.buildPrompt(recipe, history, "y ahora?")

        assertFalse(prompt.contains("mensaje 4\n"))
        assertTrue(prompt.contains("Asistente: mensaje 10"))
        assertTrue(prompt.contains("Usuario: mensaje 5"))
    }

    @Test
    fun `propaga el error del repositorio`() = runTest {
        coEvery { repository.generateReply(any()) } returns Result.failure(IllegalStateException("Sin conexion"))

        val result = useCase(recipe, emptyList(), "hola")

        assertEquals("Sin conexion", result.exceptionOrNull()?.message)
    }
}
