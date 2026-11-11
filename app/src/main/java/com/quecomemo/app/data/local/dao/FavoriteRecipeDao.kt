package com.quecomemo.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quecomemo.app.data.local.entity.FavoriteRecipeEntity
import com.quecomemo.app.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteRecipeDao {
    @Query(
        """
        SELECT recipes.* FROM recipes
        INNER JOIN favorite_recipes ON favorite_recipes.recipeId = recipes.id
        ORDER BY favorite_recipes.createdAt DESC
        """
    )
    fun observeFavoriteRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT recipeId FROM favorite_recipes")
    fun observeFavoriteIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_recipes WHERE recipeId = :recipeId)")
    suspend fun isFavorite(recipeId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteRecipeEntity)

    @Query("DELETE FROM favorite_recipes WHERE recipeId = :recipeId")
    suspend fun removeFavorite(recipeId: String)
}
