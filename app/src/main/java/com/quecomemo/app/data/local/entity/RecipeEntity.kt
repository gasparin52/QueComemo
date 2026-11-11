package com.quecomemo.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val imageAssetName: String?,
    val imageUrl: String?,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val servings: Int,
    val difficulty: String,
    val cookingMood: String,
    val estimatedCost: String,
    val category: String,
    val ingredientsJson: String,
    val stepsJson: String,
    val tagsJson: String
)
