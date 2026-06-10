package com.resumeai.pro.presentation.builder.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.resumeai.pro.domain.model.Project
import com.resumeai.pro.presentation.builder.BuilderViewModel
import com.resumeai.pro.ui.components.GlassCard
import com.resumeai.pro.ui.components.NeonButton
import com.resumeai.pro.ui.theme.*

@Composable
fun ProjectsStep(viewModel: BuilderViewModel) {
    val state = viewModel.formState.collectAsState().value
    val projects = state.projects

    var projectName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var technologies by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }

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
            text = "Personal & Professional Projects",
            color = CyberCyan,
            style = MaterialTheme.typography.titleLarge
        )

        // List of current projects
        if (projects.isNotEmpty()) {
            Text("Added Projects", color = StarWhite, style = MaterialTheme.typography.titleMedium)
            projects.forEachIndexed { index, proj ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = proj.name,
                                color = StarWhite,
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (proj.technologies.isNotEmpty()) {
                                Text(
                                    text = "Tech: ${proj.technologies}",
                                    color = CyberCyan,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (proj.link.isNotEmpty()) {
                                Text(
                                    text = proj.link,
                                    color = NebulaPurple,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (proj.description.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = proj.description,
                                    color = StarWhite.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = {
                                projectName = proj.name
                                description = proj.description
                                technologies = proj.technologies
                                link = proj.link
                                editIndex = index
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CyberCyan)
                            }
                            IconButton(onClick = { viewModel.removeProject(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = MoonGray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))
        }

        // Add New Project Form
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (editIndex >= 0) "Edit Project" else "Add Project",
                    color = StarWhite,
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = { Text("Project Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    singleLine = true
                )

                OutlinedTextField(
                    value = technologies,
                    onValueChange = { technologies = it },
                    label = { Text("Technologies Used (e.g. Kotlin, Compose, Room)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    singleLine = true
                )

                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("Project Link (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Project Description") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    colors = textFieldColors,
                    maxLines = 5
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
                                projectName = ""
                                description = ""
                                technologies = ""
                                link = ""
                                editIndex = -1
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    NeonButton(
                        text = if (editIndex >= 0) "Save Changes" else "Add Project",
                        onClick = {
                            if (projectName.isNotBlank()) {
                                val proj = Project(
                                    name = projectName,
                                    description = description,
                                    technologies = technologies,
                                    link = link
                                )
                                if (editIndex >= 0) {
                                    viewModel.updateProject(editIndex, proj)
                                } else {
                                    viewModel.addProject(proj)
                                }

                                // Reset
                                projectName = ""
                                description = ""
                                technologies = ""
                                link = ""
                                editIndex = -1
                            }
                        },
                        modifier = Modifier.weight(if (editIndex >= 0) 1f else 2f),
                        enabled = projectName.isNotBlank()
                    )
                }
            }
        }
    }
}
