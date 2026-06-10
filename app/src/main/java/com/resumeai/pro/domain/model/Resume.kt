package com.resumeai.pro.domain.model

data class Resume(
    val id: String,
    val name: String,
    val templateId: String,
    val personalInfo: PersonalInfo,
    val experiences: List<Experience>,
    val educations: List<Education>,
    val skills: List<Skill>,
    val projects: List<Project> = emptyList(),
    val summary: String,
    val jobDescription: String = "",
    val jobDescriptionUrl: String = "",
    val accentColor: String,
    val fontPair: String,
    val completionPercent: Int,
    val createdAt: Long,
    val updatedAt: Long
)
