package com.resumeai.pro.domain.usecase

/**
 * Centralized object containing all AI prompt templates.
 * Extremely concise to reduce latency and token usage.
 * Strict rules to prevent conversational output and markdown formatting.
 */
object PromptTemplates {

    const val SYSTEM_PROMPT = """You are a strict text processor. Output EXACTLY the requested resume content and nothing else. NO explanations, NO conversational text, NO markdown formatting, NO headers. NEVER fabricate facts or metrics."""

    const val STAR_SYSTEM_PROMPT = """You are a strict text processor. Rewrite into STAR-method bullets. Output ONLY the bullets. NO explanations, NO markdown, NO labels, NO conversational text."""

    fun suggestSkillsPrompt(
        jobTitle: String,
        existingSkills: List<String>,
        experiences: String,
        jobDescription: String
    ): String {
        return """Suggest 10 ATS-friendly skills.
Title: $jobTitle
Current: ${existingSkills.joinToString(", ").ifEmpty { "None" }}
Exp: $experiences
JD: ${jobDescription.take(800)}
RULES: Return ONLY a comma-separated list. NO explanations."""
    }

    fun rewriteExperiencePrompt(
        jobTitle: String,
        company: String,
        description: String,
        jobDescription: String
    ): String {
        return """Rewrite as STAR-method bullets.
Title: $jobTitle
Company: $company
Original: $description
${if (jobDescription.isNotBlank()) "JD: ${jobDescription.take(800)}" else ""}
RULES: Start with action verbs. NO fabricated metrics. Return ONLY 3-5 bullets starting with '•'."""
    }

    fun enhanceBulletPointsPrompt(
        jobTitle: String,
        company: String,
        description: String,
        jobDescription: String
    ): String {
        return """Enhance these bullets for ATS.
Title: $jobTitle
Company: $company
Original: $description
${if (jobDescription.isNotBlank()) "JD: ${jobDescription.take(800)}" else ""}
RULES: Preserve facts exactly. Return ONLY 3-5 bullets starting with '•'."""
    }

    fun generateSummaryPrompt(
        name: String,
        jobTitle: String,
        years: String,
        skills: String,
        achievement: String,
        tone: String,
        jobDescription: String
    ): String {
        return """Write a professional summary (60-80 words).
Name: $name
Title: $jobTitle
Years: $years
Skills: $skills
Context: $achievement
Tone: $tone
${if (jobDescription.isNotBlank()) "JD: ${jobDescription.take(800)}" else ""}
RULES: Focus on facts. Return ONLY the summary text."""
    }

    fun improveProjectsPrompt(
        projectName: String,
        description: String,
        technologies: String
    ): String {
        return """Improve project description.
Project: $projectName
Tech: $technologies
Original: $description
RULES: Do not invent metrics. Return ONLY 3-4 bullets starting with '•'."""
    }
}
