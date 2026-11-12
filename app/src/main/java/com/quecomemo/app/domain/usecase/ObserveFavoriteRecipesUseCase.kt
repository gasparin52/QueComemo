package com.quecomemo.app.domain.usecase

import com.quecomemo.app.domain.model.Recipe
import com.quecomemo.app.domain.repository.FavoriteRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveFavoriteRecipesUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    operator fun invoke(): Flow<List<Recipe>> = favoriteRepository.observeFavoriteRecipes()
}
