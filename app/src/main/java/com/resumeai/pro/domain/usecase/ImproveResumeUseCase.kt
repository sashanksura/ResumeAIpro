package com.resumeai.pro.domain.usecase

import com.google.gson.Gson
import com.resumeai.pro.data.api.AIService
import com.resumeai.pro.domain.model.Resume
import javax.inject.Inject

class ImproveResumeUseCase @Inject constructor(
    private val aiService: AIService
) {
    suspend operator fun invoke(resume: Resume): Result<String> {
        val context = buildString {
            append("Name: ${resume.personalInfo.fullName}\n")
            append("Title: ${resume.personalInfo.jobTitle}\n")
            append("Summary: ${resume.summary}\n")
            append("Skills: ${resume.skills.joinToString(", ") { it.name }}\n")
            append("Experience:\n")
            resume.experiences.forEach { append("• ${it.jobTitle} at ${it.company}: ${it.description.take(150)}\n") }
            append("Education:\n")
            resume.educations.forEach { append("• ${it.degree} in ${it.fieldOfStudy} from ${it.institution}\n") }
        }

        val userPrompt = """
            Review this resume and provide actionable improvement feedback.
            
            Resume:
            $context
            
            Provide a JSON response with exactly this structure:
            {
              "overallScore": 85,
              "strengths": ["strength1", "strength2"],
              "weaknesses": ["weakness1", "weakness2"],
              "actionItems": ["action1", "action2"]
            }
            Return ONLY the JSON. No markdown blocks, no other text.
        """.trimIndent()

        val result = aiService.generateWithSystem(
            systemPrompt = PromptTemplates.SYSTEM_PROMPT,
            userPrompt = userPrompt,
            requestId = "improve_resume",
            maxTokens = 800
        )
        
        return result.mapCatching { json ->
            val cleanJson = json.replace("```json", "").replace("```", "").trim()
            val type = object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
            val map: Map<String, Any> = Gson().fromJson(cleanJson, type)
            
            buildString {
                append("Overall Score: ${map["overallScore"] ?: "N/A"}/100\n")
                append("\nStrengths:\n")
                (map["strengths"] as? List<*>)?.forEach { append("• $it\n") }
                append("\nWeaknesses:\n")
                (map["weaknesses"] as? List<*>)?.forEach { append("• $it\n") }
                append("\nAction Items:\n")
                (map["actionItems"] as? List<*>)?.forEach { append("• $it\n") }
            }
        }
    }
}
