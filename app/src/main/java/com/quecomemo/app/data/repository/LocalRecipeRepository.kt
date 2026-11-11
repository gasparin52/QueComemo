package com.quecomemo.app.data.repository

import com.quecomemo.app.data.local.dao.RecipeDao
import com.quecomemo.app.data.local.seed.RecipeSeedReader
import com.quecomemo.app.data.mapper.toDomain
import com.quecomemo.app.domain.model.Recipe
import com.quecomemo.app.domain.repository.RecipeRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalRecipeRepository @Inject constructor(
    private val recipeDao: RecipeDao,
    private val recipeSeedReader: RecipeSeedReader
) : RecipeRepository {
    override suspend fun ensureSeedData() {
        val seedRecipes = recipeSeedReader.readRecipes()
        val currentCount = recipeDao.countRecipes()
        val hasOldGeneratedSeed = recipeDao.existsRecipe(OLD_GENERATED_SEED_ID)
        // Si el seed del APK cambio de tamano (recetas nuevas o quitadas), se reimporta.
        // Los favoritos viven en otra tabla y se conservan.
        if (currentCount != seedRecipes.size || hasOldGeneratedSeed) {
            recipeDao.deleteAllRecipes()
            recipeDao.insertRecipes(seedRecipes)
        }
    }

    override fun observeRecipes(): Flow<List<Recipe>> {
        return recipeDao.observeRecipes().map { recipes ->
            recipes.map { it.toDomain() }
        }
    }

    override suspend fun getRecipeById(recipeId: String): Recipe? {
        return recipeDao.getRecipeById(recipeId)?.toDomain()
    }

    private companion object {
        const val OLD_GENERATED_SEED_ID = "milanesa-napolitana-clasica"
    }
}
