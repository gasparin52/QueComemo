package com.quecomemo.app.data.repository

import com.quecomemo.app.data.local.dao.RecipeDao
import com.quecomemo.app.data.local.entity.RecipeEntity
import com.quecomemo.app.data.local.seed.RecipeSeedReader
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalRecipeRepositoryTest {
    private val recipeDao = mockk<RecipeDao>(relaxUnitFun = true)
    private val seedReader = mockk<RecipeSeedReader>()
    private val repository = LocalRecipeRepository(recipeDao, seedReader)

    private val seed = listOf(entity("tortilla"), entity("omelette"))

    @Test
    fun `importa el seed cuando la base esta vacia`() = runTest {
        givenDatabase(count = 0)

        repository.ensureSeedData()

        coVerify { recipeDao.deleteAllRecipes() }
        coVerify { recipeDao.insertRecipes(seed) }
    }

    @Test
    fun `reimporta cuando el seed del APK tiene otra cantidad de recetas`() = runTest {
        givenDatabase(count = 1)

        repository.ensureSeedData()

        coVerify { recipeDao.insertRecipes(seed) }
    }

    @Test
    fun `no toca la base si ya esta al dia`() = runTest {
        givenDatabase(count = seed.size)

        repository.ensureSeedData()

        coVerify(exactly = 0) { recipeDao.deleteAllRecipes() }
        coVerify(exactly = 0) { recipeDao.insertRecipes(any()) }
    }

    @Test
    fun `mapea las entidades de Room a modelos de dominio`() = runTest {
        every { recipeDao.observeRecipes() } returns flowOf(seed)

        val recipes = repository.observeRecipes().first()

        assertEquals(listOf("tortilla", "omelette"), recipes.map { it.id })
        assertEquals(listOf("huevo", "sal"), recipes.first().ingredients)
        assertEquals(25, recipes.first().totalTimeMinutes)
    }

    private fun givenDatabase(count: Int) {
        every { seedReader.readRecipes() } returns seed
        coEvery { recipeDao.countRecipes() } returns count
        coEvery { recipeDao.existsRecipe(any()) } returns false
    }

    private fun entity(id: String) = RecipeEntity(
        id = id,
        name = id,
        description = "",
        imageAssetName = null,
        imageUrl = null,
        prepTimeMinutes = 10,
        cookTimeMinutes = 15,
        servings = 2,
        difficulty = "EASY",
        cookingMood = "MEDIUM",
        estimatedCost = "LOW",
        category = "Rapidas",
        ingredientsJson = """["huevo","sal"]""",
        stepsJson = """["Cocinar."]""",
        tagsJson = """["cocina"]"""
    )
}
