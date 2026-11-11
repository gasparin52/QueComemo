package com.quecomemo.app.data.local.seed

import android.content.Context
import com.quecomemo.app.data.local.entity.RecipeEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import org.json.JSONArray

class RecipeSeedReader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun readRecipes(): List<RecipeEntity> {
        val json = context.assets.open(SEED_FILE_NAME)
            .bufferedReader()
            .use { it.readText() }
        val recipes = JSONArray(json)

        return buildList {
            for (index in 0 until recipes.length()) {
                val item = recipes.getJSONObject(index)
                add(
                    RecipeEntity(
                        id = item.getString("id"),
                        name = item.getString("name"),
                        description = item.getString("description"),
                        imageAssetName = item.optString("imageAssetName").takeIf { it.isNotBlank() },
                        imageUrl = item.optString("imageUrl").takeIf { it.isNotBlank() },
                        prepTimeMinutes = item.getInt("prepTimeMinutes"),
                        cookTimeMinutes = item.getInt("cookTimeMinutes"),
                        servings = item.getInt("servings"),
                        difficulty = item.getString("difficulty"),
                        cookingMood = item.getString("cookingMood"),
                        estimatedCost = item.getString("estimatedCost"),
                        category = item.getString("category"),
                        ingredientsJson = item.getJSONArray("ingredients").toString(),
                        stepsJson = item.getJSONArray("steps").toString(),
                        tagsJson = item.getJSONArray("tags").toString()
                    )
                )
            }
        }
    }

    private companion object {
        const val SEED_FILE_NAME = "recipes_seed.json"
    }
}
