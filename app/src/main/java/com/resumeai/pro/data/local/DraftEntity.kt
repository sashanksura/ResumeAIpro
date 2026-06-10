package com.resumeai.pro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drafts")
data class DraftEntity(
    @PrimaryKey val id: String = "current_draft",
    val name: String,
    val templateId: String,
    val personalInfoJson: String,
    val experiencesJson: String,
    val educationsJson: String,
    val skillsJson: String,
    val suggestedSkillsJson: String = "[]",
    val projectsJson: String = "[]",
    val summary: String,
    val jobDescription: String = "",
    val jobDescriptionUrl: String = "",
    val selectedTone: String = "Professional",
    val currentStep: Int = 0,
    val accentColor: String,
    val lastModifiedAt: Long
)
