package com.resumeai.pro.domain.usecase

import com.google.gson.Gson
import com.resumeai.pro.data.api.AIService
import com.resumeai.pro.domain.model.ATSResult
import com.resumeai.pro.domain.model.Resume
import javax.inject.Inject

class ATSOptimizeUseCase @Inject constructor(
    private val aiService: AIService
) {
    suspend operator fun invoke(resume: Resume, jobDescription: String = ""): Result<String> {
        val context = buildString {
            append("Title: ${resume.personalInfo.jobTitle}\n")
            append("Skills: ${resume.skills.joinToString(", ") { it.name }}\n")
            append("Experience Titles:\n")
            resume.experiences.forEach { append("• ${it.jobTitle} at ${it.company}\n") }
        }

        val userPrompt = """
            Analyze this resume for ATS (Applicant Tracking System) optimization against the target job description.
            
            Resume:
            $context
            
            Target Job Description:
            ${jobDescription.take(1500).ifBlank { "General industry standards" }}
            
            Provide a JSON response with EXACTLY this structure (all fields required, integers 0-100):
            {
              "overallScore": 78,
              "skillsMatch": 82,
              "keywordMatch": 71,
              "experienceMatch": 85,
              "educationMatch": 90,
              "missingSkills": ["skill1", "skill2"],
              "missingKeywords": ["keyword1", "keyword2"],
              "criticalFixes": ["fix1", "fix2"],
              "suggestions": ["suggestion1", "suggestion2"]
            }
            Return ONLY the JSON. No markdown blocks, no explanatory text.
        """.trimIndent()

        val result = aiService.generateWithSystem(
            systemPrompt = PromptTemplates.SYSTEM_PROMPT,
            userPrompt = userPrompt,
            requestId = "ats_optimize",
            maxTokens = 1024
        )
        
        return result.mapCatching { json ->
            val cleanJson = json.replace("```json", "").replace("```", "").trim()
            val type = object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
            val map: Map<String, Any> = Gson().fromJson(cleanJson, type)
            
            buildString {
                append("ATS Score: ${(map["overallScore"] as? Double)?.toInt() ?: map["overallScore"] ?: "N/A"}/100\n")
                append("Skills Match: ${(map["skillsMatch"] as? Double)?.toInt() ?: map["skillsMatch"] ?: "N/A"}%\n")
                append("Keyword Match: ${(map["keywordMatch"] as? Double)?.toInt() ?: map["keywordMatch"] ?: "N/A"}%\n")
                append("Experience Match: ${(map["experienceMatch"] as? Double)?.toInt() ?: map["experienceMatch"] ?: "N/A"}%\n")
                append("Education Match: ${(map["educationMatch"] as? Double)?.toInt() ?: map["educationMatch"] ?: "N/A"}%\n")
                append("\nMissing Skills:\n")
                (map["missingSkills"] as? List<*>)?.forEach { append("• $it\n") }
                append("\nMissing Keywords:\n")
                (map["missingKeywords"] as? List<*>)?.forEach { append("• $it\n") }
                append("\nCritical Fixes:\n")
                (map["criticalFixes"] as? List<*>)?.forEach { append("• $it\n") }
                append("\nSuggestions:\n")
                (map["suggestions"] as? List<*>)?.forEach { append("• $it\n") }
            }
        }
    }

    /** Parse raw AI JSON into a structured ATSResult for dashboard display. */
    fun parseATSResult(json: String): ATSResult? {
        return try {
            val cleanJson = json.replace("```json", "").replace("```", "").trim()
            val type = object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
            val map: Map<String, Any> = Gson().fromJson(cleanJson, type)
            ATSResult(
                overallScore = (map["overallScore"] as? Double)?.toInt() ?: 0,
                skillsMatch = (map["skillsMatch"] as? Double)?.toInt() ?: 0,
                keywordMatch = (map["keywordMatch"] as? Double)?.toInt() ?: 0,
                experienceMatch = (map["experienceMatch"] as? Double)?.toInt() ?: 0,
                educationMatch = (map["educationMatch"] as? Double)?.toInt() ?: 0,
                missingSkills = (map["missingSkills"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                missingKeywords = (map["missingKeywords"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                suggestions = (map["suggestions"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                criticalFixes = (map["criticalFixes"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            )
        } catch (e: Exception) {
            null
        }
    }
}
