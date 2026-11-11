package com.quecomemo.app.domain.model

data class Recipe(
    val id: String,
    val name: String,
    val description: String,
    val imageAssetName: String?,
    val imageUrl: String?,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val servings: Int,
    val difficulty: RecipeDifficulty,
    val cookingMood: CookingMood,
    val estimatedCost: EstimatedCost,
    val category: String,
    val ingredients: List<String>,
    val steps: List<String>,
    val tags: List<String>
) {
    val totalTimeMinutes: Int
        get() = prepTimeMinutes + cookTimeMinutes
}
