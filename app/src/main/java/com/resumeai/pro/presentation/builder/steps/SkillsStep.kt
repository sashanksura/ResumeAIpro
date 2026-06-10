package com.resumeai.pro.presentation.builder.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.resumeai.pro.domain.model.Skill
import com.resumeai.pro.presentation.builder.BuilderViewModel
import com.resumeai.pro.presentation.builder.BuilderUiState
import com.resumeai.pro.ui.components.GlassCard
import com.resumeai.pro.ui.components.NeonButton
import com.resumeai.pro.ui.theme.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SkillsStep(viewModel: BuilderViewModel) {
    val state = viewModel.formState.collectAsState().value
    val uiState = viewModel.uiState.collectAsState().value
    
    val skills = state.skills
    val suggestedSkills = state.suggestedSkills

    var skillName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Technical") }
    var proficiency by remember { mutableStateOf(0.5f) }

    val categories = listOf("Technical", "Soft Skill", "Language", "Tool/Framework")

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
            text = "Skills & Expertise",
            color = CyberCyan,
            style = MaterialTheme.typography.titleLarge
        )

        // AI Skill Suggestions Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Skill Suggestions",
                        color = StarWhite,
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    if (uiState is BuilderUiState.Loading) {
                        CircularProgressIndicator(color = CyberCyan, modifier = Modifier.size(20.dp))
                    } else {
                        IconButton(onClick = { viewModel.suggestSkills() }) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Suggest Skills", tint = CyberCyan)
                        }
                    }
                }
                
                Text(
                    text = "Let AI analyze your target job description and experiences to suggest missing skills.",
                    color = MoonGray,
                    style = MaterialTheme.typography.bodySmall
                )

                if (suggestedSkills.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        suggestedSkills.forEach { skill ->
                            InputChip(
                                selected = false,
                                onClick = { viewModel.acceptSuggestedSkill(skill) },
                                label = { Text(skill) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { viewModel.dismissSuggestedSkill(skill) },
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Dismiss",
                                            tint = MoonGray,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                },
                                colors = InputChipDefaults.inputChipColors(
                                    labelColor = CyberCyan,
                                    containerColor = CosmicCard
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = CyberCyan.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
            }
        }

        // List of current skills
        if (skills.isNotEmpty()) {
            Text("Added Skills", color = StarWhite, style = MaterialTheme.typography.titleMedium)
            skills.forEachIndexed { index, skill ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(skill.name, color = StarWhite, style = MaterialTheme.typography.titleMedium)
                                Text(skill.category, color = MoonGray, style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { viewModel.removeSkill(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Proficiency:", color = MoonGray, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(80.dp))
                            Slider(
                                value = skill.proficiency,
                                onValueChange = { viewModel.updateSkillLevel(index, it) },
                                colors = SliderDefaults.colors(
                                    thumbColor = CyberCyan,
                                    activeTrackColor = CyberCyan,
                                    inactiveTrackColor = MoonGray.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            HorizontalDivider(color = MoonGray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))
        }

        // Add New Skill Form
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add Skill Manually",
                    color = StarWhite,
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = skillName,
                    onValueChange = { skillName = it },
                    label = { Text("Skill Name (e.g. Kotlin)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    singleLine = true
                )

                // Simple Dropdown/Selection for Category
                Text("Category", color = MoonGray, style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = category == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { category = cat },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                labelColor = if (isSelected) StarWhite else MoonGray,
                                selectedLabelColor = StarWhite,
                                selectedContainerColor = NebulaPurple,
                                containerColor = CosmicCard
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) NebulaPurple else MoonGray.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Proficiency", color = MoonGray, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(80.dp))
                    Slider(
                        value = proficiency,
                        onValueChange = { proficiency = it },
                        colors = SliderDefaults.colors(
                            thumbColor = CyberCyan,
                            activeTrackColor = CyberCyan,
                            inactiveTrackColor = MoonGray.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                NeonButton(
                    text = "Add Skill",
                    onClick = {
                        if (skillName.isNotBlank()) {
                            viewModel.addSkill(
                                Skill(
                                    name = skillName,
                                    category = category,
                                    proficiency = proficiency
                                )
                            )
                            skillName = ""
                            proficiency = 0.5f
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = skillName.isNotBlank()
                )
            }
        }
    }
}
