package com.example.mindcare.data.api

import com.example.mindcare.data.model.AnalysisSessionResponse
import com.example.mindcare.data.model.BaseResponse
import com.example.mindcare.data.model.ChatRequest
import com.example.mindcare.data.model.DashboardStatsResponse
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonElement

object AnalysisService {

    private val client = KtorClient.httpClient

    suspend fun sendTextMessage(token: String, request: ChatRequest): ApiResult<BaseResponse<AnalysisSessionResponse>> =
        client.post("analysis/text") {
            header("Authorization", token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.toApiResult()

    suspend fun sendAudioMessage(
        token: String,
        fileName: String,
        audioBytes: ByteArray
    ): ApiResult<BaseResponse<AnalysisSessionResponse>> =
        client.post("analysis/audio") {
            header("Authorization", token)
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("file", audioBytes, Headers.build {
                            append(HttpHeaders.ContentType, "audio/mpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                        })
                    }
                )
            )
        }.toApiResult()

    suspend fun getAllSessions(token: String): ApiResult<BaseResponse<List<AnalysisSessionResponse>>> =
        client.get("analysis") {
            header("Authorization", token)
        }.toApiResult()

    suspend fun getDashboardStats(token: String): ApiResult<BaseResponse<DashboardStatsResponse>> =
        client.get("analysis/dashboard-stats") {
            header("Authorization", token)
        }.toApiResult()

    suspend fun getSessionById(token: String, id: String): ApiResult<BaseResponse<AnalysisSessionResponse>> =
        client.get("analysis/$id") {
            header("Authorization", token)
        }.toApiResult()

    suspend fun deleteSession(token: String, id: String): ApiResult<BaseResponse<JsonElement>> =
        client.delete("analysis/$id") {
            header("Authorization", token)
        }.toApiResult()
}
