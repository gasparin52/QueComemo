package com.quecomemo.app.testutil

import com.quecomemo.app.domain.model.CookingMood
import com.quecomemo.app.domain.model.EstimatedCost
import com.quecomemo.app.domain.model.Recipe
import com.quecomemo.app.domain.model.RecipeDifficulty

fun testRecipe(
    id: String,
    name: String = id,
    totalMinutes: Int = 15,
    ingredients: List<String> = emptyList(),
    steps: List<String> = listOf("Cocinar."),
    category: String = "Argentino cotidiano",
    tags: List<String> = emptyList()
) = Recipe(
    id = id,
    name = name,
    description = "",
    imageAssetName = null,
    imageUrl = null,
    prepTimeMinutes = 0,
    cookTimeMinutes = totalMinutes,
    servings = 2,
    difficulty = RecipeDifficulty.EASY,
    cookingMood = CookingMood.LOW,
    estimatedCost = EstimatedCost.LOW,
    category = category,
    ingredients = ingredients,
    steps = steps,
    tags = tags
)
