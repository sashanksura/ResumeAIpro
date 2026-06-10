package com.resumeai.pro.presentation.preview

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import com.resumeai.pro.ui.components.HolographicCard
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.resumeai.pro.ui.components.GlassCard
import com.resumeai.pro.ui.components.NeonButton
import com.resumeai.pro.ui.components.ParticleBackground
import com.resumeai.pro.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    navController: NavController,
    resumeId: String,
    viewModel: PreviewViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(resumeId) {
        viewModel.loadResume(resumeId)
    }

    val resume = viewModel.resume.collectAsState().value
    val uiState = viewModel.uiState.collectAsState().value
    val atsResult = viewModel.atsResult.collectAsState().value
    var showJobDescDialog by remember { mutableStateOf(false) }
    var jobDescText by remember { mutableStateOf("") }
    var jobMatchMode by remember { mutableStateOf(false) } // false=ATS, true=JobMatch

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preview", color = StarWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = StarWhite)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: WIP Share Feature */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = CyberCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepSpace)
            )
        },
        containerColor = DeepSpace
    ) { padding ->
        if (resume == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                ParticleBackground(alpha = 0.2f)
                CircularProgressIndicator(color = CyberCyan, strokeWidth = 3.dp, modifier = Modifier.size(48.dp))
            }
        } else if (uiState is PreviewUiState.Exporting) {
            // === FULL-SCREEN EXPORT LOADING UI ===
            ExportLoadingScreen(
                modifier = Modifier.padding(padding),
                progress = uiState.progress,
                statusMessage = uiState.statusMessage,
                format = uiState.format
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                ParticleBackground(alpha = 0.15f)
                
                var isVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { isVisible = true }
                
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(600)) + slideInVertically(initialOffsetY = { it / 4 }),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                // Resume preview card
                HolographicCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        // Profile photo + Header
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Profile photo
                            val photoUri = resume.personalInfo.photoUri
                            if (!photoUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(Uri.parse(photoUri))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, CyberCyan, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                            Column {
                                Text(
                                    text = resume.personalInfo.fullName,
                                    color = StarWhite,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = resume.personalInfo.jobTitle,
                                    color = CyberCyan,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = listOf(resume.personalInfo.email, resume.personalInfo.phone, resume.personalInfo.location)
                                .filter { it.isNotEmpty() }
                                .joinToString(" • "),
                            color = MoonGray,
                            fontSize = 11.sp
                        )

                        // Links
                        val links = listOfNotNull(
                            resume.personalInfo.linkedinUrl.takeIf { it.isNotEmpty() },
                            resume.personalInfo.githubUrl.takeIf { it.isNotEmpty() },
                            resume.personalInfo.portfolioUrl.takeIf { it.isNotEmpty() }
                        )
                        if (links.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = links.joinToString(" | "),
                                color = NebulaPurple,
                                fontSize = 10.sp
                            )
                        }

                        if (resume.summary.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MoonGray.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("PROFESSIONAL SUMMARY", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(resume.summary, color = StarWhite, fontSize = 12.sp, lineHeight = 18.sp)
                        }

                        if (resume.experiences.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MoonGray.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("WORK EXPERIENCE", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            resume.experiences.forEach { exp ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(exp.jobTitle, color = StarWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("${exp.company} | ${exp.startDate} – ${exp.endDate}", color = MoonGray, fontSize = 11.sp)
                                if (exp.description.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(exp.description, color = StarWhite.copy(alpha = 0.9f), fontSize = 11.sp, lineHeight = 16.sp)
                                }
                            }
                        }

                        if (resume.educations.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MoonGray.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("EDUCATION", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            resume.educations.forEach { edu ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("${edu.degree} in ${edu.fieldOfStudy}", color = StarWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                val eduLine = buildString {
                                    append(edu.institution)
                                    if (edu.startYear.isNotEmpty()) append(" | ${edu.startYear} – ${edu.endYear}")
                                    if (edu.gpa.isNotEmpty()) append(" | GPA: ${edu.gpa}")
                                }
                                Text(eduLine, color = MoonGray, fontSize = 11.sp)
                            }
                        }

                        if (resume.skills.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MoonGray.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("SKILLS", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = resume.skills.joinToString(" • ") { it.name },
                                color = StarWhite,
                                fontSize = 11.sp
                            )
                        }
                        if (resume.projects.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = MoonGray.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("PROJECTS", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            resume.projects.forEach { proj ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(proj.name, color = StarWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    if (proj.link.isNotEmpty()) {
                                        Text(proj.link, color = NebulaPurple, fontSize = 10.sp)
                                    }
                                }
                                if (proj.technologies.isNotEmpty()) {
                                    Text("Technologies: ${proj.technologies}", color = MoonGray, fontSize = 11.sp)
                                }
                                if (proj.description.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(proj.description, color = StarWhite.copy(alpha = 0.9f), fontSize = 11.sp, lineHeight = 16.sp)
                                }
                            }
                        }
                    }
                }

                // === EXPORT BUTTONS ===
                Text("Export", color = CyberCyan, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NeonButton(
                        text = "📄 PDF",
                        onClick = { viewModel.exportPdf(context) },
                        modifier = Modifier.weight(1f),
                        enabled = uiState !is PreviewUiState.Loading
                    )
                    NeonButton(
                        text = "📝 DOCX",
                        onClick = { viewModel.exportDocx(context) },
                        modifier = Modifier.weight(1f),
                        enabled = uiState !is PreviewUiState.Loading
                    )
                }

                // === AI TOOLS SECTION ===
                Text("AI Tools", color = CyberCyan, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AIToolCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.AutoAwesome,
                        title = "Improve",
                        onClick = { viewModel.improveResume() },
                        enabled = uiState !is PreviewUiState.Loading
                    )
                    AIToolCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.VerifiedUser,
                        title = "ATS Score",
                        onClick = { viewModel.atsOptimize() },
                        enabled = uiState !is PreviewUiState.Loading
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AIToolCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.AutoMirrored.Filled.CompareArrows,
                        title = "Job Match",
                        onClick = {
                            jobMatchMode = true
                            showJobDescDialog = true
                        },
                        enabled = uiState !is PreviewUiState.Loading
                    )
                    AIToolCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.AutoMirrored.Filled.FormatAlignLeft,
                        title = "Format AI",
                        onClick = { viewModel.analyzeFormatting() },
                        enabled = uiState !is PreviewUiState.Loading
                    )
                }

                // === LOADING ===
                if (uiState is PreviewUiState.Loading) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = CyberCyan, modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("AI is analyzing your resume...", color = MoonGray, fontSize = 13.sp)
                        }
                    }
                }

                // === AI RESULTS ===
                if (uiState is PreviewUiState.AiResult) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = uiState.title,
                                    color = CyberCyan,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { viewModel.clearUiState() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MoonGray, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.result,
                                color = StarWhite,
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // === ERROR ===
                if (uiState is PreviewUiState.Error) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                            Text(uiState.message, color = ErrorRed, fontSize = 13.sp)
                        }
                    }
                }

                // === EXPORT SUCCESS ===
                if (uiState is PreviewUiState.ExportSuccess) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                            Text(uiState.message, color = SuccessGreen, fontSize = 13.sp)
                        }
                    }
                }

                // === ATS DASHBOARD ===
                if (atsResult != null) {
                    ATSDashboard(
                        atsResult = atsResult,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
                }
            }
        }
    }

    // Job description dialog
    if (showJobDescDialog) {
        AlertDialog(
            onDismissRequest = { showJobDescDialog = false },
            title = {
                Text(
                    if (jobMatchMode) "Paste Job Description" else "Enter Job Description (optional)",
                    color = StarWhite
                )
            },
            text = {
                OutlinedTextField(
                    value = jobDescText,
                    onValueChange = { jobDescText = it },
                    label = { Text("Job Description") },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    colors = OutlinedTextFieldDefaults.colors(
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
                )
            },
            confirmButton = {
                NeonButton(
                    text = "Analyze",
                    onClick = {
                        showJobDescDialog = false
                        if (jobMatchMode) {
                            viewModel.jobMatch(jobDescText)
                        } else {
                            viewModel.atsOptimize(jobDescText)
                        }
                    }
                )
            },
            dismissButton = {
                NeonButton(
                    text = "Cancel",
                    isOutlined = true,
                    onClick = { showJobDescDialog = false }
                )
            },
            containerColor = DeepSpace,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

/**
 * Full-screen animated export loading UI with progress ring and dynamic status.
 */
@Composable
private fun ExportLoadingScreen(
    modifier: Modifier = Modifier,
    progress: Float,
    statusMessage: String,
    format: String
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "progress_animation"
    )
    val percentValue = (animatedProgress * 100).toInt()

    // Pulsing glow
    val infiniteTransition = rememberInfiniteTransition(label = "export_glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpace),
        contentAlignment = Alignment.Center
    ) {
        ParticleBackground(alpha = 0.3f)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Progress ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                // Background glow
                Box(
                    modifier = Modifier
                        .size((160 * glowScale).dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    NebulaPurple.copy(alpha = 0.15f),
                                    CyberCyan.copy(alpha = 0.05f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )

                // Track ring
                androidx.compose.foundation.Canvas(modifier = Modifier.size(150.dp)) {
                    drawArc(
                        color = MoonGray.copy(alpha = 0.2f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                        topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                        size = Size(size.width - 8.dp.toPx(), size.height - 8.dp.toPx())
                    )
                }

                // Progress arc with gradient
                androidx.compose.foundation.Canvas(modifier = Modifier.size(150.dp)) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(NebulaPurple, CyberCyan, NebulaPurple)
                        ),
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                        topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                        size = Size(size.width - 8.dp.toPx(), size.height - 8.dp.toPx())
                    )
                }

                // Percentage text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${percentValue}%",
                        color = StarWhite,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = format,
                        color = CyberCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Status message with fade
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(300))
            ) {
                Text(
                    text = statusMessage,
                    color = MoonGray,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Please wait while your document is being prepared",
                color = MoonGray.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun AIToolCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    GlassCard(
        modifier = modifier.height(80.dp),
        onClick = if (enabled) onClick else null
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (enabled) CyberCyan else MoonGray.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = if (enabled) StarWhite else MoonGray.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
