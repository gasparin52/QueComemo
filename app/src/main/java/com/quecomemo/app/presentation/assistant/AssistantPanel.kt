package com.quecomemo.app.presentation.assistant

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quecomemo.app.R
import com.quecomemo.app.domain.model.AssistantMessage
import com.quecomemo.app.domain.model.AssistantMessageAuthor
import com.quecomemo.app.domain.model.Recipe
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AssistantOrange = Color(0xFFFFA642)
private val AssistantHeaderOrange = Color(0xFFFFA749)
private val UserBubbleColor = Color(0xFFFFCF9C)

@Composable
fun RecipeAssistantDialog(
    recipe: Recipe,
    onDismiss: () -> Unit,
    viewModel: AssistantViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(recipe.id) {
        viewModel.startConversation(recipe)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        AssistantPanel(
            uiState = uiState,
            onInputChange = viewModel::updateInput,
            onSend = viewModel::sendMessage,
            onRetry = viewModel::retry,
            onDismiss = onDismiss
        )
    }
}

@Composable
fun AssistantPanel(
    uiState: AssistantUiState,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x47000000))
            .clickable(onClick = onDismiss)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
                    clip = false
                )
                .background(Color.White)
                // Evita que un toque dentro del panel lo cierre.
                .clickable(enabled = false, onClick = {})
                .navigationBarsPadding()
                .padding(bottom = 8.dp)
        ) {
            AssistantHeader(onDismiss = onDismiss)

            AssistantMessageList(
                uiState = uiState,
                onRetry = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            AssistantInputBar(
                value = uiState.input,
                canSend = uiState.canSend,
                onValueChange = onInputChange,
                onSend = onSend
            )
            Text(
                text = "La IA puede cometer errores. Verifica la informacion importante.",
                modifier = Modifier.padding(start = 29.dp, top = 5.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Color(0x6B000000)
            )
        }
    }
}

@Composable
private fun AssistantHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
            .background(
                color = AssistantHeaderOrange,
                shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
            )
            .padding(start = 20.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Asistente de IA",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "x",
            modifier = Modifier
                .clickable(onClick = onDismiss)
                .padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun AssistantMessageList(
    uiState: AssistantUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val itemCount = uiState.messages.size + 1 +
        (if (uiState.isSending) 1 else 0) +
        (if (uiState.errorMessage != null) 1 else 0)

    LaunchedEffect(itemCount) {
        listState.animateScrollToItem(itemCount - 1)
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "welcome") {
            AssistantMessageBubble(
                text = "Hola! Preguntame lo que quieras sobre ${uiState.recipe?.name ?: "esta receta"}: " +
                    "reemplazos, tiempos, tecnicas o como dejarla mejor.",
                time = null,
                isUser = false
            )
        }
        items(uiState.messages) { message ->
            AssistantMessageBubble(
                text = message.text,
                time = message.formattedTime(),
                isUser = message.author == AssistantMessageAuthor.USER
            )
        }
        if (uiState.isSending) {
            item(key = "typing") {
                Row(
                    modifier = Modifier.padding(start = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = AssistantOrange,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Pensando...",
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0x99000000)
                    )
                }
            }
        }
        uiState.errorMessage?.let { message ->
            item(key = "error") {
                AssistantErrorRow(message = message, onRetry = onRetry)
            }
        }
    }
}

@Composable
private fun AssistantErrorRow(
    message: String,
    onRetry: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFFEDED))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF9A2B2B)
        )
        Text(
            text = "Reintentar",
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onRetry)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = AssistantOrange
        )
    }
}

@Composable
private fun AssistantMessageBubble(
    text: String,
    time: String?,
    isUser: Boolean
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(10.dp),
                    clip = false
                )
                .clip(RoundedCornerShape(10.dp))
                .background(if (isUser) UserBubbleColor else Color.White)
                .border(
                    width = if (isUser) 0.dp else 1.dp,
                    color = Color(0x40000000),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(start = 13.dp, top = 8.dp, end = 13.dp, bottom = 6.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 15.sp),
                color = Color.Black
            )
            if (time != null) {
                Text(
                    text = time,
                    modifier = Modifier.align(Alignment.End),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = Color(0xB3000000)
                )
            }
        }
    }
}

@Composable
private fun AssistantInputBar(
    value: String,
    canSend: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 13.dp, end = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .border(
                    width = 1.dp,
                    color = Color(0x75989797),
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = "Escribi tu pregunta",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0x75989797)
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() })
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(if (canSend) AssistantOrange else Color(0xFFFFD2A1))
                .clickable(enabled = canSend, onClick = onSend),
            contentAlignment = Alignment.Center
        ) {
            SendIcon(modifier = Modifier.size(21.dp))
        }
    }
}

@Composable
private fun SendIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 2.dp.toPx()
        val points = listOf(
            Offset(size.width * 0.18f, size.height * 0.18f) to Offset(size.width * 0.86f, size.height * 0.5f),
            Offset(size.width * 0.86f, size.height * 0.5f) to Offset(size.width * 0.18f, size.height * 0.82f),
            Offset(size.width * 0.18f, size.height * 0.18f) to Offset(size.width * 0.36f, size.height * 0.5f),
            Offset(size.width * 0.36f, size.height * 0.5f) to Offset(size.width * 0.18f, size.height * 0.82f)
        )
        points.forEach { (start, end) ->
            drawLine(color = Color.White, start = start, end = end, strokeWidth = strokeWidth)
        }
    }
}

@Composable
fun AssistantFloatingButton(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Image(
        painter = painterResource(id = R.drawable.assistant_chef),
        contentDescription = "Asistente IA",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(65.dp)
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(100.dp),
                clip = false
            )
            .clip(RoundedCornerShape(100.dp))
            .background(AssistantOrange)
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
    )
}

private fun AssistantMessage.formattedTime(): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(sentAtMillis))
}
