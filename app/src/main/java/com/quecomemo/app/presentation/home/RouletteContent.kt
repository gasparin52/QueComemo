package com.quecomemo.app.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quecomemo.app.R
import com.quecomemo.app.domain.model.CookingCategoryMode
import com.quecomemo.app.domain.model.CookingEffort
import com.quecomemo.app.domain.model.IngredientMatchMode
import com.quecomemo.app.domain.model.Recipe

@Composable
internal fun RouletteFlow(
    uiState: HomeUiState,
    ingredientSuggestions: List<String>,
    onSelectCategoryMode: (CookingCategoryMode) -> Unit,
    onSelectEffort: (CookingEffort) -> Unit,
    onIngredientQueryChange: (String) -> Unit,
    onAddIngredient: (String) -> Unit,
    onRemoveIngredient: (String) -> Unit,
    onContinueToSpin: () -> Unit,
    onChangeIngredients: () -> Unit,
    onChangeIngredientMode: () -> Unit,
    onSelectIngredientMatchMode: (IngredientMatchMode) -> Unit,
    onSpin: () -> Unit,
    onOpenRecipe: (Recipe) -> Unit
) {
    AnimatedContent(
        targetState = uiState.rouletteStep,
        transitionSpec = {
            (fadeIn(tween(220)) + slideInHorizontally { it / 5 })
                .togetherWith(fadeOut(tween(150)) + slideOutHorizontally { -it / 6 })
        },
        label = "roulette-step-transition"
    ) { step ->
        when (step) {
            RouletteStep.CATEGORY -> CookingCategoryStep(onSelectCategoryMode = onSelectCategoryMode)
            RouletteStep.EFFORT -> EffortStep(onSelectEffort = onSelectEffort)
            RouletteStep.INGREDIENTS -> IngredientsStep(
                query = uiState.ingredientQuery,
                selectedIngredients = uiState.selectedIngredients,
                suggestions = ingredientSuggestions,
                onQueryChange = onIngredientQueryChange,
                onAddIngredient = onAddIngredient,
                onRemoveIngredient = onRemoveIngredient,
                onContinue = onContinueToSpin
            )
            RouletteStep.INGREDIENT_SCOPE -> IngredientScopeStep(
                onSelectMode = onSelectIngredientMatchMode
            )
            RouletteStep.SPIN -> SpinStep(
                selectedEffort = uiState.selectedEffort,
                selectedIngredients = uiState.selectedIngredients,
                ingredientMatchMode = uiState.ingredientMatchMode,
                result = uiState.rouletteResult,
                spinCount = uiState.rouletteSpinCount,
                onSpin = onSpin,
                onChangeIngredients = onChangeIngredients,
                onChangeIngredientMode = onChangeIngredientMode,
                onOpenRecipe = onOpenRecipe
            )
        }
    }
}

@Composable
private fun CookingCategoryStep(
    onSelectCategoryMode: (CookingCategoryMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 116.dp, end = 32.dp, bottom = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\u00BFQu\u00E9 quer\u00E9s\npreparar?",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BrandWine,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        IngredientScopeOption(
            title = "Comida",
            description = "Almuerzo, cena, algo rapido o una receta para cocinar en casa.",
            modifier = Modifier.padding(top = 55.dp),
            onClick = { onSelectCategoryMode(CookingCategoryMode.MEAL) }
        )
        IngredientScopeOption(
            title = "Postre",
            description = "Algo dulce para merienda, sobremesa o antojo.",
            modifier = Modifier.padding(top = 24.dp),
            onClick = { onSelectCategoryMode(CookingCategoryMode.DESSERT) }
        )
    }
}

@Composable
private fun EffortStep(
    onSelectEffort: (CookingEffort) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\u00BFcu\u00E1ntas ganas tienes\nde cocinar?",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BrandWine,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        EffortOption(
            title = "Pocas",
            imageRes = R.drawable.effort_low,
            modifier = Modifier.padding(top = 25.dp),
            onClick = { onSelectEffort(CookingEffort.LOW) }
        )
        EffortOption(
            title = "Un poco...",
            imageRes = R.drawable.effort_medium,
            modifier = Modifier.padding(top = 22.dp),
            onClick = { onSelectEffort(CookingEffort.MEDIUM) }
        )
        EffortOption(
            title = "Muchas!",
            imageRes = R.drawable.effort_high,
            modifier = Modifier.padding(top = 22.dp),
            onClick = { onSelectEffort(CookingEffort.HIGH) }
        )
    }
}

