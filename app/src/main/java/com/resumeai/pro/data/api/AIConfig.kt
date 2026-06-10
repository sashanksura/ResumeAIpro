package com.resumeai.pro.data.api

import com.resumeai.pro.BuildConfig

/**
 * Centralized AI configuration for the app.
 * All AI settings are managed here for easy future model upgrades.
 */
object AIConfig {
    /** API key loaded from local.properties via BuildConfig */
    val apiKey: String = BuildConfig.NVIDIA_API_KEY

    /** NVIDIA NIM endpoint for DeepSeek V4 Pro */
    const val endpoint = "https://integrate.api.nvidia.com/v1/"

    /** Primary model */
    const val primaryModel = "deepseek-ai/deepseek-v4-pro"

    /** Fallback model — single fallback for fast failure */
    val fallbackModels = listOf(
        "nvidia/llama-3.3-nemotron-super-49b-v1"
    )

    /** Full model chain: primary + 1 fallback */
    val modelChain: List<String> = listOf(primaryModel) + fallbackModels

    /** Default generation parameters — tuned for speed & quality */
    const val defaultTemperature = 0.7f
    const val defaultTopP = 0.95f
    const val defaultMaxTokens = 2048
    const val maxRetries = 1
    const val requestTimeoutMs = 60_000L

    /** Coroutine-level timeout for each individual API call */
    const val requestTimeoutSeconds = 60L

    /** Extra body params for DeepSeek (disable thinking mode for faster responses) */
    val extraBody: Map<String, Any> = mapOf(
        "chat_template_kwargs" to mapOf("thinking" to false)
    )
}
