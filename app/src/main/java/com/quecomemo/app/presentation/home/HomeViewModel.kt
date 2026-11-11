package com.quecomemo.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    getCurrentUserUseCase: GetCurrentUserUseCase,
    private val observeRecipesUseCase: ObserveRecipesUseCase,
    private val observeFavoriteRecipesUseCase: ObserveFavoriteRecipesUseCase,
    private val observeFavoriteIdsUseCase: ObserveFavoriteIdsUseCase,
    private val pickRouletteRecipeUseCase: PickRouletteRecipeUseCase,
    private val seedRecipesUseCase: SeedRecipesUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val toggleFavoriteRecipeUseCase: ToggleFavoriteRecipeUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        getCurrentUserUseCase().let { user ->
            HomeUiState(
                displayName = user?.displayName,
                email = user?.email
            )
        }
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var recipesJob: Job? = null

    init {
        loadRecipes()
    }

    fun retryRecipes() {
        loadRecipes()
    }

    fun openMenu() {
        _uiState.value = _uiState.value.copy(
            currentSection = HomeSection.MENU,
            fullDetailRecipe = null,
            detailRecipe = null
        )
    }

    fun openRoulette() {
        _uiState.value = _uiState.value.copy(
            currentSection = HomeSection.ROULETTE,
            rouletteStep = RouletteStep.CATEGORY,
            cookingCategoryMode = CookingCategoryMode.MEAL,
            rouletteResult = null,
            rouletteSpinCount = 0,
            detailRecipe = null,
            fullDetailRecipe = null
        )
    }

    fun openDeliveryRoulette() {
        _uiState.value = _uiState.value.copy(
            currentSection = HomeSection.DELIVERY,
            deliveryStep = DeliveryStep.PEOPLE,
            deliveryPeopleMode = null,
            deliveryBudget = null,
            deliveryResult = null,
            deliverySpinCount = 0,
            detailRecipe = null,
            fullDetailRecipe = null
        )
    }

    fun openFavorites() {
        _uiState.value = _uiState.value.copy(
            currentSection = HomeSection.FAVORITES,
            detailRecipe = null,
            fullDetailRecipe = null
        )
    }

    fun openPopular() {
        _uiState.value = _uiState.value.copy(
            currentSection = HomeSection.POPULAR,
            detailRecipe = null,
            fullDetailRecipe = null
        )
    }

    fun navigateBack() {
        val current = _uiState.value
        when {
            current.isAssistantOpen -> closeAssistant()
            current.detailRecipe != null -> closeRecipeDetail()
            current.currentSection == HomeSection.RECIPE_DETAIL -> {
                _uiState.value = current.copy(
                    currentSection = current.detailReturnSection,
                    fullDetailRecipe = null,
                    isAssistantOpen = false
                )
            }
            current.currentSection == HomeSection.FAVORITES -> openMenu()
            current.currentSection == HomeSection.POPULAR -> openMenu()
            current.currentSection == HomeSection.DELIVERY && current.deliveryStep == DeliveryStep.SPIN -> {
                _uiState.value = current.copy(
                    deliveryStep = DeliveryStep.BUDGET,
                    deliveryResult = null
                )
            }
            current.currentSection == HomeSection.DELIVERY && current.deliveryStep == DeliveryStep.BUDGET -> {
                _uiState.value = current.copy(
                    deliveryStep = DeliveryStep.PEOPLE,
                    deliveryResult = null
                )
            }
            current.currentSection == HomeSection.DELIVERY -> openMenu()
            current.currentSection == HomeSection.ROULETTE && current.rouletteStep == RouletteStep.SPIN -> {
                _uiState.value = current.copy(
                    rouletteStep = RouletteStep.INGREDIENT_SCOPE,
                    rouletteResult = null
                )
            }
            current.currentSection == HomeSection.ROULETTE && current.rouletteStep == RouletteStep.INGREDIENT_SCOPE -> {
                _uiState.value = current.copy(
                    rouletteStep = RouletteStep.INGREDIENTS,
                    rouletteResult = null
                )
            }
            current.currentSection == HomeSection.ROULETTE && current.rouletteStep == RouletteStep.INGREDIENTS -> {
                _uiState.value = current.copy(
                    rouletteStep = RouletteStep.EFFORT,
                    rouletteResult = null
                )
            }
            current.currentSection == HomeSection.ROULETTE && current.rouletteStep == RouletteStep.EFFORT -> {
                _uiState.value = current.copy(
                    rouletteStep = RouletteStep.CATEGORY,
                    rouletteResult = null
                )
            }
            current.currentSection == HomeSection.ROULETTE -> openMenu()
        }
    }

    fun selectCookingCategoryMode(mode: CookingCategoryMode) {
        _uiState.value = _uiState.value.copy(
            cookingCategoryMode = mode,
            rouletteStep = RouletteStep.EFFORT,
            selectedEffort = null,
            selectedIngredients = emptyList(),
            ingredientQuery = "",
            ingredientMatchMode = IngredientMatchMode.FLEXIBLE,
            rouletteResult = null,
            rouletteSpinCount = 0
        )
    }

    fun selectEffort(effort: CookingEffort) {
        _uiState.value = _uiState.value.copy(
            selectedEffort = effort,
            rouletteStep = RouletteStep.INGREDIENTS,
            rouletteResult = null,
            rouletteSpinCount = 0
        )
    }

    fun updateIngredientQuery(query: String) {
        _uiState.value = _uiState.value.copy(ingredientQuery = query)
    }

    fun addIngredient(ingredient: String) {
        val cleanIngredient = ingredient.trim()
        if (cleanIngredient.isBlank()) return

        val current = _uiState.value
        if (current.selectedIngredients.any { it.equals(cleanIngredient, ignoreCase = true) }) {
            _uiState.value = current.copy(ingredientQuery = "")
            return
        }

        _uiState.value = current.copy(
            selectedIngredients = current.selectedIngredients + cleanIngredient,
            ingredientQuery = "",
            rouletteResult = null,
            rouletteSpinCount = 0
        )
    }

    fun removeIngredient(ingredient: String) {
        _uiState.value = _uiState.value.copy(
            selectedIngredients = _uiState.value.selectedIngredients.filterNot { it == ingredient },
            rouletteResult = null,
            rouletteSpinCount = 0
        )
    }

    fun goToSpinStep() {
        _uiState.value = _uiState.value.copy(
            rouletteStep = RouletteStep.INGREDIENT_SCOPE,
            rouletteResult = null,
            rouletteSpinCount = 0
        )
    }

    fun goToIngredientsStep() {
        _uiState.value = _uiState.value.copy(
            rouletteStep = RouletteStep.INGREDIENTS,
            rouletteResult = null,
            rouletteSpinCount = 0
        )
    }

    fun goToIngredientScopeStep() {
        _uiState.value = _uiState.value.copy(
            rouletteStep = RouletteStep.INGREDIENT_SCOPE,
            rouletteResult = null,
            rouletteSpinCount = 0
        )
    }

    fun selectIngredientMatchMode(mode: IngredientMatchMode) {
        _uiState.value = _uiState.value.copy(
            ingredientMatchMode = mode,
            rouletteStep = RouletteStep.SPIN,
            rouletteResult = null,
            rouletteSpinCount = 0
        )
    }

    fun selectDeliveryPeopleMode(mode: DeliveryPeopleMode) {
        _uiState.value = _uiState.value.copy(
            deliveryPeopleMode = mode,
            deliveryStep = DeliveryStep.BUDGET,
            deliveryResult = null,
            deliverySpinCount = 0
        )
    }

    fun selectDeliveryBudget(budget: DeliveryBudget) {
        _uiState.value = _uiState.value.copy(
            deliveryBudget = budget,
            deliveryStep = DeliveryStep.SPIN,
            deliveryResult = null,
            deliverySpinCount = 0
        )
    }

    fun spinDeliveryRoulette() {
        val current = _uiState.value
        val peopleMode = current.deliveryPeopleMode ?: return
        val budget = current.deliveryBudget ?: return
        val result = deliveryOptions
            .filter { option ->
                option.peopleModes.contains(peopleMode) && option.budgets.contains(budget)
            }
            .let { candidates ->
                candidates
                    .filterNot { option -> option.name == current.deliveryResult }
                    .ifEmpty { candidates }
            }
            .randomOrNull()
            ?.name

        _uiState.value = current.copy(
            deliveryResult = result,
            deliverySpinCount = current.deliverySpinCount + 1
        )
    }

    fun spinRoulette() {
        val current = _uiState.value
        val effort = current.selectedEffort ?: return
        val result = pickRouletteRecipeUseCase(
            recipes = current.recipes,
            categoryMode = current.cookingCategoryMode,
            effort = effort,
            selectedIngredients = current.selectedIngredients,
            ingredientMatchMode = current.ingredientMatchMode,
            excludedRecipeIds = current.rouletteResult?.let { setOf(it.id) }.orEmpty()
        )
        _uiState.value = current.copy(
            rouletteResult = result,
            rouletteSpinCount = current.rouletteSpinCount + 1
        )
    }

    fun openRecipeDetail(recipeId: String) {
        val recipe = (_uiState.value.recipes + _uiState.value.favoriteRecipes)
            .firstOrNull { it.id == recipeId }
        _uiState.value = _uiState.value.copy(detailRecipe = recipe)
    }

    fun openFullRecipeDetail(recipeId: String) {
        val current = _uiState.value
        val recipe = (current.recipes + current.favoriteRecipes)
            .firstOrNull { it.id == recipeId }
            ?: current.detailRecipe
            ?: return

        _uiState.value = current.copy(
            currentSection = HomeSection.RECIPE_DETAIL,
            detailReturnSection = current.currentSection,
            fullDetailRecipe = recipe,
            detailRecipe = null,
            isAssistantOpen = false
        )
    }

    fun closeRecipeDetail() {
        _uiState.value = _uiState.value.copy(detailRecipe = null)
    }

    fun openAssistant() {
        _uiState.value = _uiState.value.copy(isAssistantOpen = true)
    }

    fun closeAssistant() {
        _uiState.value = _uiState.value.copy(isAssistantOpen = false)
    }

    fun toggleFavorite(recipeId: String) {
        viewModelScope.launch {
            toggleFavoriteRecipeUseCase(recipeId)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
            _uiState.value = HomeUiState(isSignedOut = true)
        }
    }

    fun consumeSignedOut() {
        _uiState.value = _uiState.value.copy(isSignedOut = false)
    }

    private fun loadRecipes() {
        recipesJob?.cancel()
        recipesJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingRecipes = true,
                errorMessage = null
            )

            runCatching { seedRecipesUseCase() }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingRecipes = false,
                        errorMessage = error.message ?: "No se pudieron cargar las recetas."
                    )
                    return@launch
                }

            combine(
                observeRecipesUseCase(),
                observeFavoriteRecipesUseCase(),
                observeFavoriteIdsUseCase()
            ) { recipes, favorites, favoriteIds ->
                Triple(recipes, favorites, favoriteIds.toSet())
            }
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingRecipes = false,
                        errorMessage = error.message ?: "No se pudieron leer las recetas."
                    )
                }
                .collect { (recipes, favorites, favoriteIds) ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingRecipes = false,
                        recipes = recipes,
                        favoriteRecipes = favorites,
                        favoriteIds = favoriteIds,
                        errorMessage = null
                    )
                }
        }
    }

    fun ingredientSuggestions(): List<String> {
        val current = _uiState.value
        val query = current.ingredientQuery.trim().lowercase()
        if (query.length < 2) return emptyList()

        return current.recipes
            .flatMap { it.ingredients }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .filter { ingredient ->
                ingredient.lowercase().contains(query) &&
                    current.selectedIngredients.none { it.equals(ingredient, ignoreCase = true) }
            }
            .take(8)
    }

    private data class DeliveryOption(
        val name: String,
        val peopleModes: Set<DeliveryPeopleMode>,
        val budgets: Set<DeliveryBudget>
    )

    private companion object {
        val deliveryOptions = listOf(
            DeliveryOption("Empanadas", setOf(DeliveryPeopleMode.SOLO, DeliveryPeopleMode.GROUP), setOf(DeliveryBudget.LOW, DeliveryBudget.MEDIUM)),
            DeliveryOption("Pizza", setOf(DeliveryPeopleMode.SOLO, DeliveryPeopleMode.GROUP), setOf(DeliveryBudget.LOW, DeliveryBudget.MEDIUM)),
            DeliveryOption("Hamburguesa", setOf(DeliveryPeopleMode.SOLO, DeliveryPeopleMode.GROUP), setOf(DeliveryBudget.MEDIUM, DeliveryBudget.HIGH)),
            DeliveryOption("Sandwich de milanesa", setOf(DeliveryPeopleMode.SOLO, DeliveryPeopleMode.GROUP), setOf(DeliveryBudget.LOW, DeliveryBudget.MEDIUM)),
            DeliveryOption("Lomito", setOf(DeliveryPeopleMode.SOLO, DeliveryPeopleMode.GROUP), setOf(DeliveryBudget.MEDIUM, DeliveryBudget.HIGH)),
            DeliveryOption("Sushi", setOf(DeliveryPeopleMode.SOLO, DeliveryPeopleMode.GROUP), setOf(DeliveryBudget.HIGH)),
            DeliveryOption("Tacos", setOf(DeliveryPeopleMode.SOLO, DeliveryPeopleMode.GROUP), setOf(DeliveryBudget.MEDIUM, DeliveryBudget.HIGH)),
            DeliveryOption("Burritos", setOf(DeliveryPeopleMode.SOLO, DeliveryPeopleMode.GROUP), setOf(DeliveryBudget.MEDIUM)),
            DeliveryOption("Pollo frito", setOf(DeliveryPeopleMode.SOLO, DeliveryPeopleMode.GROUP), setOf(DeliveryBudget.MEDIUM)),
            DeliveryOption("Wok asiatico", setOf(DeliveryPeopleMode.SOLO, DeliveryPeopleMode.GROUP), setOf(DeliveryBudget.MEDIUM, DeliveryBudget.HIGH)),
            DeliveryOption("Pastas", setOf(DeliveryPeopleMode.SOLO, DeliveryPeopleMode.GROUP), setOf(DeliveryBudget.MEDIUM, DeliveryBudget.HIGH)),
            DeliveryOption("Parrilla", setOf(DeliveryPeopleMode.GROUP), setOf(DeliveryBudget.HIGH)),
            DeliveryOption("Picada", setOf(DeliveryPeopleMode.GROUP), setOf(DeliveryBudget.MEDIUM, DeliveryBudget.HIGH)),
            DeliveryOption("Helado", setOf(DeliveryPeopleMode.SOLO, DeliveryPeopleMode.GROUP), setOf(DeliveryBudget.LOW, DeliveryBudget.MEDIUM, DeliveryBudget.HIGH)),
            DeliveryOption("Papas con cheddar", setOf(DeliveryPeopleMode.SOLO, DeliveryPeopleMode.GROUP), setOf(DeliveryBudget.LOW, DeliveryBudget.MEDIUM)),
            DeliveryOption("Comida china", setOf(DeliveryPeopleMode.SOLO, DeliveryPeopleMode.GROUP), setOf(DeliveryBudget.MEDIUM, DeliveryBudget.HIGH)),
            DeliveryOption("Poke bowl", setOf(DeliveryPeopleMode.SOLO), setOf(DeliveryBudget.HIGH)),
            DeliveryOption("Tostados", setOf(DeliveryPeopleMode.SOLO), setOf(DeliveryBudget.LOW)),
            DeliveryOption("Arepas", setOf(DeliveryPeopleMode.SOLO, DeliveryPeopleMode.GROUP), setOf(DeliveryBudget.MEDIUM)),
            DeliveryOption("Wraps", setOf(DeliveryPeopleMode.SOLO, DeliveryPeopleMode.GROUP), setOf(DeliveryBudget.MEDIUM))
        )
    }
}
