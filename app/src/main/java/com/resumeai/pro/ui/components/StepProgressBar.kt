package com.resumeai.pro.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resumeai.pro.ui.theme.*

@Composable
fun StepProgressBar(
    steps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (i in 0 until steps) {
            val isActive = i == currentStep
            val isCompleted = i < currentStep

            val bgColor by animateColorAsState(
                targetValue = if (isActive || isCompleted) NebulaPurple else Color.Transparent,
                animationSpec = tween(300),
                label = "bgColor"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isActive || isCompleted) NebulaPurple else MoonGray,
                animationSpec = tween(300),
                label = "borderColor"
            )
            val elevation by animateDpAsState(
                targetValue = if (isActive) 8.dp else 0.dp,
                animationSpec = tween(300),
                label = "elevation"
            )

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .shadow(elevation, CircleShape, spotColor = CyberCyan)
                    .background(bgColor, CircleShape)
                    .border(2.dp, borderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = StarWhite,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = (i + 1).toString(),
                        color = if (isActive) StarWhite else MoonGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            if (i < steps - 1) {
                val lineColor by animateColorAsState(
                    targetValue = if (isCompleted) NebulaPurple else MoonGray.copy(alpha = 0.3f),
                    animationSpec = tween(300),
                    label = "lineColor"
                )
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    drawLine(
                        color = lineColor,
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }
    }
}
