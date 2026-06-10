package com.resumeai.pro.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton
import java.security.MessageDigest

/**
 * Centralized AI service wrapping NVIDIA NIM API calls.
 * Features: model fallback chain, retry with exponential backoff,
 * duplicate request prevention, response validation, structured error handling,
 * response caching, performance logging, request timeout, and cancellation support.
 */
@Singleton
class AIService @Inject constructor(
    private val apiService: NvidiaApiService
) {
    private val apiKey: String
        get() = AIConfig.apiKey

    private val mutex = Mutex()
    private val activeRequests = mutableSetOf<String>()
    
    // LRU-like in-memory cache for prompts to avoid duplicate API calls (max 50 entries)
    private val responseCache = LinkedHashMap<String, String>(50, 0.75f, true)
    private val maxCacheSize = 50

    private fun getCacheKey(systemPrompt: String, userPrompt: String): String {
        val input = "$systemPrompt|$userPrompt"
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Generate AI content with the default resume-writer system prompt.
     *
     * @param prompt The user prompt to send
     * @param requestId Optional unique ID to prevent duplicate concurrent requests
     * @param maxRetries Maximum retry attempts per model before falling back
     * @param temperature Sampling temperature (0.0-1.0)
     * @param maxTokens Maximum tokens in response
     * @return Result<String> with generated content or an error
     */
    suspend fun generate(
        prompt: String,
        requestId: String = "",
        maxRetries: Int = AIConfig.maxRetries,
        temperature: Float = AIConfig.defaultTemperature,
        topP: Float = AIConfig.defaultTopP,
        maxTokens: Int = AIConfig.defaultMaxTokens
    ): Result<String> {
        return generateWithSystem(
            systemPrompt = com.resumeai.pro.domain.usecase.PromptTemplates.SYSTEM_PROMPT,
            userPrompt = prompt,
            requestId = requestId,
            temperature = temperature,
            topP = topP,
            maxTokens = maxTokens,
            maxRetries = maxRetries
        )
    }

    /**
     * Generate AI content with a custom system prompt.
     * Includes performance logging, timeout protection, and LRU cache.
     */
    suspend fun generateWithSystem(
        systemPrompt: String,
        userPrompt: String,
        requestId: String = "",
        temperature: Float = AIConfig.defaultTemperature,
        topP: Float = AIConfig.defaultTopP,
        maxTokens: Int = AIConfig.defaultMaxTokens,
        maxRetries: Int = AIConfig.maxRetries
    ): Result<String> = withContext(Dispatchers.IO) {
        if (userPrompt.isBlank()) {
            return@withContext Result.failure(Exception("Prompt cannot be empty"))
        }

        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("NVIDIA API key is not configured. Go to Settings to add your API key."))
        }
        
        val cacheKey = getCacheKey(systemPrompt, userPrompt)
        
        // Check cache first
        mutex.withLock {
            responseCache[cacheKey]?.let { cached ->
                PerformanceLogger.logComplete(
                    PerformanceLogger.AIRequestMetrics(
                        requestId = requestId.ifEmpty { "cached" },
                        model = "cache",
                        startTimeMs = System.currentTimeMillis(),
                        totalDurationMs = 0,
                        success = true
                    )
                )
                return@withContext Result.success(cached)
            }
        }

        // Prevent duplicate concurrent requests
        if (requestId.isNotEmpty()) {
            mutex.withLock {
                if (activeRequests.contains(requestId)) {
                    return@withContext Result.failure(Exception("Request already in progress"))
                }
                activeRequests.add(requestId)
            }
        }

        val overallStartTime = System.currentTimeMillis()
        val inputTokenEstimate = PerformanceLogger.estimateTokens(systemPrompt, userPrompt)

        try {
            var lastException: Exception? = null

            for (model in AIConfig.modelChain) {
                for (attempt in 0..maxRetries) {
                    try {
                        if (attempt > 0) delay(1000L * attempt)

                        val startTime = PerformanceLogger.logStart(
                            requestId = requestId.ifEmpty { "anon" },
                            model = model,
                            inputTokenEstimate = inputTokenEstimate
                        )

                        val request = ChatRequest(
                            model = model,
                            messages = listOf(
                                ChatMessage(role = "system", content = systemPrompt),
                                ChatMessage(role = "user", content = userPrompt)
                            ),
                            temperature = temperature,
                            topP = topP,
                            maxTokens = maxTokens
                        )

                        // Wrap the actual API call in a timeout
                        val response = withTimeout(AIConfig.requestTimeoutSeconds * 1000) {
                            apiService.chatCompletion(
                                authorization = "Bearer $apiKey",
                                request = request
                            )
                        }

                        val networkTimeMs = PerformanceLogger.logResponse(
                            requestId = requestId.ifEmpty { "anon" },
                            startTimeMs = startTime
                        )

                        if (response.isSuccessful) {
                            val parseStart = System.currentTimeMillis()
                            val content = response.body()?.choices?.firstOrNull()?.message?.content
                            val parseTimeMs = System.currentTimeMillis() - parseStart

                            if (!content.isNullOrBlank()) {
                                val trimmedContent = sanitizeAiOutput(content)
                                val totalDuration = System.currentTimeMillis() - overallStartTime

                                // Cache the result (with LRU eviction)
                                mutex.withLock {
                                    if (responseCache.size >= maxCacheSize) {
                                        val oldestKey = responseCache.keys.firstOrNull()
                                        if (oldestKey != null) responseCache.remove(oldestKey)
                                    }
                                    responseCache[cacheKey] = trimmedContent
                                }

                                PerformanceLogger.logComplete(
                                    PerformanceLogger.AIRequestMetrics(
                                        requestId = requestId.ifEmpty { "anon" },
                                        model = model,
                                        startTimeMs = overallStartTime,
                                        responseTimeMs = networkTimeMs,
                                        parseTimeMs = parseTimeMs,
                                        totalDurationMs = totalDuration,
                                        inputTokenEstimate = inputTokenEstimate,
                                        success = true
                                    )
                                )

                                return@withContext Result.success(trimmedContent)
                            }
                            lastException = Exception("AI returned an empty response.")
                        } else {
                            val code = response.code()
                            if (code == 401 || code == 403) {
                                return@withContext Result.failure(Exception("Invalid API key. Check Settings."))
                            }
                            if (code == 404 || code == 503) break
                            if (code == 429) {
                                delay(3000L * (attempt + 1))
                                continue
                            }
                            lastException = Exception("API Error ($code): ${response.errorBody()?.string()}")
                        }
                    } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                        val elapsed = System.currentTimeMillis() - overallStartTime
                        PerformanceLogger.logError(requestId.ifEmpty { "anon" }, "Timeout after ${elapsed}ms on model $model", elapsed)
                        lastException = Exception("Request timed out after ${AIConfig.requestTimeoutSeconds}s. Please try again.")
                        break // Don't retry on timeout, try next model
                    } catch (e: java.net.UnknownHostException) {
                        return@withContext Result.failure(Exception("No internet connection."))
                    } catch (e: kotlinx.coroutines.CancellationException) {
                        throw e // Propagate cancellation
                    } catch (e: Exception) {
                        lastException = e
                    }
                }
            }

            val totalDuration = System.currentTimeMillis() - overallStartTime
            PerformanceLogger.logError(
                requestId.ifEmpty { "anon" },
                lastException?.message ?: "Unknown error",
                totalDuration
            )

            Result.failure(lastException ?: Exception("AI generation failed. Please try again."))
        } finally {
            if (requestId.isNotEmpty()) {
                mutex.withLock { activeRequests.remove(requestId) }
            }
        }
    }

    /**
     * Cancel an active request by removing it from the active set.
     * The actual coroutine cancellation is handled by the caller via Job.cancel().
     */
    fun cancelRequest(requestId: String) {
        activeRequests.remove(requestId)
    }

    /**
     * Quick test to verify the API key and connection work.
     */
    suspend fun testConnection(): Result<String> {
        return generate(
            prompt = "Respond with exactly: Connection successful.",
            requestId = "connection_test",
            maxRetries = 1,
            maxTokens = 50
        )
    }

    private fun sanitizeAiOutput(content: String): String {
        var text = content.trim()
        
        // Remove markdown formatting blocks
        text = text.replace(Regex("```[a-zA-Z]*\n"), "")
        text = text.replace("```", "")
        
        // Remove headers
        text = text.replace(Regex("^#+\\s+.*$", RegexOption.MULTILINE), "")
        
        // Remove bold/italic tags
        text = text.replace("**", "")
        text = text.replace("__", "")

        // Remove conversational prefixes and suffixes
        val prefixesToRemove = listOf(
            "Here are", "Here is", "Sure", "Certainly", "Based on",
            "Given the", "I have", "The following", "As a", "Note:",
            "Here's", "To answer", "This is", "Below is", "These are",
            "I've", "Please find"
        )
        
        val lines = text.split("\n").toMutableList()
        
        while (lines.isNotEmpty()) {
            val firstLine = lines.first().trim()
            val isConversational = prefixesToRemove.any { firstLine.startsWith(it, ignoreCase = true) }
            if (isConversational || firstLine.isEmpty()) {
                lines.removeAt(0)
            } else {
                break
            }
        }
        
        while (lines.isNotEmpty()) {
            val lastLine = lines.last().trim()
            val isConversational = prefixesToRemove.any { lastLine.startsWith(it, ignoreCase = true) } ||
                    lastLine.startsWith("Let me know", ignoreCase = true) ||
                    lastLine.startsWith("Feel free", ignoreCase = true) ||
                    lastLine.startsWith("I hope", ignoreCase = true) ||
                    lastLine.startsWith("Note:", ignoreCase = true)
            if (isConversational || lastLine.isEmpty()) {
                lines.removeAt(lines.size - 1)
            } else {
                break
            }
        }

        return lines.joinToString("\n").trim()
    }
}
