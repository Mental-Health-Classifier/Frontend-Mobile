package com.example.mindcare.data.api

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class ApiResult<T>(
    val isSuccessful: Boolean,
    val code: Int,
    val body: T?,
    val errorBody: String?
)

suspend inline fun <reified T> HttpResponse.toApiResult(): ApiResult<T> {
    return if (status.value in 200..299) {
        ApiResult(isSuccessful = true, code = status.value, body = body<T>(), errorBody = null)
    } else {
        ApiResult(isSuccessful = false, code = status.value, body = null, errorBody = bodyAsText())
    }
}

// Ekstrak field "message" dari body error backend (format BaseResponse), jatuh ke [fallback] jika gagal di-parse.
fun extractErrorMessage(errorBody: String?, fallback: String): String {
    if (errorBody.isNullOrBlank()) return fallback
    return try {
        Json.parseToJsonElement(errorBody).jsonObject["message"]?.jsonPrimitive?.contentOrNull ?: fallback
    } catch (e: Exception) {
        fallback
    }
}
