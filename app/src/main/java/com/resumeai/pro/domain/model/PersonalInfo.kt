package com.resumeai.pro.domain.model

data class PersonalInfo(
    val fullName: String = "",
    val jobTitle: String = "",
    val email: String = "",
    val phone: String = "",
    val linkedinUrl: String = "",
    val githubUrl: String = "",
    val location: String = "",
    val portfolioUrl: String = "",
    val photoUri: String? = null
)
