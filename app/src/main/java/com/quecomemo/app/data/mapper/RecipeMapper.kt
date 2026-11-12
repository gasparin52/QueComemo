package com.quecomemo.app.data.mapper

import com.quecomemo.app.data.local.entity.RecipeEntity
import com.quecomemo.app.domain.model.CookingMood
import com.quecomemo.app.domain.model.EstimatedCost
import com.quecomemo.app.domain.model.Recipe
import com.quecomemo.app.domain.model.RecipeDifficulty
import org.json.JSONArray

fun RecipeEntity.toDomain(): Recipe {
    return Recipe(
        id = id,
        name = name,
        description = description,
        imageAssetName = imageAssetName,
        imageUrl = imageUrl,
        prepTimeMinutes = prepTimeMinutes,
        cookTimeMinutes = cookTimeMinutes,
        servings = servings,
        difficulty = difficulty.toEnum(default = RecipeDifficulty.EASY),
        cookingMood = cookingMood.toEnum(default = CookingMood.MEDIUM),
        estimatedCost = estimatedCost.toEnum(default = EstimatedCost.MEDIUM),
        category = category,
        ingredients = ingredientsJson.toStringList(),
        steps = stepsJson.toStringList(),
        tags = tagsJson.toStringList()
    )
}

private inline fun <reified T : Enum<T>> String.toEnum(default: T): T {
    return runCatching { enumValueOf<T>(uppercase()) }.getOrDefault(default)
}

private fun String.toStringList(): List<String> {
    val array = JSONArray(this)
    return buildList {
        for (index in 0 until array.length()) {
            add(array.getString(index))
        }
    }
}
