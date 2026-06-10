package com.resumeai.pro.presentation.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.resumeai.pro.presentation.navigation.Screen
import com.resumeai.pro.ui.components.*
import com.resumeai.pro.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val resumes = viewModel.recentResumes.collectAsState()

    // Animated gradient for hero text
    val infiniteTransition = rememberInfiniteTransition(label = "hero_gradient")
    @Suppress("UNUSED_VARIABLE")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )

    Box(modifier = Modifier.fillMaxSize().background(DeepSpace)) {
        ParticleBackground(alpha = 0.4f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Welcome Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Welcome back 👋",
                        color = StarWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ready to build something great?",
                        color = MoonGray,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Hero Text with gradient
            Text(
                text = "Build Your Dream\nResume with AI",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 34.sp,
                style = LocalTextStyle.current.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(NebulaPurple, CyberCyan, NebulaPurple)
                    )
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Actions Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.AutoAwesome,
                    title = "New Resume",
                    subtitle = "Start fresh",
                    onClick = { navController.navigate(Screen.Builder.route) }
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.FolderOpen,
                    title = "My Resumes",
                    subtitle = "View saved",
                    onClick = { navController.navigate(Screen.MyResumes.route) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Palette,
                    title = "Templates",
                    subtitle = "10 designs",
                    onClick = { navController.navigate(Screen.Templates.route) }
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Settings,
                    title = "Settings",
                    subtitle = "API & more",
                    onClick = { navController.navigate(Screen.Settings.route) }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Recent Resumes
            Text(
                text = "Recent Resumes",
                color = StarWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (resumes.value.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "No resumes",
                            tint = MoonGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No resumes yet",
                            color = MoonGray,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to create your first resume",
                            color = MoonGray.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(resumes.value) { resume ->
                        HolographicCard(
                            modifier = Modifier.width(160.dp).height(200.dp),
                            onClick = { navController.navigate(Screen.Preview.createRoute(resume.id)) }
                        ) {
                            Column {
                                Text(
                                    text = resume.name,
                                    color = StarWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = resume.templateId,
                                    color = CyberCyan,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                LinearProgressIndicator(
                                    progress = { resume.completionPercent / 100f },
                                    modifier = Modifier.fillMaxWidth().height(4.dp),
                                    color = CyberCyan,
                                    trackColor = MoonGray.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${resume.completionPercent}% complete",
                                    color = MoonGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // FAB
        FloatingActionButton(
            onClick = { navController.navigate(Screen.Builder.route) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = NebulaPurple,
            contentColor = StarWhite,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "New Resume")
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = modifier.height(120.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = CyberCyan,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = title,
                    color = StarWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = MoonGray,
                    fontSize = 11.sp
                )
            }
        }
    }
}
