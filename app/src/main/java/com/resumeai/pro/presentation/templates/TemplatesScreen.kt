package com.resumeai.pro.presentation.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.resumeai.pro.data.export.TemplateConfig
import com.resumeai.pro.data.export.TemplateEngine
import com.resumeai.pro.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TemplateViewModel @Inject constructor() : ViewModel() {
    val templates: List<TemplateConfig> = TemplateEngine.getAllTemplates()
    var selectedTemplateId by mutableStateOf("ats_modern")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    navController: NavController,
    viewModel: TemplateViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Templates", color = StarWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = StarWhite)
                    }
                },
                actions = {
                    Text(
                        text = "${viewModel.templates.size} templates",
                        color = MoonGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepSpace)
            )
        },
        containerColor = DeepSpace
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(padding).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(viewModel.templates) { template ->
                val isSelected = viewModel.selectedTemplateId == template.id
                val gradientColors = template.gradientColors.map { hex ->
                    try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { NebulaPurple }
                }.ifEmpty { listOf(NebulaPurple, CyberCyan) }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .then(
                            if (isSelected) Modifier.border(2.dp, CyberCyan, RoundedCornerShape(16.dp))
                            else Modifier.border(1.dp, MoonGray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        )
                        .clickable {
                            viewModel.selectedTemplateId = template.id
                        },
                    color = CosmicCard,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Gradient preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(gradientColors)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Mock resume layout lines
                            Column(
                                modifier = Modifier.fillMaxSize().padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Name line
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color.White.copy(alpha = 0.8f))
                                )
                                // Title line
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.4f)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color.White.copy(alpha = 0.5f))
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                // Section lines
                                repeat(4) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(1.5.dp))
                                            .background(Color.White.copy(alpha = 0.3f))
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                repeat(3) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.8f)
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(1.5.dp))
                                            .background(Color.White.copy(alpha = 0.2f))
                                    )
                                }
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = CyberCyan,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(6.dp)
                                        .size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = template.name,
                            color = if (isSelected) CyberCyan else StarWhite,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = template.description,
                            color = MoonGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
