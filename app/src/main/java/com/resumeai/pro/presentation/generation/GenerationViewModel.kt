package com.resumeai.pro.presentation.generation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GenerationState(
    val progress: Float = 0f,
    val currentStep: String = "Initializing...",
    val stepIndex: Int = 0,
    val completedSteps: List<String> = emptyList(),
    val isComplete: Boolean = false
)

/**
 * Manages the resume generation animation/progress sequence.
 * Simulates step-by-step progress to give the user visual feedback while the
 * resume is being processed. Actual processing happens in BuilderViewModel/PreviewViewModel;
 * this just drives the UI animation.
 */
@HiltViewModel
class GenerationViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(GenerationState())
    val state: StateFlow<GenerationState> = _state.asStateFlow()

    private val generationSteps = listOf(
        Triple("Analyzing Candidate Profile", 0.05f, 1200L),
        Triple("Extracting Skills", 0.15f, 1000L),
        Triple("Reading Job Description", 0.30f, 1200L),
        Triple("Calculating ATS Keywords", 0.45f, 1500L),
        Triple("Generating Resume Content", 0.65f, 2000L),
        Triple("Formatting Document", 0.82f, 1200L),
        Triple("Finalizing", 0.95f, 800L)
    )

    fun startGeneration(@Suppress("UNUSED_PARAMETER") resumeId: String) {
        if (_state.value.progress > 0f) return // Already started
        viewModelScope.launch {
            generationSteps.forEachIndexed { index, (label, progress, delayMs) ->
                _state.value = _state.value.copy(
                    currentStep = label,
                    stepIndex = index,
                    progress = progress
                )
                delay(delayMs)
            }
            // Mark complete
            _state.value = _state.value.copy(
                progress = 1f,
                currentStep = "Done!",
                stepIndex = generationSteps.size,
                isComplete = true
            )
        }
    }
}