@Composable
private fun EffortOption(
    title: String,
    imageRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .width(170.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(142.dp)
                .clip(RoundedCornerShape(100.dp))
        )
        Text(
            text = title,
            modifier = Modifier.padding(top = 6.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun IngredientsStep(
    query: String,
    selectedIngredients: List<String>,
    suggestions: List<String>,
    onQueryChange: (String) -> Unit,
    onAddIngredient: (String) -> Unit,
    onRemoveIngredient: (String) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 136.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\u00BFQu\u00E9 ingredientes\ntienes?",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BrandWine,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        IngredientSearchField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp)
        )

        if (suggestions.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                suggestions.take(4).forEach { ingredient ->
                    Text(
                        text = ingredient,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .border(
                                width = 1.dp,
                                color = Color(0x33989797),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onAddIngredient(ingredient) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xB8000000),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp)
                .height(253.dp),
            color = Color.Transparent,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color(0x3B000000))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                selectedIngredients.chunked(3).forEach { rowIngredients ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowIngredients.forEach { ingredient ->
                            IngredientChip(
                                text = ingredient,
                                onRemove = { onRemoveIngredient(ingredient) }
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .padding(top = 84.dp)
                .width(188.dp)
                .height(31.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(10.dp),
                    clip = false
                )
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFFF5700))
                .clickable(onClick = onContinue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Siguiente",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun IngredientScopeStep(
    onSelectMode: (IngredientMatchMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 126.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\u00BFCocin\u00E1s solo\ncon esto?",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BrandWine,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        IngredientScopeOption(
            title = "Solo tengo esto",
            description = "Busca recetas que puedas hacer con tus ingredientes. Condimentos, agua y aceite no bloquean.",
            modifier = Modifier.padding(top = 54.dp),
            onClick = { onSelectMode(IngredientMatchMode.STRICT) }
        )

        IngredientScopeOption(
            title = "Puedo sumar ingredientes",
            description = "Busca recetas que usen lo que cargaste, aunque pidan otros ingredientes.",
            modifier = Modifier.padding(top = 24.dp),
            onClick = { onSelectMode(IngredientMatchMode.FLEXIBLE) }
        )
    }
}

@Composable
private fun IngredientScopeOption(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(10.dp),
                clip = false
            )
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFFAA48))
            .clickable(onClick = onClick)
            .padding(horizontal = 23.dp, vertical = 18.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = description,
            modifier = Modifier.padding(top = 9.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}

@Composable
private fun IngredientSearchField(
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
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color(0xDE000000)),
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
        Canvas(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(22.dp)
        ) {
            val iconColor = Color(0xFF989797)
            val stroke = Stroke(width = 2.dp.toPx())
            drawCircle(
                color = iconColor,
                radius = size.minDimension * 0.32f,
                center = center.copy(x = size.width * 0.42f, y = size.height * 0.42f),
                style = stroke
            )
            drawLine(
                color = iconColor,
                start = androidx.compose.ui.geometry.Offset(size.width * 0.66f, size.height * 0.66f),
                end = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.9f),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

@Composable
private fun IngredientChip(
    text: String,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .width(104.dp)
            .height(28.dp)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(10.dp),
                clip = false
            )
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color(0x30000000),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(start = 8.dp, end = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xB8000000),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "x",
            modifier = Modifier.clickable(onClick = onRemove),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8B8B8B)
        )
    }
}

@Composable
private fun SpinStep(
    selectedEffort: CookingEffort?,
    selectedIngredients: List<String>,
    ingredientMatchMode: IngredientMatchMode,
    result: Recipe?,
    spinCount: Int,
    onSpin: () -> Unit,
    onChangeIngredients: () -> Unit,
    onChangeIngredientMode: () -> Unit,
    onOpenRecipe: (Recipe) -> Unit
) {
    val wheelRotation by animateFloatAsState(
        targetValue = spinCount * 1080f,
        animationSpec = tween(durationMillis = 900),
        label = "roulette-logo-rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 136.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ruleta de comida",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BrandWine,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Image(
            painter = painterResource(id = R.drawable.logo_solo),
            contentDescription = "Ruleta de comida",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(top = 24.dp)
                .size(261.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(131.dp),
                    clip = false
                )
                .rotate(wheelRotation)
        )

        Box(
            modifier = Modifier
                .padding(top = 48.dp)
                .width(266.dp)
                .height(45.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x33F1F1F1))
                .border(
                    width = 1.dp,
                    color = Color(0x29000000),
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable(
                    enabled = result != null,
                    onClick = { result?.let(onOpenRecipe) }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = result?.name.orEmpty(),
                modifier = Modifier.padding(horizontal = 12.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BrandWine,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (spinCount > 0 && result == null) {
            Column(
                modifier = Modifier
                    .width(286.dp)
                    .padding(top = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (ingredientMatchMode) {
                        IngredientMatchMode.STRICT -> "No encontramos recetas solo con esos ingredientes. Prob\u00E1 sumar alguno m\u00E1s."
                        IngredientMatchMode.FLEXIBLE -> "No encontramos recetas con esos filtros. Prob\u00E1 sumar m\u00E1s ingredientes."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xB3000000),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RouletteSmallActionButton(
                        text = "Ingredientes",
                        onClick = onChangeIngredients
                    )
                    RouletteSmallActionButton(
                        text = "Modo",
                        onClick = onChangeIngredientMode
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(14.dp))
        }

        Box(
            modifier = Modifier
                .padding(top = 43.dp)
                .width(188.dp)
                .height(31.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(10.dp),
                    clip = false
                )
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFFF5700))
                .clickable(onClick = onSpin),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Spin",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun RouletteSmallActionButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(118.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = Color(0xFF989797),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = BrandWine
        )
    }
}

@Composable
private fun RecipeCard(
    recipe: Recipe,
    isFavorite: Boolean,
    onOpenRecipe: () -> Unit,
    onToggleFavorite: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = recipe.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = recipe.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RecipeChip(text = "${recipe.totalTimeMinutes} min")
                RecipeChip(text = recipe.category)
            }
            Text(
                text = "${recipe.difficulty.toLabel()} - ${recipe.cookingMood.toLabel()} - ${recipe.estimatedCost.toLabel()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onOpenRecipe) {
                    Text(text = "Ver receta")
                }
                onToggleFavorite?.let {
                    OutlinedButton(onClick = it) {
                        Text(text = if (isFavorite) "Quitar favorito" else "Guardar")
                    }
                }
            }
        }
    }
}
