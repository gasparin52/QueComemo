package com.quecomemo.app.presentation.home

import com.quecomemo.app.domain.model.CookingCategoryMode
import com.quecomemo.app.domain.model.CookingEffort
import com.quecomemo.app.domain.model.IngredientMatchMode
import com.quecomemo.app.domain.usecase.GetCurrentUserUseCase
import com.quecomemo.app.domain.usecase.ObserveFavoriteIdsUseCase
import com.quecomemo.app.domain.usecase.ObserveFavoriteRecipesUseCase
import com.quecomemo.app.domain.usecase.ObserveRecipesUseCase
import com.quecomemo.app.domain.usecase.PickRouletteRecipeUseCase
import com.quecomemo.app.domain.usecase.SeedRecipesUseCase
import com.quecomemo.app.domain.usecase.SignOutUseCase
import com.quecomemo.app.domain.usecase.ToggleFavoriteRecipeUseCase
import com.quecomemo.app.testutil.FakeAuthRepository
import com.quecomemo.app.testutil.FakeFavoriteRepository
import com.quecomemo.app.testutil.FakeRecipeRepository
import com.quecomemo.app.testutil.MainDispatcherRule
import com.quecomemo.app.testutil.testRecipe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val recipes = listOf(
        testRecipe(id = "omelette", name = "Omelette simple", totalMinutes = 10, ingredients = listOf("huevos", "sal")),
        testRecipe(id = "tortilla", name = "Tortilla de papa", totalMinutes = 40, ingredients = listOf("papas", "huevos", "cebolla"))
    )
    private val recipeRepository = FakeRecipeRepository(recipes)
    private val authRepository = FakeAuthRepository()

    private fun createViewModel(): HomeViewModel {
        val favoriteRepository = FakeFavoriteRepository(recipeRepository)
        return HomeViewModel(
            getCurrentUserUseCase = GetCurrentUserUseCase(authRepository),
            observeRecipesUseCase = ObserveRecipesUseCase(recipeRepository),
            observeFavoriteRecipesUseCase = ObserveFavoriteRecipesUseCase(favoriteRepository),
            observeFavoriteIdsUseCase = ObserveFavoriteIdsUseCase(favoriteRepository),
            pickRouletteRecipeUseCase = PickRouletteRecipeUseCase(),
            seedRecipesUseCase = SeedRecipesUseCase(recipeRepository),
            signOutUseCase = SignOutUseCase(authRepository),
            toggleFavoriteRecipeUseCase = ToggleFavoriteRecipeUseCase(favoriteRepository)
        )
    }

    @Test
    fun `al iniciar carga el seed y muestra las recetas de Room`() {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(1, recipeRepository.seedCalls)
        assertFalse(state.isLoadingRecipes)
        assertEquals(recipes, state.recipes)
        assertEquals("Ana", state.displayName)
        assertNull(state.errorMessage)
    }

    @Test
    fun `si falla el seed muestra error y reintentar recupera`() {
        recipeRepository.seedError = IllegalStateException("Base rota")
        val viewModel = createViewModel()

        assertEquals("Base rota", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoadingRecipes)

        recipeRepository.seedError = null
        viewModel.retryRecipes()

        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(recipes, viewModel.uiState.value.recipes)
    }

    @Test
    fun `flujo completo de ruleta y vuelta atras paso a paso`() {
        val viewModel = createViewModel()

        viewModel.openRoulette()
        viewModel.selectCookingCategoryMode(CookingCategoryMode.MEAL)
        viewModel.selectEffort(CookingEffort.LOW)
        viewModel.addIngredient(" huevos ")
        viewModel.addIngredient("HUEVOS")
        viewModel.goToSpinStep()
        viewModel.selectIngredientMatchMode(IngredientMatchMode.FLEXIBLE)
        viewModel.spinRoulette()

        val state = viewModel.uiState.value
        assertEquals(RouletteStep.SPIN, state.rouletteStep)
        assertEquals(listOf("huevos"), state.selectedIngredients)
        assertEquals("omelette", state.rouletteResult?.id)
        assertEquals(1, state.rouletteSpinCount)

        val expectedBackSteps = listOf(
            RouletteStep.INGREDIENT_SCOPE,
            RouletteStep.INGREDIENTS,
            RouletteStep.EFFORT,
            RouletteStep.CATEGORY
        )
        expectedBackSteps.forEach { expected ->
            viewModel.navigateBack()
            assertEquals(expected, viewModel.uiState.value.rouletteStep)
        }
        viewModel.navigateBack()
        assertEquals(HomeSection.MENU, viewModel.uiState.value.currentSection)
    }

    @Test
    fun `detalle completo abre y cierra el asistente con atras`() {
        val viewModel = createViewModel()
        viewModel.openFavorites()
        viewModel.openRecipeDetail("tortilla")
        assertNotNull(viewModel.uiState.value.detailRecipe)

        viewModel.openFullRecipeDetail("tortilla")
        viewModel.openAssistant()
        assertTrue(viewModel.uiState.value.isAssistantOpen)

        viewModel.navigateBack()
        assertFalse(viewModel.uiState.value.isAssistantOpen)
        assertEquals(HomeSection.RECIPE_DETAIL, viewModel.uiState.value.currentSection)

        viewModel.navigateBack()
        assertEquals(HomeSection.FAVORITES, viewModel.uiState.value.currentSection)
    }

    @Test
    fun `marcar favorito actualiza ids y lista de favoritos`() {
        val viewModel = createViewModel()

        viewModel.toggleFavorite("tortilla")
        assertEquals(setOf("tortilla"), viewModel.uiState.value.favoriteIds)
        assertEquals(listOf("tortilla"), viewModel.uiState.value.favoriteRecipes.map { it.id })

        viewModel.toggleFavorite("tortilla")
        assertTrue(viewModel.uiState.value.favoriteIds.isEmpty())
    }

    @Test
    fun `ruleta de delivery respeta personas y presupuesto`() {
        val viewModel = createViewModel()
        viewModel.openDeliveryRoulette()
        viewModel.selectDeliveryPeopleMode(DeliveryPeopleMode.SOLO)
        viewModel.selectDeliveryBudget(DeliveryBudget.LOW)

        repeat(10) { viewModel.spinDeliveryRoulette() }

        val state = viewModel.uiState.value
        assertEquals(DeliveryStep.SPIN, state.deliveryStep)
        assertTrue(
            state.deliveryResult in setOf(
                "Empanadas", "Pizza", "Sandwich de milanesa", "Helado", "Papas con cheddar", "Tostados"
            )
        )
        assertEquals(10, state.deliverySpinCount)
    }

    @Test
    fun `sugerencias de ingredientes salen de las recetas locales`() {
        val viewModel = createViewModel()
        viewModel.updateIngredientQuery("ceb")

        assertEquals(listOf("cebolla"), viewModel.ingredientSuggestions())

        viewModel.updateIngredientQuery("c")
        assertTrue(viewModel.ingredientSuggestions().isEmpty())
    }

    @Test
    fun `cerrar sesion limpia el estado y avisa a la UI`() {
        val viewModel = createViewModel()

        viewModel.signOut()

        assertEquals(1, authRepository.signOutCalls)
        assertTrue(viewModel.uiState.value.isSignedOut)
        viewModel.consumeSignedOut()
        assertFalse(viewModel.uiState.value.isSignedOut)
    }
}
