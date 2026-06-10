package com.resumeai.pro.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.resumeai.pro.ui.theme.CosmicCard
import com.resumeai.pro.ui.theme.NebulaPurple

import androidx.compose.foundation.layout.width

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 20.dp,
    width: Dp? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by infiniteTransition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            CosmicCard,
            NebulaPurple.copy(alpha = 0.2f),
            CosmicCard
        ),
        start = Offset(translateAnim, 0f),
        end = Offset(translateAnim + 500f, 0f)
    )

    var boxModifier = modifier
        .height(height)
        .clip(RoundedCornerShape(8.dp))
        .background(brush)
        
    if (width != null) {
        boxModifier = boxModifier.width(width)
    } else {
        boxModifier = boxModifier.fillMaxWidth()
    }

    Box(modifier = boxModifier)
}
