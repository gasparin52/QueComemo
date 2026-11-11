package com.quecomemo.app.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quecomemo.app.R

@Composable
internal fun MainMenu(
    onRoulette: () -> Unit,
    onDelivery: () -> Unit,
    onFavorites: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 31.dp, vertical = 56.dp),
        verticalArrangement = Arrangement.spacedBy(34.dp)
    ) {
        Text(
            text = "\u00BFQu\u00E9 te gustar\u00EDa hacer hoy?",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = BrandWine,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        MenuOptionCard(
            title = "Ruleta de cocina",
            description = "Elegis ganas de cocinar, ingredientes disponibles y la app sortea una receta.",
            enabled = true,
            imageRes = R.drawable.home_card_roulette,
            imageStyle = HomeCardImageStyle.ROULETTE,
            gradient = Brush.horizontalGradient(listOf(Color(0xFFFFF4E5), Color(0xFFFFC078))),
            onClick = onRoulette
        )
        MenuOptionCard(
            title = "Ruleta de delivery",
            description = "Decidi que pedir segun presupuesto y cantidad de personas.",
            enabled = true,
            imageRes = R.drawable.home_card_popular,
            imageStyle = HomeCardImageStyle.DELIVERY,
            gradient = Brush.horizontalGradient(listOf(Color(0xFFEAF8FF), Color(0xFF78D7FF))),
            onClick = onDelivery
        )
        MenuOptionCard(
            title = "Recetas favoritas",
            description = "Acceso rapido a las recetas que guardaste en este celular.",
            enabled = true,
            imageRes = R.drawable.home_card_favorites,
            imageStyle = HomeCardImageStyle.FAVORITES,
            gradient = Brush.horizontalGradient(listOf(Color(0xFFFFF1F1), Color(0xFFFF7878))),
            onClick = onFavorites
        )
    }
}

@Composable
private fun MenuOptionCard(
    title: String,
    description: String,
    enabled: Boolean,
    imageRes: Int,
    imageStyle: HomeCardImageStyle,
    gradient: Brush,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(153.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 3.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x24000000))
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(10.dp))
                .background(gradient)
                .clickable(enabled = enabled, onClick = onClick)
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .homeCardImageModifier(imageStyle)
                    .align(Alignment.CenterStart)
                    .graphicsLayer { alpha = if (enabled) 0.78f else 0.62f }
            )
            Text(
                text = title,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .fillMaxWidth(0.46f),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (title.contains("delivery")) Color.Black else BrandWine,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private enum class HomeCardImageStyle {
    ROULETTE,
    DELIVERY,
    FAVORITES,
    POPULAR
}

private fun Modifier.homeCardImageModifier(style: HomeCardImageStyle): Modifier {
    return when (style) {
        HomeCardImageStyle.ROULETTE -> this
            .size(400.dp)
            .offset(x = (-85).dp, y = (0).dp)
        HomeCardImageStyle.DELIVERY -> this
            .width(400.dp)
            .height(213.dp)
            .offset(x = (-75).dp, y = (0).dp)
        HomeCardImageStyle.FAVORITES -> this
            .width(400.dp)
            .height(185.dp)
            .offset(x = (-65).dp, y = (0).dp)
        HomeCardImageStyle.POPULAR -> this
            .width(400.dp)
            .height(213.dp)
            .offset(x = (-75).dp, y = (0).dp)
    }
}
