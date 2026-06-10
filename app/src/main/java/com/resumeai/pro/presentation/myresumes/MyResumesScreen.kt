package com.resumeai.pro.presentation.myresumes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.resumeai.pro.domain.model.Resume
import com.resumeai.pro.domain.repository.ResumeRepository
import com.resumeai.pro.presentation.navigation.Screen
import com.resumeai.pro.ui.components.GlassCard
import com.resumeai.pro.ui.components.HolographicCard
import com.resumeai.pro.ui.components.NeonButton
import com.resumeai.pro.ui.components.ParticleBackground
import com.resumeai.pro.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyResumesViewModel @Inject constructor(
    private val repository: ResumeRepository
) : ViewModel() {
    val resumes: StateFlow<List<Resume>> = repository.getAllResumes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteResume(id: String) {
        viewModelScope.launch { repository.deleteResume(id) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyResumesScreen(
    navController: NavController,
    viewModel: MyResumesViewModel = hiltViewModel()
) {
    val resumes = viewModel.resumes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Resumes", color = StarWhite, fontWeight = FontWeight.Bold) },
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
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(DeepSpace)) {
            ParticleBackground(alpha = 0.2f)

            if (resumes.value.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = MoonGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No resumes yet", color = StarWhite, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Start building your first resume", color = MoonGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    NeonButton(text = "Create Resume", onClick = { navController.navigate(Screen.Builder.route) })
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(resumes.value) { resume ->
                        HolographicCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { navController.navigate(Screen.Preview.createRoute(resume.id)) }
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(resume.name, color = StarWhite, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(resume.templateId, color = CyberCyan, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { resume.completionPercent / 100f },
                                        modifier = Modifier.fillMaxWidth().height(4.dp),
                                        color = CyberCyan,
                                        trackColor = MoonGray.copy(alpha = 0.3f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${resume.completionPercent}% complete", color = MoonGray, fontSize = 11.sp)
                                }
                                IconButton(onClick = { viewModel.deleteResume(resume.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
