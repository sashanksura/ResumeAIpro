package com.resumeai.pro.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import com.resumeai.pro.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.resumeai.pro.presentation.navigation.Screen
import com.resumeai.pro.ui.components.ParticleBackground
import com.resumeai.pro.ui.theme.CyberCyan
import com.resumeai.pro.ui.theme.DeepSpace
import com.resumeai.pro.ui.theme.NebulaPurple
import com.resumeai.pro.ui.theme.StarWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    var textState by remember { mutableStateOf("") }
    val fullText = "ResumeAI Pro"
    val subtitleAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.5f) }

    // Pulsing glow animation (reserved for future enhancement)

    LaunchedEffect(Unit) {
        logoScale.animateTo(1f, tween(800))
        for (i in fullText.indices) {
            textState = fullText.substring(0, i + 1)
            delay(80)
        }
        subtitleAlpha.animateTo(1f, tween(500))
        delay(1000)
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace),
        contentAlignment = Alignment.Center
    ) {
        ParticleBackground()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Shield logo
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(logoScale.value),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = textState,
                color = StarWhite,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Craft Your Future with AI",
                color = CyberCyan,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.alpha(subtitleAlpha.value)
            )
        }
    }
}


