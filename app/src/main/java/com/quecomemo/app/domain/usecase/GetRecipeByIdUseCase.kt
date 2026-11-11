package com.quecomemo.app.domain.usecase

import com.quecomemo.app.domain.model.Recipe
import com.quecomemo.app.domain.repository.RecipeRepository
import javax.inject.Inject

class GetRecipeByIdUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke(recipeId: String): Recipe? = recipeRepository.getRecipeById(recipeId)
}
