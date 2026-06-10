package com.resumeai.pro.domain.usecase

import com.resumeai.pro.data.api.AIService
import javax.inject.Inject

class RewriteExperienceUseCase @Inject constructor(
    private val aiService: AIService
) {
    suspend operator fun invoke(
        jobTitle: String,
        company: String,
        description: String,
        jobDescription: String = ""
    ): Result<String> {
        val userPrompt = PromptTemplates.rewriteExperiencePrompt(jobTitle, company, description, jobDescription)

        return aiService.generateWithSystem(
            systemPrompt = PromptTemplates.SYSTEM_PROMPT,
            userPrompt = userPrompt,
            requestId = "rewrite_exp_${jobTitle}_$company",
            maxTokens = 800,
            temperature = 0.7f
        )
    }
}
