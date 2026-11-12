package com.quecomemo.app.domain.repository

import com.quecomemo.app.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun observeFavoriteRecipes(): Flow<List<Recipe>>
    fun observeFavoriteIds(): Flow<List<String>>
    suspend fun toggleFavorite(recipeId: String)
}
