package com.resumeai.pro.presentation.generation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.resumeai.pro.presentation.navigation.Screen
import com.resumeai.pro.ui.components.NeonButton
import com.resumeai.pro.ui.components.ParticleBackground
import com.resumeai.pro.ui.theme.*

/**
 * Full-screen resume generation experience with step-by-step progress display.
 * Shows animated stages as the resume is being built and AI processing occurs.
 */
@Composable
fun GenerationScreen(
    navController: NavController,
    resumeId: String,
    viewModel: GenerationViewModel = hiltViewModel()
) {
    LaunchedEffect(resumeId) {
        viewModel.startGeneration(resumeId)
    }

    val state = viewModel.state.collectAsState().value

    // Navigate to preview once complete
    LaunchedEffect(state.isComplete) {
        if (state.isComplete) {
            kotlinx.coroutines.delay(1200)
            navController.navigate(Screen.Preview.createRoute(resumeId)) {
                popUpTo(Screen.Generation.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace),
        contentAlignment = Alignment.Center
    ) {
        ParticleBackground(alpha = 0.3f)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            // Title
            Text(
                text = if (state.isComplete) "Resume Ready! 🎉" else "Building Your Resume",
                color = StarWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )

            // Progress ring
            if (!state.isComplete) {
                GenerationProgressRing(progress = state.progress)
            } else {
                // Success icon
                val infiniteTransition = rememberInfiniteTransition(label = "success_pulse")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                    label = "success_scale"
                )
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Complete",
                    tint = SuccessGreen,
                    modifier = Modifier.size(80.dp).scale(scale)
                )
            }

            // Current step label
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300))
            ) {
                Text(
                    text = state.currentStep,
                    color = MoonGray,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Steps checklist
            GenerationStepsList(steps = state.completedSteps, currentStep = state.stepIndex)

            // Preview button (shown on completion)
            if (state.isComplete) {
                NeonButton(
                    text = "Preview Resume →",
                    onClick = {
                        navController.navigate(Screen.Preview.createRoute(resumeId)) {
                            popUpTo(Screen.Generation.route) { inclusive = true }
                        }
                    }
                )
            } else {
                Text(
                    text = "Please wait – this may take up to 30 seconds",
                    color = MoonGray.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun GenerationProgressRing(progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "gen_progress"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(130.dp)) {
            drawArc(
                color = MoonGray.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(5.dp.toPx(), 5.dp.toPx()),
                size = Size(size.width - 10.dp.toPx(), size.height - 10.dp.toPx())
            )
            drawArc(
                brush = Brush.sweepGradient(colors = listOf(NebulaPurple, CyberCyan, NebulaPurple)),
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(5.dp.toPx(), 5.dp.toPx()),
                size = Size(size.width - 10.dp.toPx(), size.height - 10.dp.toPx())
            )
        }
        Text(
            text = "${(animatedProgress * 100).toInt()}%",
            color = StarWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun GenerationStepsList(@Suppress("UNUSED_PARAMETER") steps: List<String>, currentStep: Int) {
    val allSteps = listOf(
        "Analyzing Candidate Profile",
        "Extracting Skills",
        "Reading Job Description",
        "Calculating ATS Keywords",
        "Generating Resume Content",
        "Formatting Document",
        "Finalizing"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        allSteps.forEachIndexed { index, step ->
            val isDone = index < currentStep
            val isCurrent = index == currentStep

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Status indicator
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isDone -> SuccessGreen.copy(alpha = 0.3f)
                        isCurrent -> CyberCyan.copy(alpha = 0.3f)
                        else -> MoonGray.copy(alpha = 0.1f)
                    },
                    modifier = Modifier.size(8.dp)
                ) {}

                Text(
                    text = step,
                    color = when {
                        isDone -> SuccessGreen
                        isCurrent -> CyberCyan
                        else -> MoonGray.copy(alpha = 0.4f)
                    },
                    fontSize = 13.sp,
                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal
                )

                if (isCurrent) {
                    val infiniteTransition = rememberInfiniteTransition(label = "step_dot")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
                        label = "dot_alpha"
                    )
                    Text("•••", color = CyberCyan.copy(alpha = alpha), fontSize = 13.sp)
                }
            }
        }
    }
}
