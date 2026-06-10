package com.resumeai.pro.presentation.builder

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.resumeai.pro.presentation.builder.steps.*
import com.resumeai.pro.presentation.navigation.Screen
import com.resumeai.pro.ui.components.NeonButton
import com.resumeai.pro.ui.components.StepProgressBar
import com.resumeai.pro.ui.theme.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuilderScreen(
    navController: NavController,
    viewModel: BuilderViewModel = hiltViewModel()
) {
    val state = viewModel.formState.collectAsState().value
    val uiState = viewModel.uiState.collectAsState().value

    // Fix: use LaunchedEffect to avoid navigation during composition
    LaunchedEffect(uiState) {
        if (uiState is BuilderUiState.Saved) {
            // Navigate to Generation screen for animated experience, which then leads to Preview
            navController.navigate(Screen.Generation.createRoute(uiState.resumeId)) {
                popUpTo(Screen.Builder.route) { inclusive = true }
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                viewModel.saveDraftNow()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Build Resume", color = StarWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = StarWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepSpace)
            )
        },
        containerColor = DeepSpace
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(DeepSpace)) {
            StepProgressBar(steps = 6, currentStep = state.currentStep)

            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = state.currentStep,
                    transitionSpec = {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                    },
                    label = "builder_steps"
                ) { targetStep ->
                    when (targetStep) {
                        0 -> PersonalInfoStep(viewModel)
                        1 -> JobDescriptionStep(viewModel)
                        2 -> EducationStep(viewModel)
                        3 -> ExperienceStep(viewModel, navController)
                        4 -> SkillsStep(viewModel)
                        5 -> ProjectsStep(viewModel)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (state.currentStep > 0) {
                    NeonButton(text = "Back", isOutlined = true, onClick = { viewModel.previousStep() })
                } else {
                    Spacer(modifier = Modifier.width(64.dp))
                }

                if (state.currentStep < 5) {
                    NeonButton(text = "Next", onClick = { viewModel.nextStep() })
                } else {
                    NeonButton(text = "Preview Resume", onClick = { viewModel.saveResume() })
                }
            }
        }
    }
}
