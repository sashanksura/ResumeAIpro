package com.resumeai.pro.domain.model

/**
 * Structured result from ATS analysis by the AI.
 * Used to populate the ATSDashboard in PreviewScreen.
 */
data class ATSResult(
    val overallScore: Int = 0,
    val skillsMatch: Int = 0,
    val keywordMatch: Int = 0,
    val experienceMatch: Int = 0,
    val educationMatch: Int = 0,
    val missingSkills: List<String> = emptyList(),
    val missingKeywords: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val criticalFixes: List<String> = emptyList()
)
