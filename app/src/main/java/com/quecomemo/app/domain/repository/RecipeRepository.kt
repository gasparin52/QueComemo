package com.quecomemo.app.domain.repository

import com.quecomemo.app.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    suspend fun ensureSeedData()
    fun observeRecipes(): Flow<List<Recipe>>
    suspend fun getRecipeById(recipeId: String): Recipe?
}
