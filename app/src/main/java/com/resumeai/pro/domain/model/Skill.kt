package com.resumeai.pro.domain.model

data class Skill(
    val name: String,
    val category: String = "Technical",
    val proficiency: Float = 0.5f
)
