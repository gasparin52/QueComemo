package com.quecomemo.app.presentation.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quecomemo.app.R
import com.quecomemo.app.domain.model.CookingEffort
import com.quecomemo.app.domain.model.CookingMood
import com.quecomemo.app.domain.model.EstimatedCost
import com.quecomemo.app.domain.model.RecipeDifficulty
import com.quecomemo.app.presentation.assistant.AssistantFloatingButton

@Composable
internal fun SearchIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
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

@Composable
internal fun ClockIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val iconColor = Color(0xFF555555)
        val stroke = Stroke(width = 1.4.dp.toPx())
        drawCircle(color = iconColor, style = stroke)
        drawLine(
            color = iconColor,
            start = center,
            end = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.25f),
            strokeWidth = 1.4.dp.toPx()
        )
        drawLine(
            color = iconColor,
            start = center,
            end = androidx.compose.ui.geometry.Offset(size.width * 0.7f, size.height * 0.55f),
            strokeWidth = 1.4.dp.toPx()
        )
    }
}

@Composable
internal fun BowlIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val iconColor = Color(0xFF555555)
        val strokeWidth = 1.4.dp.toPx()
        drawArc(
            color = iconColor,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.12f, size.height * 0.28f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.76f, size.height * 0.5f),
            style = Stroke(width = strokeWidth)
        )
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.18f, size.height * 0.55f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.82f, size.height * 0.55f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.32f, size.height * 0.82f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.68f, size.height * 0.82f),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
internal fun LoadingBlock() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(872.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 284.dp)
                .size(72.dp),
            color = Color(0xFF8B8B8B),
            strokeWidth = 4.dp
        )
        AssistantFloatingButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 30.dp, bottom = 28.dp)
        )
    }
}

@Composable
internal fun ErrorBlock(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(872.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 284.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CloudOffIcon(modifier = Modifier.size(64.dp))
            Text(
                text = "Error al cargar receta.",
                modifier = Modifier.padding(top = 40.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xB3000000)
            )
            if (message.isNotBlank()) {
                Text(
                    text = message,
                    modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0x80000000),
                    textAlign = TextAlign.Center
                )
            }
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 24.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA642))
            ) {
                Text(text = stringResource(R.string.home_retry), color = Color.White)
            }
        }
        AssistantFloatingButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 30.dp, bottom = 17.dp)
        )
    }
}

@Composable
internal fun CloudOffIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val iconColor = Color(0xFF6F6F6F)
        val stroke = Stroke(width = 2.dp.toPx())
        drawArc(
            color = iconColor,
            startAngle = 190f,
            sweepAngle = 210f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.22f, size.height * 0.18f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.5f, size.height * 0.42f),
            style = stroke
        )
        drawArc(
            color = iconColor,
            startAngle = 245f,
            sweepAngle = 210f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.55f, size.height * 0.29f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.32f, size.height * 0.32f),
            style = stroke
        )
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.18f, size.height * 0.72f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.78f, size.height * 0.72f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.18f, size.height * 0.57f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.18f, size.height * 0.88f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.18f, size.height * 0.88f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.72f, size.height * 0.88f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.05f, size.height * 0.05f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.95f, size.height * 0.95f),
            strokeWidth = 2.2.dp.toPx()
        )
    }
}

internal fun CookingEffort.toLabel(): String {
    return when (this) {
        CookingEffort.LOW -> "Pocas ganas: hasta 20 minutos"
        CookingEffort.MEDIUM -> "Mas o menos: 20 a 45 minutos"
        CookingEffort.HIGH -> "Muchas ganas: mas de 45 minutos"
    }
}

internal fun RecipeDifficulty.toLabel(): String {
    return when (this) {
        RecipeDifficulty.EASY -> "facil"
        RecipeDifficulty.MEDIUM -> "media"
        RecipeDifficulty.HARD -> "dificil"
    }
}

internal fun RecipeDifficulty.toColor(): Color {
    return when (this) {
        RecipeDifficulty.EASY -> Color(0xFF20C85A)
        RecipeDifficulty.MEDIUM -> Color(0xFFFFB000)
        RecipeDifficulty.HARD -> Color(0xFFFF1717)
    }
}

internal fun CookingMood.toLabel(): String {
    return when (this) {
        CookingMood.LOW -> "pocas ganas"
        CookingMood.MEDIUM -> "ganas medias"
        CookingMood.HIGH -> "con tiempo"
    }
}

internal fun EstimatedCost.toLabel(): String {
    return when (this) {
        EstimatedCost.LOW -> "barato"
        EstimatedCost.MEDIUM -> "medio"
        EstimatedCost.HIGH -> "caro"
    }
}

internal val BrandWine = Color(0xFF3C0006)
