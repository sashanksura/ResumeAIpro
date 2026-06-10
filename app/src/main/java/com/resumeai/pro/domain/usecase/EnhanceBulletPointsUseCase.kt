package com.resumeai.pro.domain.usecase

import com.resumeai.pro.data.api.AIService
import javax.inject.Inject

class EnhanceBulletPointsUseCase @Inject constructor(
    private val aiService: AIService
) {
    suspend operator fun invoke(
        jobTitle: String,
        company: String,
        description: String,
        jobDescription: String = ""
    ): Result<String> {
        val userPrompt = PromptTemplates.enhanceBulletPointsPrompt(jobTitle, company, description, jobDescription)

        return aiService.generateWithSystem(
            systemPrompt = PromptTemplates.SYSTEM_PROMPT,
            userPrompt = userPrompt,
            requestId = "enhance_bullets_${jobTitle}_$company",
            maxTokens = 800,
            temperature = 0.7f
        )
    }
}
