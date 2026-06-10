package com.resumeai.pro.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.resumeai.pro.ui.theme.CyberCyan
import com.resumeai.pro.ui.theme.NebulaPurple
import com.resumeai.pro.ui.theme.StarWhite

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    
    val surfaceModifier = modifier.shadow(
        elevation = 8.dp,
        shape = shape,
        spotColor = NebulaPurple,
        ambientColor = NebulaPurple
    )

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = surfaceModifier,
            shape = shape,
            color = Color.Transparent,
            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.25f))
        ) {
            Box(
                modifier = Modifier.background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            StarWhite.copy(alpha = 0.08f),
                            StarWhite.copy(alpha = 0.06f)
                        )
                    )
                ),
                content = content
            )
        }
    } else {
        Surface(
            modifier = surfaceModifier,
            shape = shape,
            color = Color.Transparent,
            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.25f))
        ) {
            Box(
                modifier = Modifier.background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            StarWhite.copy(alpha = 0.08f),
                            StarWhite.copy(alpha = 0.06f)
                        )
                    )
                ),
                content = content
            )
        }
    }
}
