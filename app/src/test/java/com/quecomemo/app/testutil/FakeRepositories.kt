package com.quecomemo.app.testutil

import com.quecomemo.app.domain.model.AppUser
import com.quecomemo.app.domain.model.Recipe
import com.quecomemo.app.domain.repository.AuthRepository
import com.quecomemo.app.domain.repository.FavoriteRepository
import com.quecomemo.app.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeRecipeRepository(
    recipes: List<Recipe> = emptyList(),
    var seedError: Throwable? = null
) : RecipeRepository {
    val recipes = MutableStateFlow(recipes)
    var seedCalls = 0
        private set

    override suspend fun ensureSeedData() {
        seedCalls++
        seedError?.let { throw it }
    }

    override fun observeRecipes(): Flow<List<Recipe>> = recipes

    override suspend fun getRecipeById(recipeId: String): Recipe? =
        recipes.value.firstOrNull { it.id == recipeId }
}

class FakeFavoriteRepository(
    private val recipeRepository: FakeRecipeRepository
) : FavoriteRepository {
    private val favoriteIds = MutableStateFlow<List<String>>(emptyList())

    override fun observeFavoriteRecipes(): Flow<List<Recipe>> = favoriteIds.map { ids ->
        recipeRepository.recipes.value.filter { it.id in ids }
    }

    override fun observeFavoriteIds(): Flow<List<String>> = favoriteIds

    override suspend fun toggleFavorite(recipeId: String) {
        val current = favoriteIds.value
        favoriteIds.value = if (recipeId in current) current - recipeId else current + recipeId
    }
}

class FakeAuthRepository(
    private var user: AppUser? = AppUser(displayName = "Ana", email = "ana@example.com")
) : AuthRepository {
    var signOutCalls = 0
        private set

    override fun getCurrentUser(): AppUser? = user

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<AppUser> =
        user?.let { Result.success(it) } ?: Result.failure(IllegalStateException("Sin usuario"))

    override suspend fun signOut() {
        signOutCalls++
        user = null
    }
}
