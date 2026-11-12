package com.quecomemo.app.domain.usecase

import com.quecomemo.app.domain.repository.FavoriteRepository
import javax.inject.Inject

class ToggleFavoriteRecipeUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(recipeId: String) = favoriteRepository.toggleFavorite(recipeId)
}
