package com.quecomemo.app.domain.usecase

import com.quecomemo.app.domain.model.Recipe
import com.quecomemo.app.domain.repository.RecipeRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveRecipesUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    operator fun invoke(): Flow<List<Recipe>> = recipeRepository.observeRecipes()
}
