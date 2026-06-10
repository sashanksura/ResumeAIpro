package com.resumeai.pro.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.resumeai.pro.ui.theme.CyberCyan
import com.resumeai.pro.ui.theme.NebulaPurple
import kotlinx.coroutines.delay
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var dx: Float,
    var dy: Float,
    val radius: Float,
    val color: Color,
    val alpha: Float
)

@Composable
fun ParticleBackground(
    modifier: Modifier = Modifier,
    alpha: Float = 1f
) {
    var width by remember { mutableStateOf(0f) }
    var height by remember { mutableStateOf(0f) }
    
    val particles = remember {
        List(40) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                dx = (Random.nextFloat() - 0.5f) * 0.002f,
                dy = (Random.nextFloat() - 0.5f) * 0.002f,
                radius = Random.nextFloat() * 4f + 2f,
                color = if (Random.nextBoolean()) NebulaPurple else CyberCyan,
                alpha = Random.nextFloat() * 0.3f + 0.3f
            )
        }
    }

    var tick by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(16) // ~60fps
            tick++
            particles.forEach { p ->
                p.x += p.dx
                p.y += p.dy
                
                if (p.x < 0f) p.x = 1f
                if (p.x > 1f) p.x = 0f
                if (p.y < 0f) p.y = 1f
                if (p.y > 1f) p.y = 0f
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        width = size.width
        height = size.height

        @Suppress("UNUSED_VARIABLE")
        val currentTick = tick // Read state to trigger recomposition
        particles.forEach { p ->
            drawCircle(
                color = p.color.copy(alpha = p.alpha * alpha),
                radius = p.radius,
                center = Offset(p.x * width, p.y * height)
            )
        }
    }
}
