package com.resumeai.pro.domain.usecase

import com.resumeai.pro.data.api.AIService
import com.resumeai.pro.domain.model.Resume
import javax.inject.Inject

class TailorResumeUseCase @Inject constructor(
    private val aiService: AIService
) {
    suspend operator fun invoke(resume: Resume, jobDescription: String): Result<String> {
        if (jobDescription.isBlank()) {
            return Result.failure(Exception("Please provide a job description to tailor your resume for."))
        }

        val systemPrompt = "You are a career coach who specializes in tailoring resumes for specific job applications. CRITICAL RULE: Never fabricate, assume, exaggerate, or invent any information. Only improve wording, grammar, clarity, and professionalism while preserving factual accuracy exactly as provided by the user. Do not add fake metrics, statistics, achievements, responsibilities, tools, or years of experience. All rewrites must use ONLY information already present in the resume."

        val userPrompt = """
            Tailor this resume for the given job description. Provide specific changes.

            RESUME:
            Name: ${resume.personalInfo.fullName}
            Role: ${resume.personalInfo.jobTitle}
            Summary: ${resume.summary}
            Skills: ${resume.skills.joinToString(", ") { it.name }}
            Experience:
            ${resume.experiences.joinToString("\n") { "• ${it.jobTitle} at ${it.company}: ${it.description.take(120)}" }}

            TARGET JOB DESCRIPTION:
            ${jobDescription.take(1500)}

            Provide:
            1. A rewritten summary tailored to this job (3 sentences)
            2. 5 keywords from the JD to add to skills
            3. Suggested rewrites for the top 2 experience bullet points to better match
            4. Any sections to emphasize or de-emphasize

            Be specific with actual rewritten text using ONLY existing resume content. Do NOT invent any new facts, metrics, or achievements.
        """.trimIndent()

        return aiService.generateWithSystem(
            systemPrompt = systemPrompt,
            userPrompt = userPrompt,
            requestId = "tailor_resume",
            maxTokens = 2048
        )
    }
}
