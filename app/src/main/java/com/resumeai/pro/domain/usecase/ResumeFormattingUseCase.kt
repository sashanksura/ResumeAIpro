package com.resumeai.pro.domain.usecase

import com.google.gson.Gson
import com.resumeai.pro.data.api.AIService
import com.resumeai.pro.domain.model.Resume
import javax.inject.Inject

class ResumeFormattingUseCase @Inject constructor(
    private val aiService: AIService
) {
    suspend operator fun invoke(resume: Resume): Result<String> {
        val context = buildString {
            append("Total Sections: ${listOfNotNull(
                if (resume.summary.isNotEmpty()) "Summary" else null,
                if (resume.experiences.isNotEmpty()) "Experience (${resume.experiences.size})" else null,
                if (resume.educations.isNotEmpty()) "Education (${resume.educations.size})" else null,
                if (resume.skills.isNotEmpty()) "Skills (${resume.skills.size})" else null,
                if (resume.projects.isNotEmpty()) "Projects (${resume.projects.size})" else null
            ).joinToString(", ")}\n")
            append("Summary length: ${resume.summary.length} characters\n")
        }

        val userPrompt = """
            Analyze the formatting and structure of this resume for ATS compliance and professional readability.
            
            Resume Structure:
            $context
            Target Role: ${resume.personalInfo.jobTitle}
            
            Provide a JSON response with exactly this structure:
            {
              "structureScore": 85,
              "readabilityScore": 90,
              "formattingIssues": ["issue1", "issue2"],
              "improvements": ["improvement1", "improvement2"]
            }
            Return ONLY the JSON. No markdown blocks, no other text.
        """.trimIndent()

        val result = aiService.generateWithSystem(
            systemPrompt = PromptTemplates.SYSTEM_PROMPT,
            userPrompt = userPrompt,
            requestId = "resume_formatting",
            maxTokens = 800
        )
        
        return result.mapCatching { json ->
            val cleanJson = json.replace("```json", "").replace("```", "").trim()
            val type = object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
            val map: Map<String, Any> = Gson().fromJson(cleanJson, type)
            
            buildString {
                append("Structure Score: ${map["structureScore"] ?: "N/A"}/100\n")
                append("Readability Score: ${map["readabilityScore"] ?: "N/A"}/100\n")
                append("\nFormatting Issues:\n")
                (map["formattingIssues"] as? List<*>)?.forEach { append("• $it\n") }
                append("\nImprovements:\n")
                (map["improvements"] as? List<*>)?.forEach { append("• $it\n") }
            }
        }
    }
}
