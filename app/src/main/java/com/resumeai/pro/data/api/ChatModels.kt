package com.resumeai.pro.data.api

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    val model: String = AIConfig.primaryModel,
    val messages: List<ChatMessage>,
    val temperature: Float = AIConfig.defaultTemperature,
    @SerializedName("top_p")
    val topP: Float = AIConfig.defaultTopP,
    @SerializedName("max_tokens")
    val maxTokens: Int = AIConfig.defaultMaxTokens,
    val stream: Boolean = false,
    @SerializedName("extra_body")
    val extraBody: Map<String, Any>? = AIConfig.extraBody
)

data class ChatMessage(
    val role: String,
    val content: String
)

data class ChatResponse(
    val id: String?,
    val choices: List<Choice>?,
    val error: ApiError? = null
)

data class Choice(
    val index: Int?,
    val message: ChatMessage?,
    @SerializedName("finish_reason")
    val finishReason: String? = null
)

data class ApiError(
    val message: String?,
    val type: String? = null,
    val code: String? = null
)
