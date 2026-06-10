package com.resumeai.pro.data.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Extraction progress states for UI feedback.
 */
enum class ExtractionProgress(val message: String) {
    FETCHING("Fetching page content..."),
    STRIPPING("Extracting relevant text..."),
    ANALYZING("AI analyzing job requirements..."),
    COMPLETE("Job description extracted!"),
    ERROR("Extraction failed")
}

/**
 * Extracts job description content from a URL using OkHttp.
 * Strips HTML and uses the AI to parse structured JD text.
 * Features: URL-based caching, progress callbacks, optimized HTML stripping,
 * and graceful fallback with informative error messages.
 */
class JobDescriptionExtractor(
    private val okHttpClient: OkHttpClient,
    private val aiService: AIService
) {
    companion object {
        private const val MAX_HTML_LENGTH = 10_000  // chars, reduced to minimize AI prompt size
        private const val MAX_JD_LENGTH = 6_000
        private const val FETCH_TIMEOUT_SECONDS = 10L
    }

    // URL-based cache to prevent re-extraction of the same URL
    private val urlCache = mutableMapOf<String, String>()

    /**
     * Fetches the URL content and extracts a clean job description.
     * @param url The job posting URL
     * @param onProgress Callback for granular progress updates
     * @return Result<String> with the extracted job description text,
     *         or a failure with a descriptive message.
     */
    suspend fun extract(
        url: String,
        onProgress: (ExtractionProgress) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        val trimmedUrl = url.trim()
        if (trimmedUrl.isBlank()) {
            return@withContext Result.failure(Exception("URL cannot be empty."))
        }
        if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://")) {
            return@withContext Result.failure(Exception("Please enter a valid URL starting with https://"))
        }

        // Check URL cache first
        urlCache[trimmedUrl]?.let { cached ->
            onProgress(ExtractionProgress.COMPLETE)
            return@withContext Result.success(cached)
        }

        return@withContext try {
            onProgress(ExtractionProgress.FETCHING)

            // Build a client with a shorter timeout for page fetching
            val fetchClient = okHttpClient.newBuilder()
                .connectTimeout(FETCH_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(FETCH_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url(trimmedUrl)
                .header("User-Agent", "Mozilla/5.0 (compatible; ResumeAI/1.0)")
                .header("Accept", "text/html,application/xhtml+xml")
                .build()

            val response = fetchClient.newCall(request).execute()
            if (!response.isSuccessful) {
                onProgress(ExtractionProgress.ERROR)
                return@withContext Result.failure(
                    Exception("Could not load page (HTTP ${response.code}). Please paste the job description manually.")
                )
            }

            val rawHtml = response.body?.string() ?: ""
            if (rawHtml.isBlank()) {
                onProgress(ExtractionProgress.ERROR)
                return@withContext Result.failure(Exception("The page returned empty content. Please paste the job description manually."))
            }

            onProgress(ExtractionProgress.STRIPPING)

            // Enhanced HTML → text stripping with structural element removal
            val plainText = stripHtml(rawHtml).take(MAX_HTML_LENGTH)

            if (plainText.length < 200) {
                onProgress(ExtractionProgress.ERROR)
                return@withContext Result.failure(Exception("Page content too short to extract a job description. Please paste it manually."))
            }

            onProgress(ExtractionProgress.ANALYZING)

            // Use AI to extract and structure the JD from raw page text
            val systemPrompt = """Extract ONLY the job description from the raw text. Return sections: Job Title, Company, Requirements, Skills, Responsibilities.
Reply exactly NO_JD_FOUND if not found. NO explanations."""

            val userPrompt = """$plainText"""

            val aiResult = aiService.generateWithSystem(
                systemPrompt = systemPrompt,
                userPrompt = userPrompt,
                requestId = "jd_extract_${trimmedUrl.hashCode()}",
                maxTokens = 1500,
                temperature = 0.3f
            )

            aiResult.mapCatching { extracted ->
                if (extracted.contains("NO_JD_FOUND", ignoreCase = true)) {
                    onProgress(ExtractionProgress.ERROR)
                    throw Exception("No job description found on this page. Please paste the job description manually.")
                }
                val result = extracted.trim().take(MAX_JD_LENGTH)
                // Cache successful extraction
                urlCache[trimmedUrl] = result
                onProgress(ExtractionProgress.COMPLETE)
                result
            }
        } catch (e: java.net.UnknownHostException) {
            onProgress(ExtractionProgress.ERROR)
            Result.failure(Exception("No internet connection. Please check your network and try again."))
        } catch (e: java.net.SocketTimeoutException) {
            onProgress(ExtractionProgress.ERROR)
            Result.failure(Exception("Request timed out. The page may be blocking automated access. Please paste the job description manually."))
        } catch (e: Exception) {
            onProgress(ExtractionProgress.ERROR)
            if (e.message?.contains("NO_JD_FOUND") == true ||
                e.message?.contains("paste") == true ||
                e.message?.contains("manually") == true) {
                Result.failure(e)
            } else {
                Result.failure(Exception("Could not extract job description: ${e.message}. Please paste it manually."))
            }
        }
    }

    /**
     * Strips HTML tags, removes structural noise elements, decodes common entities,
     * and normalizes whitespace.
     */
    private fun stripHtml(html: String): String {
        return html
            // Remove script and style blocks entirely
            .replace(Regex("<script[^>]*>[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("<style[^>]*>[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), " ")
            // Remove structural noise elements (nav, header, footer, aside, iframe)
            .replace(Regex("<nav[^>]*>[\\s\\S]*?</nav>", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("<header[^>]*>[\\s\\S]*?</header>", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("<footer[^>]*>[\\s\\S]*?</footer>", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("<aside[^>]*>[\\s\\S]*?</aside>", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("<iframe[^>]*>[\\s\\S]*?</iframe>", RegexOption.IGNORE_CASE), " ")
            .replace(Regex("<noscript[^>]*>[\\s\\S]*?</noscript>", RegexOption.IGNORE_CASE), " ")
            // Replace block-level tags with newlines for readability
            .replace(Regex("</(p|div|li|h[1-6]|tr|br)[^>]*>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<(br|hr)[^>]*>", RegexOption.IGNORE_CASE), "\n")
            // Remove remaining HTML tags
            .replace(Regex("<[^>]+>"), " ")
            // Decode HTML entities
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace(Regex("&#\\d+;"), "")
            // Normalize whitespace
            .replace(Regex("[ \\t]+"), " ")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }
}
