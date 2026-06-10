package com.resumeai.pro.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.resumeai.pro.ui.theme.CosmicCard
import com.resumeai.pro.ui.theme.CyberCyan
import com.resumeai.pro.ui.theme.NebulaPurple

@Composable
fun HolographicCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 16.dp else 8.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "holo_elevation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "holo_border")
    val borderOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "holo_border_anim"
    )

    val borderBrush = Brush.linearGradient(
        colors = listOf(NebulaPurple, CyberCyan, NebulaPurple),
        start = Offset(borderOffset, borderOffset),
        end = Offset(borderOffset + 500f, borderOffset + 500f)
    )

    val shape = RoundedCornerShape(16.dp)

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.shadow(elevation, shape, spotColor = CyberCyan),
            shape = shape,
            color = CosmicCard,
            interactionSource = interactionSource
        ) {
            Box(
                modifier = Modifier
                    .border(2.dp, borderBrush, shape)
                    .clip(shape)
                    .background(CosmicCard)
                    .padding(16.dp),
                content = content
            )
        }
    } else {
        Surface(
            modifier = modifier.shadow(elevation, shape, spotColor = CyberCyan),
            shape = shape,
            color = CosmicCard
        ) {
            Box(
                modifier = Modifier
                    .border(2.dp, borderBrush, shape)
                    .clip(shape)
                    .background(CosmicCard)
                    .padding(16.dp),
                content = content
            )
        }
    }
}
