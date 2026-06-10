package com.resumeai.pro.domain.usecase

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.resumeai.pro.data.api.AIService
import javax.inject.Inject

class SuggestSkillsUseCase @Inject constructor(
    private val aiService: AIService
) {
    suspend operator fun invoke(
        jobTitle: String,
        existingSkills: List<String>,
        experiences: String,
        jobDescription: String = ""
    ): Result<List<String>> {
        val userPrompt = PromptTemplates.suggestSkillsPrompt(jobTitle, existingSkills, experiences, jobDescription)

        return aiService.generateWithSystem(
            systemPrompt = PromptTemplates.SYSTEM_PROMPT,
            userPrompt = userPrompt,
            requestId = "suggest_skills",
            temperature = 0.6f,
            maxTokens = 200
        ).mapCatching { text ->
            val cleanText = text.replace("```json", "").replace("```", "").trim()
            try {
                val type = object : TypeToken<List<String>>() {}.type
                val list: List<String> = Gson().fromJson(cleanText, type) ?: emptyList()
                if (list.isNotEmpty()) return@mapCatching list
            } catch (_: Exception) {}

            text.split(",").map { it.trim().removePrefix("-").removePrefix("•").trim() }
                .filter { it.length in 2..30 }
                .take(10)
        }
    }
}
