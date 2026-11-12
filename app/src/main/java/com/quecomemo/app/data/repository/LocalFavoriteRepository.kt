package com.quecomemo.app.data.repository

import com.quecomemo.app.data.local.dao.FavoriteRecipeDao
import com.quecomemo.app.data.local.entity.FavoriteRecipeEntity
import com.quecomemo.app.data.mapper.toDomain
import com.quecomemo.app.domain.model.Recipe
import com.quecomemo.app.domain.repository.FavoriteRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalFavoriteRepository @Inject constructor(
    private val favoriteRecipeDao: FavoriteRecipeDao
) : FavoriteRepository {
    override fun observeFavoriteRecipes(): Flow<List<Recipe>> {
        return favoriteRecipeDao.observeFavoriteRecipes().map { recipes ->
            recipes.map { it.toDomain() }
        }
    }

    override fun observeFavoriteIds(): Flow<List<String>> {
        return favoriteRecipeDao.observeFavoriteIds()
    }

    override suspend fun toggleFavorite(recipeId: String) {
        if (favoriteRecipeDao.isFavorite(recipeId)) {
            favoriteRecipeDao.removeFavorite(recipeId)
        } else {
            favoriteRecipeDao.addFavorite(
                FavoriteRecipeEntity(
                    recipeId = recipeId,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }
}
