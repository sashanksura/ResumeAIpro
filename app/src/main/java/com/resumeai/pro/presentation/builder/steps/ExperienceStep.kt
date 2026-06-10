package com.resumeai.pro.presentation.builder.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.resumeai.pro.domain.model.Experience
import com.resumeai.pro.presentation.builder.BuilderViewModel
import com.resumeai.pro.presentation.builder.BuilderUiState
import com.resumeai.pro.ui.components.GlassCard
import com.resumeai.pro.ui.components.NeonButton
import com.resumeai.pro.ui.theme.*

@Composable
fun ExperienceStep(viewModel: BuilderViewModel, navController: NavController) {
    val state = viewModel.formState.collectAsState().value
    val uiState = viewModel.uiState.collectAsState().value
    val aiLoadingIndex = viewModel.aiLoadingIndex.collectAsState().value
    
    val experiences = state.experiences

    // Local form state
    var company by remember { mutableStateOf("") }
    var jobTitle by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    // -1 means we are adding, otherwise represents the index of the experience being edited
    var editIndex by remember { mutableStateOf(-1) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Work Experience",
            color = CyberCyan,
            style = MaterialTheme.typography.titleLarge
        )

        // List of current experiences
        if (experiences.isNotEmpty()) {
            Text("Added Experiences", color = StarWhite, style = MaterialTheme.typography.titleMedium)
            experiences.forEachIndexed { index, exp ->
                val isCurrentAiLoading = aiLoadingIndex == index && uiState is BuilderUiState.Loading

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exp.jobTitle,
                                    color = StarWhite,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = exp.company,
                                    color = CyberCyan,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${exp.startDate} - ${exp.endDate}",
                                    color = MoonGray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = {
                                    company = exp.company
                                    jobTitle = exp.jobTitle
                                    startDate = exp.startDate
                                    endDate = exp.endDate
                                    description = exp.description
                                    editIndex = index
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CyberCyan)
                                }
                                IconButton(onClick = { viewModel.removeExperience(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                                }
                            }
                        }

                        if (exp.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = exp.description,
                                color = StarWhite.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // AI Enhancement options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isCurrentAiLoading) {
                                CircularProgressIndicator(color = CyberCyan, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("AI rewriting...", color = MoonGray, style = MaterialTheme.typography.bodySmall)
                            } else {
                                AssistChip(
                                    onClick = { viewModel.enhanceBulletPoints(index) },
                                    label = { Text("AI Enhance") },
                                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        labelColor = CyberCyan,
                                        leadingIconContentColor = CyberCyan,
                                        containerColor = CosmicCard
                                    ),
                                    border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f))
                                )
                                
                                AssistChip(
                                    onClick = { viewModel.rewriteExperience(index) },
                                    label = { Text("AI Rewrite") },
                                    leadingIcon = { Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        labelColor = NebulaPurple,
                                        leadingIconContentColor = NebulaPurple,
                                        containerColor = CosmicCard
                                    ),
                                    border = BorderStroke(1.dp, NebulaPurple.copy(alpha = 0.4f))
                                )
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = MoonGray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))
        }

        // Show AI Error Message if general to AI rewrite
        if (uiState is BuilderUiState.Error && aiLoadingIndex == -1) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = uiState.message,
                    color = ErrorRed,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Form for Adding / Editing Experience
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (editIndex >= 0) "Edit Experience" else "Add Experience",
                    color = StarWhite,
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = jobTitle,
                    onValueChange = { jobTitle = it },
                    label = { Text("Job Title (e.g. Software Engineer)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    singleLine = true
                )

                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("Company Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date") },
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date") },
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors,
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Responsibilities / Achievements") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    colors = textFieldColors,
                    maxLines = 6
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (editIndex >= 0) {
                        NeonButton(
                            text = "Cancel",
                            isOutlined = true,
                            onClick = {
                                company = ""
                                jobTitle = ""
                                startDate = ""
                                endDate = ""
                                description = ""
                                editIndex = -1
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    NeonButton(
                        text = if (editIndex >= 0) "Save Changes" else "Add Experience",
                        onClick = {
                            if (jobTitle.isNotBlank() && company.isNotBlank()) {
                                val exp = Experience(
                                    company = company,
                                    jobTitle = jobTitle,
                                    startDate = startDate,
                                    endDate = endDate,
                                    description = description
                                )
                                if (editIndex >= 0) {
                                    viewModel.updateExperience(editIndex, exp)
                                } else {
                                    viewModel.addExperience(exp)
                                }
                                
                                // Reset
                                company = ""
                                jobTitle = ""
                                startDate = ""
                                endDate = ""
                                description = ""
                                editIndex = -1
                            }
                        },
                        modifier = Modifier.weight(if (editIndex >= 0) 1f else 2f),
                        enabled = jobTitle.isNotBlank() && company.isNotBlank()
                    )
                }
            }
        }
    }
}
