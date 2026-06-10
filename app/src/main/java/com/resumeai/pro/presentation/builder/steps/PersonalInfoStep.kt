package com.resumeai.pro.presentation.builder.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.resumeai.pro.presentation.builder.BuilderViewModel
import com.resumeai.pro.ui.theme.*

@Composable
fun PersonalInfoStep(viewModel: BuilderViewModel) {
    val state = viewModel.formState.collectAsState().value
    val info = state.personalInfo

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Personal Information",
            color = CyberCyan,
            style = MaterialTheme.typography.titleLarge
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

        OutlinedTextField(
            value = info.fullName,
            onValueChange = { viewModel.updatePersonalInfo(info.copy(fullName = it)) },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors,
            singleLine = true
        )

        OutlinedTextField(
            value = info.jobTitle,
            onValueChange = { viewModel.updatePersonalInfo(info.copy(jobTitle = it)) },
            label = { Text("Target Job Title") },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors,
            singleLine = true
        )

        OutlinedTextField(
            value = info.email,
            onValueChange = { viewModel.updatePersonalInfo(info.copy(email = it)) },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors,
            singleLine = true
        )

        OutlinedTextField(
            value = info.phone,
            onValueChange = { viewModel.updatePersonalInfo(info.copy(phone = it)) },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors,
            singleLine = true
        )

        OutlinedTextField(
            value = info.location,
            onValueChange = { viewModel.updatePersonalInfo(info.copy(location = it)) },
            label = { Text("Location (City, Country)") },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors,
            singleLine = true
        )

        OutlinedTextField(
            value = info.linkedinUrl,
            onValueChange = { viewModel.updatePersonalInfo(info.copy(linkedinUrl = it)) },
            label = { Text("LinkedIn URL") },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors,
            singleLine = true
        )

        OutlinedTextField(
            value = info.githubUrl,
            onValueChange = { viewModel.updatePersonalInfo(info.copy(githubUrl = it)) },
            label = { Text("GitHub URL") },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors,
            singleLine = true
        )

        OutlinedTextField(
            value = info.portfolioUrl,
            onValueChange = { viewModel.updatePersonalInfo(info.copy(portfolioUrl = it)) },
            label = { Text("Portfolio URL") },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors,
            singleLine = true
        )

        OutlinedTextField(
            value = info.photoUri ?: "",
            onValueChange = { viewModel.updatePhotoUri(it) },
            label = { Text("Photo URI / URL (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors,
            singleLine = true
        )
    }
}
