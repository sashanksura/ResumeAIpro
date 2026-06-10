package com.resumeai.pro.data.api

import android.util.Log

/**
 * Structured performance logging for AI requests.
 * Tracks timing metrics for each request phase: network, parsing, total.
 * Stores the last N metrics in a ring buffer for debugging.
 */
object PerformanceLogger {
    private const val TAG = "AIPerf"
    private const val MAX_ENTRIES = 20

    data class AIRequestMetrics(
        val requestId: String,
        val model: String,
        val startTimeMs: Long,
        val responseTimeMs: Long = 0L,
        val parseTimeMs: Long = 0L,
        val totalDurationMs: Long = 0L,
        val inputTokenEstimate: Int = 0,
        val success: Boolean = false,
        val error: String? = null
    ) {
        fun toLogString(): String {
            val status = if (success) "OK" else "FAIL"
            return "[$status] id=$requestId model=$model total=${totalDurationMs}ms " +
                    "(network=${responseTimeMs}ms parse=${parseTimeMs}ms) " +
                    "inputTokens≈$inputTokenEstimate" +
                    (error?.let { " error=$it" } ?: "")
        }
    }

    private val metricsBuffer = ArrayDeque<AIRequestMetrics>(MAX_ENTRIES)

    fun logStart(requestId: String, model: String, inputTokenEstimate: Int): Long {
        val startTime = System.currentTimeMillis()
        Log.d(TAG, "▶ START id=$requestId model=$model inputTokens≈$inputTokenEstimate")
        return startTime
    }

    fun logResponse(requestId: String, startTimeMs: Long): Long {
        val responseTime = System.currentTimeMillis() - startTimeMs
        Log.d(TAG, "◀ RESPONSE id=$requestId network=${responseTime}ms")
        return responseTime
    }

    fun logComplete(metrics: AIRequestMetrics) {
        synchronized(metricsBuffer) {
            if (metricsBuffer.size >= MAX_ENTRIES) {
                metricsBuffer.removeFirst()
            }
            metricsBuffer.addLast(metrics)
        }
        Log.i(TAG, "✓ COMPLETE ${metrics.toLogString()}")
    }

    fun logError(requestId: String, error: String, durationMs: Long) {
        Log.e(TAG, "✗ ERROR id=$requestId duration=${durationMs}ms error=$error")
    }

    /**
     * Returns a formatted performance report of recent requests.
     */
    fun getReport(): String {
        val entries = synchronized(metricsBuffer) { metricsBuffer.toList() }
        if (entries.isEmpty()) return "No AI requests recorded yet."

        return buildString {
            append("=== AI Performance Report ===\n")
            append("Total requests tracked: ${entries.size}\n")
            val successes = entries.filter { it.success }
            val failures = entries.filter { !it.success }
            append("Successful: ${successes.size}, Failed: ${failures.size}\n")

            if (successes.isNotEmpty()) {
                val avgTotal = successes.map { it.totalDurationMs }.average().toLong()
                val maxTotal = successes.maxOf { it.totalDurationMs }
                val minTotal = successes.minOf { it.totalDurationMs }
                val avgNetwork = successes.map { it.responseTimeMs }.average().toLong()
                append("\n--- Timing (successful requests) ---\n")
                append("Avg total: ${avgTotal}ms (${avgTotal / 1000}s)\n")
                append("Min total: ${minTotal}ms (${minTotal / 1000}s)\n")
                append("Max total: ${maxTotal}ms (${maxTotal / 1000}s)\n")
                append("Avg network: ${avgNetwork}ms (${avgNetwork / 1000}s)\n")
            }

            append("\n--- Recent Requests ---\n")
            entries.takeLast(10).forEach { append("  ${it.toLogString()}\n") }
        }
    }

    /** Estimate token count from text (rough: ~4 chars per token) */
    fun estimateTokens(vararg texts: String): Int {
        return texts.sumOf { it.length } / 4
    }
}
