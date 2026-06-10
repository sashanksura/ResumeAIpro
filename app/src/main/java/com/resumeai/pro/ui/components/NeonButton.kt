package com.resumeai.pro.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.resumeai.pro.ui.theme.CyberCyan
import com.resumeai.pro.ui.theme.NebulaPurple
import com.resumeai.pro.ui.theme.StarWhite

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isOutlined: Boolean = false,
    enabled: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val buttonModifier = modifier
        .scale(if (enabled && !isOutlined) scale else 1.0f)
        .shadow(
            elevation = if (enabled && !isOutlined) 12.dp else 0.dp,
            shape = RoundedCornerShape(24.dp),
            spotColor = CyberCyan,
            ambientColor = CyberCyan
        )

    Button(
        onClick = onClick,
        modifier = buttonModifier,
        enabled = enabled,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isOutlined) androidx.compose.ui.graphics.Color.Transparent else NebulaPurple,
            contentColor = if (isOutlined) CyberCyan else StarWhite,
            disabledContainerColor = com.resumeai.pro.ui.theme.MoonGray.copy(alpha = 0.3f),
            disabledContentColor = com.resumeai.pro.ui.theme.StarWhite.copy(alpha = 0.5f)
        ),
        border = if (isOutlined) androidx.compose.foundation.BorderStroke(1.dp, CyberCyan) else null,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        interactionSource = remember { MutableInteractionSource() }
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold
        )
    }
}
