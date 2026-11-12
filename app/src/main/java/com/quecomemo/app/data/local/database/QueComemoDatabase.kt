package com.quecomemo.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.quecomemo.app.data.local.dao.FavoriteRecipeDao
import com.quecomemo.app.data.local.dao.RecipeDao
import com.quecomemo.app.data.local.entity.FavoriteRecipeEntity
import com.quecomemo.app.data.local.entity.RecipeEntity

@Database(
    entities = [RecipeEntity::class, FavoriteRecipeEntity::class],
    version = 3,
    exportSchema = false
)
abstract class QueComemoDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun favoriteRecipeDao(): FavoriteRecipeDao
}
