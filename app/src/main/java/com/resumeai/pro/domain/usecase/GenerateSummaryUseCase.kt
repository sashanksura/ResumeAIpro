package com.resumeai.pro.domain.usecase

import com.resumeai.pro.data.api.AIService
import javax.inject.Inject

class GenerateSummaryUseCase @Inject constructor(
    private val aiService: AIService
) {
    suspend operator fun invoke(
        name: String,
        jobTitle: String,
        years: String,
        skills: String,
        achievement: String,
        tone: String = "Professional",
        jobDescription: String = ""
    ): Result<String> {
        val userPrompt = PromptTemplates.generateSummaryPrompt(name, jobTitle, years, skills, achievement, tone, jobDescription)

        return aiService.generateWithSystem(
            systemPrompt = PromptTemplates.SYSTEM_PROMPT,
            userPrompt = userPrompt,
            requestId = "generate_summary",
            maxTokens = 400,
            temperature = 0.7f
        )
    }
}
