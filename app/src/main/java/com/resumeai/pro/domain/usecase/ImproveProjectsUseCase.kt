package com.resumeai.pro.domain.usecase

import com.resumeai.pro.data.api.AIService
import javax.inject.Inject

class ImproveProjectsUseCase @Inject constructor(
    private val aiService: AIService
) {
    suspend operator fun invoke(
        projectName: String,
        description: String,
        technologies: String
    ): Result<String> {
        val userPrompt = """
            Review and improve the description of this project to highlight technical depth and impact.
            Use the STAR method where possible. Do not invent new features or metrics not mentioned.
            
            Project Name: $projectName
            Technologies: $technologies
            Current Description:
            $description
            
            Return the improved project description as 3-4 bullet points. Return ONLY the bullet points, each starting with '• '.
        """.trimIndent()

        return aiService.generateWithSystem(
            systemPrompt = PromptTemplates.SYSTEM_PROMPT,
            userPrompt = userPrompt,
            requestId = "improve_projects_$projectName",
            maxTokens = 600,
            temperature = 0.7f
        )
    }
}
