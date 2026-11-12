package com.quecomemo.app.presentation.home

import com.quecomemo.app.domain.model.CookingEffort
import com.quecomemo.app.domain.model.CookingCategoryMode
import com.quecomemo.app.domain.model.IngredientMatchMode
import com.quecomemo.app.domain.model.Recipe

data class HomeUiState(
    val displayName: String? = null,
    val email: String? = null,
    val isSignedOut: Boolean = false,
    val isLoadingRecipes: Boolean = true,
    val recipes: List<Recipe> = emptyList(),
    val favoriteRecipes: List<Recipe> = emptyList(),
    val favoriteIds: Set<String> = emptySet(),
    val errorMessage: String? = null,
    val currentSection: HomeSection = HomeSection.MENU,
    val rouletteStep: RouletteStep = RouletteStep.CATEGORY,
    val deliveryStep: DeliveryStep = DeliveryStep.PEOPLE,
    val cookingCategoryMode: CookingCategoryMode = CookingCategoryMode.MEAL,
    val selectedEffort: CookingEffort? = null,
    val ingredientQuery: String = "",
    val selectedIngredients: List<String> = emptyList(),
    val ingredientMatchMode: IngredientMatchMode = IngredientMatchMode.FLEXIBLE,
    val rouletteResult: Recipe? = null,
    val rouletteSpinCount: Int = 0,
    val deliveryPeopleMode: DeliveryPeopleMode? = null,
    val deliveryBudget: DeliveryBudget? = null,
    val deliveryResult: String? = null,
    val deliverySpinCount: Int = 0,
    val detailRecipe: Recipe? = null,
    val fullDetailRecipe: Recipe? = null,
    val detailReturnSection: HomeSection = HomeSection.ROULETTE,
    val isAssistantOpen: Boolean = false
)
