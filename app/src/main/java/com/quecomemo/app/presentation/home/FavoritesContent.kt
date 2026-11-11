package com.quecomemo.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
internal fun FavoritesContent(
    recipes: List<Recipe>,
    favoriteIds: Set<String>,
    onOpenRecipe: (Recipe) -> Unit,
    onToggleFavorite: (Recipe) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var expandedRecipeId by remember { mutableStateOf<String?>(recipes.firstOrNull()?.id) }
    val filteredRecipes = remember(recipes, query) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            recipes
        } else {
            recipes.filter { recipe ->
                recipe.name.contains(cleanQuery, ignoreCase = true) ||
                    recipe.ingredients.any { it.contains(cleanQuery, ignoreCase = true) }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 31.dp, top = 45.dp, end = 31.dp, bottom = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Favoritos",
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
                .padding(top = 10.dp)
        )

        if (filteredRecipes.isEmpty()) {
            EmptyFavoritesState(
                modifier = Modifier.padding(top = 91.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 25.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                filteredRecipes.forEach { recipe ->
                    FavoriteRecipeItem(
                    recipe = recipe,
                    isFavorite = recipe.id in favoriteIds,
                        isExpanded = expandedRecipeId == recipe.id,
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
internal fun FavoriteSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = Color(0xFF989797),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 22.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 34.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
            decorationBox = { innerTextField ->
                if (value.isBlank()) {
                    Text(
                        text = "search",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF989797)
                    )
                }
                innerTextField()
            }
        )
        SearchIcon(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(22.dp)
        )
    }
}

@Composable
private fun FavoriteRecipeItem(
    recipe: Recipe,
    isFavorite: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onOpenRecipe: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(10.dp),
                clip = false
            )
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(Color(0xFFFFAA48))
                .clickable(onClick = onClick)
                .padding(start = 48.dp, end = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = recipe.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (isFavorite) "\u2665" else "\u2661",
                modifier = Modifier.clickable(onClick = onToggleFavorite),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(154.dp)
                    .padding(start = 24.dp, top = 14.dp, end = 16.dp, bottom = 11.dp)
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

@Composable
internal fun EmptyFavoritesState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CloudOffIcon(modifier = Modifier.size(64.dp))
        Text(
            text = "No se encontraron resultados",
            modifier = Modifier.padding(top = 42.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xB3000000),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
