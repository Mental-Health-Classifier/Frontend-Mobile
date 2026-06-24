package com.example.mindcare.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class ChatRequest(
    @SerialName("input_text") val message: String
)

@Serializable
data class AnalysisSessionResponse(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("input_text") val inputText: String? = null,
    val category: String? = null,
    val confidence: Float? = null,
    @SerialName("model_version") val modelVersion: String? = null,
    val method: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val results: List<AnalysisResultResponse>? = null
)

@Serializable
data class AnalysisResultResponse(
    val id: String? = null,
    @SerialName("analysis_session_id") val analysisSessionId: String? = null,
    @SerialName("result_type") val resultType: String? = null,
    val score: Float? = null,
    val detail: JsonElement? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

data class WordScore(
    val word: String?,
    val score: Float?
)

// Parse detail dari JsonElement ke List<WordScore>
// Mendukung format [[word, score], ...] maupun [{"word": w, "score": s}, ...]
fun parseWordScores(detail: JsonElement?): List<WordScore> {
    if (detail == null) return emptyList()
    val array = runCatching { detail.jsonArray }.getOrNull() ?: return emptyList()
    return array.mapNotNull { element ->
        runCatching {
            val arr = element.jsonArray
            if (arr.size >= 2) {
                WordScore(arr[0].jsonPrimitive.content, arr[1].jsonPrimitive.content.toFloat())
            } else null
        }.getOrElse {
            runCatching {
                val obj = element.jsonObject
                WordScore(
                    obj["word"]?.jsonPrimitive?.content,
                    obj["score"]?.jsonPrimitive?.content?.toFloatOrNull()
                )
            }.getOrNull()
        }
    }
}

// TOKEN RESPONSE
@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String = "bearer"
)

// AUTH REQUEST MODELS
@Serializable
data class UpdateMeRequest(
    val name: String? = null,
    val email: String? = null
)

@Serializable
data class UpdatePasswordRequest(
    @SerialName("old_password") val oldPassword: String,
    @SerialName("new_password") val newPassword: String
)
