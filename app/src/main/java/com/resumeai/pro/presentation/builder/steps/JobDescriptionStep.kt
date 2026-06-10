package com.resumeai.pro.presentation.builder.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.resumeai.pro.presentation.builder.BuilderViewModel
import com.resumeai.pro.presentation.builder.BuilderUiState
import com.resumeai.pro.ui.components.GlassCard
import com.resumeai.pro.ui.components.NeonButton
import com.resumeai.pro.ui.theme.*

@Composable
fun JobDescriptionStep(viewModel: BuilderViewModel) {
    val state = viewModel.formState.collectAsState().value
    val uiState = viewModel.uiState.collectAsState().value
    
    var urlInput by remember { mutableStateOf(state.jobDescriptionUrl) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Target Job Description",
            color = CyberCyan,
            style = MaterialTheme.typography.titleLarge
        )
        
        Text(
            text = "Providing a job description helps the AI optimize your resume for ATS systems and job matching.",
            color = MoonGray,
            style = MaterialTheme.typography.bodyMedium
        )

        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyberCyan,
            unfocusedBorderColor = MoonGray.copy(alpha = 0.4f),
            focusedTextColor = StarWhite,
            unfocusedTextColor = StarWhite,
            focusedContainerColor = CosmicCard,
            unfocusedContainerColor = CosmicCard,
            cursorColor = CyberCyan,
            focusedLabelColor = CyberCyan,
            unfocusedLabelColor = MoonGray
        )

        // URL Extractor Section
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Import from Job Posting URL",
                    color = StarWhite,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("Job Board URL") },
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors,
                        singleLine = true
                    )
                    
                    NeonButton(
                        text = "Extract",
                        onClick = { viewModel.extractJobDescription(urlInput) },
                        enabled = urlInput.isNotBlank() && uiState !is BuilderUiState.Loading
                    )
                }
            }
        }

        // Status messages for Extraction
        when (uiState) {
            is BuilderUiState.ExtractionProgressState -> {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = CyberCyan, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Extracting: ${uiState.progress.message}",
                            color = CyberCyan,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            is BuilderUiState.Error -> {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = uiState.message,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            is BuilderUiState.AiResult -> {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = uiState.result,
                            color = SuccessGreen,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            else -> {}
        }

        // Manual Text input
        OutlinedTextField(
            value = state.jobDescription,
            onValueChange = { viewModel.updateJobDescription(it) },
            label = { Text("Paste Job Description Details Here") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp),
            colors = textFieldColors,
            maxLines = 15
        )
    }
}
