package com.resumeai.pro.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.resumeai.pro.data.api.AIService
import com.resumeai.pro.ui.components.GlassCard
import com.resumeai.pro.ui.components.NeonButton
import com.resumeai.pro.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ConnectionTestState {
    object Idle : ConnectionTestState()
    object Testing : ConnectionTestState()
    data class Success(val message: String) : ConnectionTestState()
    data class Error(val message: String) : ConnectionTestState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val aiService: AIService
) : ViewModel() {
    private val _testState = MutableStateFlow<ConnectionTestState>(ConnectionTestState.Idle)
    val testState: StateFlow<ConnectionTestState> = _testState

    fun testConnection() {
        viewModelScope.launch {
            _testState.value = ConnectionTestState.Testing
            val result = aiService.generate(
                prompt = "Reply with exactly: CONNECTION OK",
                requestId = "connection_test",
                maxRetries = 0,
                maxTokens = 10
            )
            if (result.isSuccess) {
                _testState.value = ConnectionTestState.Success("✓ API connection successful!")
            } else {
                _testState.value = ConnectionTestState.Error(
                    result.exceptionOrNull()?.message ?: "Connection failed"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
    themeViewModel: com.resumeai.pro.ui.theme.ThemeViewModel = hiltViewModel()
) {
    val testState = viewModel.testState.collectAsState().value
    val isDarkTheme = themeViewModel.isDarkTheme.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = StarWhite, fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AI Configuration
            Text("AI Configuration", color = CyberCyan, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("API Provider", color = StarWhite, fontWeight = FontWeight.SemiBold)
                            Text("NVIDIA NIM", color = CyberCyan, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                    }

                    HorizontalDivider(color = MoonGray.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Model", color = StarWhite, fontWeight = FontWeight.SemiBold)
                            Text("DeepSeek V4 Pro", color = MoonGray, fontSize = 12.sp)
                            Text("+ Llama Fallbacks", color = CyberCyan.copy(alpha=0.7f), fontSize = 10.sp)
                        }
                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            color = SuccessGreen.copy(alpha = 0.2f)
                        ) {
                            Text("PRO", color = SuccessGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                        }
                    }

                    HorizontalDivider(color = MoonGray.copy(alpha = 0.2f))

                    Text(
                        text = "✓ API Key configured",
                        color = SuccessGreen,
                        fontSize = 12.sp
                    )

                    NeonButton(
                        text = when (testState) {
                            is ConnectionTestState.Testing -> "Testing..."
                            else -> "Test Connection"
                        },
                        isOutlined = true,
                        enabled = testState !is ConnectionTestState.Testing,
                        onClick = { viewModel.testConnection() }
                    )

                    when (testState) {
                        is ConnectionTestState.Success -> {
                            Text(testState.message, color = SuccessGreen, fontSize = 12.sp)
                        }
                        is ConnectionTestState.Error -> {
                            Text(testState.message, color = ErrorRed, fontSize = 12.sp)
                        }
                        else -> {}
                    }
                }
            }

            // AI Features
            Text("AI Features", color = CyberCyan, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FeatureRow("✨ Generate Summary")
                    FeatureRow("📝 Enhance Bullet Points")
                    FeatureRow("🔄 Rewrite Experience")
                    FeatureRow("💡 AI Suggest Skills")
                    FeatureRow("📊 ATS Optimization")
                    FeatureRow("🎯 Job Description Matching")
                    FeatureRow("📐 Resume Formatting AI")
                    FeatureRow("⬆️ Improve Resume")
                    FeatureRow("📋 Improve Projects")
                }
            }

            // Appearance
            Text("Appearance", color = CyberCyan, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Dark Theme", color = StarWhite)
                        Switch(
                            checked = isDarkTheme, 
                            onCheckedChange = { themeViewModel.toggleTheme() }, 
                            colors = SwitchDefaults.colors(checkedThumbColor = CyberCyan, checkedTrackColor = NebulaPurple)
                        )
                    }
                }
            }

            // About
            Text("About", color = CyberCyan, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("App Version", color = StarWhite)
                        Text("1.0.0", color = MoonGray)
                    }
                    HorizontalDivider(color = MoonGray.copy(alpha = 0.2f))
                    Text("ResumeAI Pro", color = StarWhite, fontWeight = FontWeight.SemiBold)
                    Text("AI-Powered Resume Builder", color = MoonGray, fontSize = 13.sp)
                    Text("Powered by NVIDIA NIM API", color = CyberCyan, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FeatureRow(feature: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
        Text(feature, color = StarWhite, fontSize = 13.sp)
    }
}
