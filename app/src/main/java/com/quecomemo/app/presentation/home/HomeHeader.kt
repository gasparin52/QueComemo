package com.quecomemo.app.presentation.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quecomemo.app.R

@Composable
internal fun HomeHeader(
    displayName: String?,
    email: String?,
    onSignOut: () -> Unit
) {
    var isUserMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(54.dp)
            .background(Color.White)
            .graphicsLayer {
                shadowElevation = 8f
                shape = RoundedCornerShape(0.dp)
                clip = false
            }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.brand_logo_mark),
            contentDescription = "QueComemo",
            modifier = Modifier.size(36.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.brand_logo_full),
            contentDescription = "QueComemo",
            modifier = Modifier
                .padding(start = 12.dp)
                .height(18.dp)
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .clickable { isUserMenuOpen = true },
                contentAlignment = Alignment.Center
            ) {
                UserCircleIcon(modifier = Modifier.size(30.dp))
            }
            UserSessionMenu(
                expanded = isUserMenuOpen,
                displayName = displayName,
                email = email,
                onDismiss = { isUserMenuOpen = false },
                onSignOut = {
                    isUserMenuOpen = false
                    onSignOut()
                }
            )
        }
    }
}

@Composable
private fun UserCircleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = 2.1.dp.toPx())
        val iconColor = Color(0xFF2A2A2A)
        drawCircle(
            color = iconColor,
            radius = size.minDimension * 0.45f,
            style = stroke
        )
        drawCircle(
            color = iconColor,
            radius = size.minDimension * 0.13f,
            center = center.copy(y = size.height * 0.38f),
            style = stroke
        )
        drawArc(
            color = iconColor,
            startAngle = 205f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(
                x = size.width * 0.29f,
                y = size.height * 0.55f
            ),
            size = androidx.compose.ui.geometry.Size(
                width = size.width * 0.42f,
                height = size.height * 0.24f
            ),
            style = stroke
        )
    }
}

@Composable
private fun UserSessionMenu(
    expanded: Boolean,
    displayName: String?,
    email: String?,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .width(136.dp)
            .background(Color.White)
    ) {
        Text(
            text = displayName?.takeIf { it.isNotBlank() }
                ?: email?.substringBefore("@").orEmpty().ifBlank { "Usuario" },
            modifier = Modifier.padding(start = 13.dp, top = 7.dp, end = 13.dp, bottom = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF2A2A2A),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        email?.takeIf { it.isNotBlank() }?.let { userEmail ->
            Text(
                text = userEmail,
                modifier = Modifier.padding(start = 13.dp, end = 13.dp, bottom = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0x99000000),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        DropdownMenuItem(
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LogoutIcon(modifier = Modifier.size(13.dp))
                    Text(
                        text = "Cerrar sesi\u00F3n",
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xDE000000)
                    )
                }
            },
            onClick = onSignOut,
            contentPadding = PaddingValues(horizontal = 13.dp, vertical = 0.dp),
            modifier = Modifier.height(34.dp)
        )
    }
}

@Composable
private fun LogoutIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val iconColor = Color(0xFF6F6F6F)
        val strokeWidth = 1.4.dp.toPx()
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.18f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.82f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.18f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.55f, size.height * 0.18f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.82f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.55f, size.height * 0.82f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.43f, size.height * 0.5f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.92f, size.height * 0.5f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.72f, size.height * 0.3f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.92f, size.height * 0.5f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = iconColor,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.92f, size.height * 0.5f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.72f, size.height * 0.7f),
            strokeWidth = strokeWidth
        )
    }
}
