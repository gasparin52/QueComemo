package com.quecomemo.app.presentation.assistant

import com.quecomemo.app.domain.model.AssistantMessageAuthor
import com.quecomemo.app.domain.repository.AssistantRepository
import com.quecomemo.app.domain.usecase.AskRecipeAssistantUseCase
import com.quecomemo.app.testutil.MainDispatcherRule
import com.quecomemo.app.testutil.testRecipe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AssistantViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<AssistantRepository>()
    private val viewModel = AssistantViewModel(AskRecipeAssistantUseCase(repository))
    private val recipe = testRecipe(id = "tarta", name = "Tarta de acelga")

    @Test
    fun `no permite enviar sin receta o con texto vacio`() {
        viewModel.updateInput("hola")
        assertFalse(viewModel.uiState.value.canSend)

        viewModel.startConversation(recipe)
        viewModel.updateInput("   ")
        assertFalse(viewModel.uiState.value.canSend)

        viewModel.updateInput("hola")
        assertTrue(viewModel.uiState.value.canSend)
    }

    @Test
    fun `enviar agrega la pregunta y la respuesta de la IA`() {
        coEvery { repository.generateReply(any()) } returns Result.success("Usa masa de hojaldre.")
        viewModel.startConversation(recipe)

        viewModel.updateInput("que masa uso?")
        viewModel.sendMessage()

        val state = viewModel.uiState.value
        assertEquals(listOf("que masa uso?", "Usa masa de hojaldre."), state.messages.map { it.text })
        assertEquals(
            listOf(AssistantMessageAuthor.USER, AssistantMessageAuthor.ASSISTANT),
            state.messages.map { it.author }
        )
        assertEquals("", state.input)
        assertFalse(state.isSending)
        assertNull(state.errorMessage)
    }

    @Test
    fun `si falla muestra error y reintentar vuelve a preguntar`() {
        coEvery { repository.generateReply(any()) } returnsMany listOf(
            Result.failure(IllegalStateException("Sin conexion a internet.")),
            Result.success("Ahora si.")
        )
        viewModel.startConversation(recipe)
        viewModel.updateInput("cuanto horno?")
        viewModel.sendMessage()

        val failed = viewModel.uiState.value
        assertEquals("Sin conexion a internet.", failed.errorMessage)
        assertEquals("cuanto horno?", failed.pendingRetryQuestion)
        assertEquals(1, failed.messages.size)

        viewModel.retry()

        val recovered = viewModel.uiState.value
        assertNull(recovered.errorMessage)
        assertEquals(listOf("cuanto horno?", "Ahora si."), recovered.messages.map { it.text })
        coVerify(exactly = 2) { repository.generateReply(any()) }
    }

    @Test
    fun `cambiar de receta reinicia la conversacion pero la misma receta la conserva`() {
        coEvery { repository.generateReply(any()) } returns Result.success("ok")
        viewModel.startConversation(recipe)
        viewModel.updateInput("hola")
        viewModel.sendMessage()

        viewModel.startConversation(recipe)
        assertEquals(2, viewModel.uiState.value.messages.size)

        viewModel.startConversation(testRecipe(id = "otra"))
        assertTrue(viewModel.uiState.value.messages.isEmpty())
        assertEquals("otra", viewModel.uiState.value.recipe?.id)
    }
}
