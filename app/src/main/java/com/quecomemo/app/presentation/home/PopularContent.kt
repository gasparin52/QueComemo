package com.quecomemo.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quecomemo.app.domain.model.Recipe

@Composable
internal fun PopularContent(
    recipes: List<Recipe>,
    favoriteIds: Set<String>,
    onOpenRecipe: (Recipe) -> Unit,
    onToggleFavorite: (Recipe) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var expandedRecipeId by remember { mutableStateOf<String?>(recipes.firstOrNull()?.id) }
    val topRecipes = remember(recipes) { recipes.take(10) }
    val filteredRecipes = remember(topRecipes, query) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            topRecipes
        } else {
            topRecipes.filter { recipe ->
                recipe.name.contains(cleanQuery, ignoreCase = true) ||
                    recipe.ingredients.any { it.contains(cleanQuery, ignoreCase = true) }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 44.dp, top = 28.dp, end = 44.dp, bottom = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "M\u00E1s populares de hoy",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BrandWine,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        FavoriteSearchField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 34.dp)
        )

        if (filteredRecipes.isEmpty()) {
            EmptyFavoritesState(
                modifier = Modifier.padding(top = 91.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 38.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                filteredRecipes.forEach { recipe ->
                    val rank = topRecipes.indexOfFirst { it.id == recipe.id }.takeIf { it >= 0 }?.plus(1) ?: 1
                    PopularRecipeItem(
                        rank = rank,
                        recipe = recipe,
                        isFavorite = recipe.id in favoriteIds,
                        isExpanded = expandedRecipeId == recipe.id,
                        popularity = 986 - ((rank - 1) * 7),
                        onClick = {
                            expandedRecipeId = if (expandedRecipeId == recipe.id) null else recipe.id
                        },
                        onOpenRecipe = { onOpenRecipe(recipe) },
                        onToggleFavorite = { onToggleFavorite(recipe) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PopularRecipeItem(
    rank: Int,
    recipe: Recipe,
    isFavorite: Boolean,
    isExpanded: Boolean,
    popularity: Int,
    onClick: () -> Unit,
    onOpenRecipe: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isExpanded) 4.dp else 0.dp,
                shape = RoundedCornerShape(5.dp),
                clip = false
            )
            .clip(RoundedCornerShape(5.dp))
            .background(if (isExpanded) Color.White else Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(41.dp)
                .background(Color(0xFFFFAA48))
                .clickable(onClick = onClick)
                .padding(start = 16.dp, end = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rank. ${recipe.name}",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "\u2665",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
                Text(
                    text = popularity.toString(),
                    modifier = Modifier.padding(start = 5.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Text(
                text = if (isFavorite) "\u2665" else "\u2661",
                modifier = Modifier
                    .padding(start = 14.dp)
                    .clickable(onClick = onToggleFavorite),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(147.dp)
                    .padding(start = 24.dp, top = 18.dp, end = 16.dp, bottom = 11.dp)
                    .clickable(onClick = onOpenRecipe)
            ) {
                Text(
                    text = "Tiempo de cocci\u00F3n",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ClockIcon(modifier = Modifier.size(14.dp))
                    Text(
                        text = "Listo en ${recipe.totalTimeMinutes} min",
                        modifier = Modifier.padding(start = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Light,
                        color = Color(0xC9000000)
                    )
                }
                Text(
                    text = "Cantidad de ingredientes",
                    modifier = Modifier.padding(top = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BowlIcon(modifier = Modifier.size(17.dp))
                    Text(
                        text = "${recipe.ingredients.size} ingredientes",
                        modifier = Modifier.padding(start = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Light,
                        color = Color(0xC9000000)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(Color(0x1F000000))
                    )
                    Text(
                        text = "Dificultad: ${recipe.difficulty.toLabel()}",
                        modifier = Modifier.padding(start = 14.dp, end = 8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Light,
                        color = Color(0xC9000000)
                    )
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(recipe.difficulty.toColor())
                    )
                }
            }
        }
    }
}
