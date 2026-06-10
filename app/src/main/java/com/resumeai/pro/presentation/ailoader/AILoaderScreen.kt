package com.resumeai.pro.presentation.ailoader

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.resumeai.pro.ui.components.ParticleBackground
import com.resumeai.pro.ui.theme.CyberCyan
import com.resumeai.pro.ui.theme.DeepSpace
import com.resumeai.pro.ui.theme.NebulaPurple
import com.resumeai.pro.ui.theme.StarWhite
import kotlinx.coroutines.delay

@Suppress("UNUSED_PARAMETER")
@Composable
fun AILoaderScreen(
    navController: NavController,
    loaderType: String
) {
    val messages = listOf(
        "Analyzing your experience…",
        "Identifying key achievements…",
        "Applying ATS optimization…",
        "Crafting powerful language…",
        "Finalizing your content…"
    )
    
    var currentMessageIndex by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        while(true) {
            delay(1800)
            currentMessageIndex = (currentMessageIndex + 1) % messages.size
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val colorPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "color"
    )

    val color = if (colorPhase < 0.5f) NebulaPurple else CyberCyan

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace),
        contentAlignment = Alignment.Center
    ) {
        ParticleBackground()

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(scale)
                    .background(color, CircleShape)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = messages[currentMessageIndex],
                color = StarWhite
            )
        }
    }
}
