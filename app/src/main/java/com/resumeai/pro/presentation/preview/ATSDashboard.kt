package com.resumeai.pro.presentation.preview

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.resumeai.pro.domain.model.ATSResult
import com.resumeai.pro.ui.components.GlassCard
import com.resumeai.pro.ui.theme.*

/**
 * ATS Analysis Dashboard composable.
 * Displays an animated circular ATS score, bar charts for each category,
 * missing skills chips, and improvement suggestions.
 */
@Composable
fun ATSDashboard(
    atsResult: ATSResult,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
            Text("ATS Dashboard", color = CyberCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        // Overall score ring
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Overall ATS Score", color = MoonGray, fontSize = 13.sp)
                ATSScoreRing(score = atsResult.overallScore)
                Text(
                    text = when {
                        atsResult.overallScore >= 85 -> "🎯 Excellent Match"
                        atsResult.overallScore >= 70 -> "✅ Good Match"
                        atsResult.overallScore >= 55 -> "⚠️ Needs Improvement"
                        else -> "❌ Low Match – Review Suggestions"
                    },
                    color = when {
                        atsResult.overallScore >= 85 -> SuccessGreen
                        atsResult.overallScore >= 70 -> CyberCyan
                        atsResult.overallScore >= 55 -> Color(0xFFFFC107)
                        else -> ErrorRed
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Category breakdown
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Score Breakdown", color = StarWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                ATSBarItem(label = "Skills Match", score = atsResult.skillsMatch, color = NebulaPurple)
                ATSBarItem(label = "Keyword Match", score = atsResult.keywordMatch, color = CyberCyan)
                ATSBarItem(label = "Experience Match", score = atsResult.experienceMatch, color = Color(0xFF00BCD4))
                ATSBarItem(label = "Education Match", score = atsResult.educationMatch, color = Color(0xFF7C4DFF))
            }
        }

        // Missing skills chips
        if (atsResult.missingSkills.isNotEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                        Text("Missing Skills", color = StarWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text("Consider adding these skills if you genuinely have them:", color = MoonGray, fontSize = 11.sp)
                    // Chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        atsResult.missingSkills.chunked(3).forEach { rowItems ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                rowItems.forEach { skill ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = ErrorRed.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = skill,
                                            color = ErrorRed.copy(alpha = 0.9f),
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Missing keywords
        if (atsResult.missingKeywords.isNotEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Missing Keywords", color = StarWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        atsResult.missingKeywords.chunked(3).forEach { rowItems ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                rowItems.forEach { keyword ->
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xFFFFC107).copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = keyword,
                                            color = Color(0xFFFFC107),
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Improvement suggestions
        if (atsResult.suggestions.isNotEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("💡 Improvement Suggestions", color = CyberCyan, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    atsResult.suggestions.forEach { suggestion ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                            Text(suggestion, color = StarWhite.copy(alpha = 0.9f), fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Critical fixes
        if (atsResult.criticalFixes.isNotEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🚨 Critical Fixes", color = ErrorRed, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    atsResult.criticalFixes.forEach { fix ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                            Text(fix, color = ErrorRed.copy(alpha = 0.9f), fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ATSScoreRing(score: Int) {
    val animatedScore by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "ats_score"
    )
    val scoreColor = when {
        score >= 85 -> SuccessGreen
        score >= 70 -> CyberCyan
        score >= 55 -> Color(0xFFFFC107)
        else -> ErrorRed
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(140.dp)
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(120.dp)) {
            // Track
            drawArc(
                color = MoonGray.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(5.dp.toPx(), 5.dp.toPx()),
                size = Size(size.width - 10.dp.toPx(), size.height - 10.dp.toPx())
            )
            // Progress
            drawArc(
                brush = Brush.sweepGradient(colors = listOf(NebulaPurple, scoreColor, NebulaPurple)),
                startAngle = -90f,
                sweepAngle = animatedScore * 360f,
                useCenter = false,
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(5.dp.toPx(), 5.dp.toPx()),
                size = Size(size.width - 10.dp.toPx(), size.height - 10.dp.toPx())
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                color = StarWhite,
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "/ 100",
                color = MoonGray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ATSBarItem(label: String, score: Int, color: Color) {
    val animatedWidth by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "bar_$label"
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = MoonGray, fontSize = 12.sp)
            Text("$score%", color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
        ) {
            // Track
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(3.dp),
                color = MoonGray.copy(alpha = 0.2f)
            ) {}
            // Progress bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth(animatedWidth)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(3.dp),
                color = color.copy(alpha = 0.8f)
            ) {}
        }
    }
}
