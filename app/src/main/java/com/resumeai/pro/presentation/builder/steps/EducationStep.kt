package com.resumeai.pro.presentation.builder.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.resumeai.pro.domain.model.Education
import com.resumeai.pro.presentation.builder.BuilderViewModel
import com.resumeai.pro.ui.components.GlassCard
import com.resumeai.pro.ui.components.NeonButton
import com.resumeai.pro.ui.theme.*

@Composable
fun EducationStep(viewModel: BuilderViewModel) {
    val state = viewModel.formState.collectAsState().value
    val educations = state.educations

    var institution by remember { mutableStateOf("") }
    var degree by remember { mutableStateOf("") }
    var fieldOfStudy by remember { mutableStateOf("") }
    var startYear by remember { mutableStateOf("") }
    var endYear by remember { mutableStateOf("") }
    var gpa by remember { mutableStateOf("") }

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
            text = "Education Details",
            color = CyberCyan,
            style = MaterialTheme.typography.titleLarge
        )

        // List of current educations
        if (educations.isNotEmpty()) {
            Text("Added Education", color = StarWhite, style = MaterialTheme.typography.titleMedium)
            educations.forEachIndexed { index, edu ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${edu.degree} in ${edu.fieldOfStudy}",
                                color = StarWhite,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = edu.institution,
                                color = CyberCyan,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${edu.startYear} - ${edu.endYear}" + if (edu.gpa.isNotEmpty()) " | GPA: ${edu.gpa}" else "",
                                color = MoonGray,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = { viewModel.removeEducation(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                        }
                    }
                }
            }
            HorizontalDivider(color = MoonGray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))
        }

        // Add New Education Form
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add Education",
                    color = StarWhite,
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = institution,
                    onValueChange = { institution = it },
                    label = { Text("School / Institution") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    singleLine = true
                )

                OutlinedTextField(
                    value = degree,
                    onValueChange = { degree = it },
                    label = { Text("Degree (e.g. Bachelor of Science)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    singleLine = true
                )

                OutlinedTextField(
                    value = fieldOfStudy,
                    onValueChange = { fieldOfStudy = it },
                    label = { Text("Field of Study (e.g. Computer Science)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startYear,
                        onValueChange = { startYear = it },
                        label = { Text("Start Year") },
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endYear,
                        onValueChange = { endYear = it },
                        label = { Text("End Year") },
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors,
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = gpa,
                    onValueChange = { gpa = it },
                    label = { Text("GPA (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    singleLine = true
                )

                NeonButton(
                    text = "Add Education",
                    onClick = {
                        if (institution.isNotBlank() && degree.isNotBlank()) {
                            viewModel.addEducation(
                                Education(
                                    institution = institution,
                                    degree = degree,
                                    fieldOfStudy = fieldOfStudy,
                                    startYear = startYear,
                                    endYear = endYear,
                                    gpa = gpa
                                )
                            )
                            // Reset form fields
                            institution = ""
                            degree = ""
                            fieldOfStudy = ""
                            startYear = ""
                            endYear = ""
                            gpa = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = institution.isNotBlank() && degree.isNotBlank()
                )
            }
        }
    }
}
