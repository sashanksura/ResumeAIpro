package com.resumeai.pro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resumes")
data class ResumeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val templateId: String,
    val personalInfoJson: String,
    val experiencesJson: String,
    val educationsJson: String,
    val skillsJson: String,
    val projectsJson: String = "[]",
    val summary: String,
    val jobDescription: String = "",
    val jobDescriptionUrl: String = "",
    val accentColor: String,
    val fontPair: String,
    val completionPercent: Int,
    val createdAt: Long,
    val updatedAt: Long
)

