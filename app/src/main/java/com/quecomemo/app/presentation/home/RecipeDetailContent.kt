package com.quecomemo.app.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.quecomemo.app.domain.model.Recipe
import com.quecomemo.app.presentation.assistant.AssistantFloatingButton
import com.quecomemo.app.presentation.assistant.RecipeAssistantDialog

@Composable
internal fun RecipeDetailDialog(
    recipe: Recipe,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onOpenFullDetail: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(180)) + scaleIn(
                animationSpec = tween(220),
                initialScale = 0.92f
            ),
            exit = fadeOut(tween(120)) + scaleOut(tween(120))
        ) {
            Card(
                modifier = Modifier
                    .width(381.dp)
                    .height(517.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0x33000000))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 43.dp, top = 30.dp, end = 33.dp, bottom = 31.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = recipe.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .width(295.dp)
                            .height(196.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color(0xFFF1F1F1))
                            .border(
                                width = 1.dp,
                                color = Color(0x29000000),
                                shape = RoundedCornerShape(15.dp)
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(144.dp)
                            .padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        recipe.steps.take(2).forEachIndexed { index, step ->
                            Text(
                                text = "${index + 1}. $step",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    lineHeight = 12.sp
                                ),
                                fontWeight = FontWeight.Light,
                                color = Color(0xBA000000),
                                maxLines = if (index == 0) 6 else 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .width(188.dp)
                            .height(31.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(10.dp),
                                clip = false
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFF5700))
                            .clickable(onClick = onOpenFullDetail),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ver",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun FullRecipeDetailContent(
    recipe: Recipe,
    isFavorite: Boolean,
    isAssistantOpen: Boolean,
    onToggleFavorite: () -> Unit,
    onOpenAssistant: () -> Unit,
    onCloseAssistant: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 52.dp, top = 48.dp, end = 52.dp, bottom = 96.dp)
        ) {
            Text(
                text = recipe.name,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Box(
                modifier = Modifier
                    .padding(top = 36.dp)
                    .width(295.dp)
                    .height(196.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF1F1F1))
                    .border(
                        width = 1.dp,
                        color = Color(0x22000000),
                        shape = RoundedCornerShape(10.dp)
                    )
            )

            Text(
                text = "Tiempo",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xB8000000),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = "Listo en ${recipe.totalTimeMinutes} minutos.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 17.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xB8000000),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Text(
                text = "Ingredientes",
                modifier = Modifier.padding(top = 40.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xBA000000)
            )
            Text(
                text = recipe.ingredients.joinToString(separator = "\n"),
                modifier = Modifier.padding(top = 17.dp),
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 16.sp),
                color = Color.Black
            )

            Text(
                text = "Instrucciones",
                modifier = Modifier.padding(top = 40.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xBA000000)
            )
            Text(
                text = recipe.steps.mapIndexed { index, step ->
                    "${('a'.code + index).toChar()}. $step"
                }.joinToString(separator = "\n"),
                modifier = Modifier.padding(top = 24.dp),
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 16.sp),
                color = Color(0xBA000000)
            )
        }

        AssistantFloatingButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 30.dp, bottom = 24.dp),
            onClick = onOpenAssistant
        )

        if (isAssistantOpen) {
            RecipeAssistantDialog(recipe = recipe, onDismiss = onCloseAssistant)
        }
    }
}

@Composable
internal fun RecipeChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}
