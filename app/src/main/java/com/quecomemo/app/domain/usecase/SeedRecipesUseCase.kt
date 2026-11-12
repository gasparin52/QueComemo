package com.quecomemo.app.domain.usecase

import com.quecomemo.app.domain.repository.RecipeRepository
import javax.inject.Inject

class SeedRecipesUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    suspend operator fun invoke() = recipeRepository.ensureSeedData()
}
