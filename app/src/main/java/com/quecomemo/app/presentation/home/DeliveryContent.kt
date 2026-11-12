package com.quecomemo.app.presentation.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quecomemo.app.R

@Composable
internal fun DeliveryFlow(
    uiState: HomeUiState,
    onSelectPeopleMode: (DeliveryPeopleMode) -> Unit,
    onSelectBudget: (DeliveryBudget) -> Unit,
    onSpin: () -> Unit
) {
    AnimatedContent(
        targetState = uiState.deliveryStep,
        transitionSpec = {
            (fadeIn(tween(220)) + slideInHorizontally { it / 5 })
                .togetherWith(fadeOut(tween(150)) + slideOutHorizontally { -it / 6 })
        },
        label = "delivery-step-transition"
    ) { step ->
        when (step) {
            DeliveryStep.PEOPLE -> DeliveryPeopleStep(onSelectPeopleMode = onSelectPeopleMode)
            DeliveryStep.BUDGET -> DeliveryBudgetStep(onSelectBudget = onSelectBudget)
            DeliveryStep.SPIN -> DeliverySpinStep(
                peopleMode = uiState.deliveryPeopleMode,
                budget = uiState.deliveryBudget,
                result = uiState.deliveryResult,
                spinCount = uiState.deliverySpinCount,
                onSpin = onSpin
            )
        }
    }
}

@Composable
private fun DeliveryPeopleStep(
    onSelectPeopleMode: (DeliveryPeopleMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 116.dp, end = 32.dp, bottom = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\u00BFPara qui\u00E9n\npedimos?",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BrandWine,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        DeliveryOptionButton(
            title = "Para m\u00ED",
            description = "Opciones simples para una persona.",
            modifier = Modifier.padding(top = 55.dp),
            onClick = { onSelectPeopleMode(DeliveryPeopleMode.SOLO) }
        )
        DeliveryOptionButton(
            title = "Para compartir",
            description = "Opciones que rinden mejor para varios.",
            modifier = Modifier.padding(top = 24.dp),
            onClick = { onSelectPeopleMode(DeliveryPeopleMode.GROUP) }
        )
    }
}

@Composable
private fun DeliveryBudgetStep(
    onSelectBudget: (DeliveryBudget) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, top = 88.dp, end = 32.dp, bottom = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "\u00BFCu\u00E1nto quer\u00E9s\ngastar?",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BrandWine,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        DeliveryOptionButton(
            title = "Barato",
            description = "Algo tranqui, rendidor y sin romper la billetera.",
            modifier = Modifier.padding(top = 42.dp),
            onClick = { onSelectBudget(DeliveryBudget.LOW) }
        )
        DeliveryOptionButton(
            title = "Medio",
            description = "Un pedido normal, con un poco m\u00E1s de margen.",
            modifier = Modifier.padding(top = 18.dp),
            onClick = { onSelectBudget(DeliveryBudget.MEDIUM) }
        )
        DeliveryOptionButton(
            title = "Caro",
            description = "Antojo fuerte o algo para darse un gusto.",
            modifier = Modifier.padding(top = 18.dp),
            onClick = { onSelectBudget(DeliveryBudget.HIGH) }
        )
    }
}

@Composable
private fun DeliveryOptionButton(
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
            .padding(horizontal = 23.dp, vertical = 17.dp)
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
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}

@Composable
private fun DeliverySpinStep(
    peopleMode: DeliveryPeopleMode?,
    budget: DeliveryBudget?,
    result: String?,
    spinCount: Int,
    onSpin: () -> Unit
) {
    val wheelRotation by animateFloatAsState(
        targetValue = spinCount * 1080f,
        animationSpec = tween(durationMillis = 900),
        label = "delivery-roulette-rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 116.dp, bottom = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ruleta de delivery",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = BrandWine,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = listOfNotNull(peopleMode?.toLabel(), budget?.toLabel()).joinToString(" · "),
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xB3000000),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Image(
            painter = painterResource(id = R.drawable.logo_solo),
            contentDescription = "Ruleta de delivery",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(top = 29.dp)
                .size(238.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(119.dp),
                    clip = false
                )
                .rotate(wheelRotation)
        )
        Box(
            modifier = Modifier
                .padding(top = 45.dp)
                .width(266.dp)
                .height(45.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x33F1F1F1))
                .border(
                    width = 1.dp,
                    color = Color(0x29000000),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = result.orEmpty(),
                modifier = Modifier.padding(horizontal = 12.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BrandWine,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            modifier = Modifier
                .padding(top = 57.dp)
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

internal fun DeliveryPeopleMode.toLabel(): String {
    return when (this) {
        DeliveryPeopleMode.SOLO -> "Para m\u00ED"
        DeliveryPeopleMode.GROUP -> "Para compartir"
    }
}

internal fun DeliveryBudget.toLabel(): String {
    return when (this) {
        DeliveryBudget.LOW -> "Barato"
        DeliveryBudget.MEDIUM -> "Medio"
        DeliveryBudget.HIGH -> "Caro"
    }
}
