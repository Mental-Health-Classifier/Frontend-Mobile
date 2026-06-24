package com.example.mindcare.data.api

import com.example.mindcare.data.model.BaseResponse
import com.example.mindcare.data.model.ForgotPasswordRequest
import com.example.mindcare.data.model.LoginRequest
import com.example.mindcare.data.model.RegisterRequest
import com.example.mindcare.data.model.TokenResponse
import com.example.mindcare.data.model.UpdateMeRequest
import com.example.mindcare.data.model.UpdatePasswordRequest
import com.example.mindcare.data.model.UserResponse
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonElement

object AuthService {

    private val client = KtorClient.httpClient

    suspend fun register(request: RegisterRequest): ApiResult<BaseResponse<JsonElement>> =
        client.post("auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.toApiResult()

    suspend fun login(request: LoginRequest): ApiResult<BaseResponse<TokenResponse>> =
        client.post("auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.toApiResult()

    suspend fun getProfile(token: String): ApiResult<BaseResponse<UserResponse>> =
        client.get("auth/me") {
            header("Authorization", token)
        }.toApiResult()

    suspend fun updateProfile(token: String, request: UpdateMeRequest): ApiResult<BaseResponse<UserResponse>> =
        client.put("auth/me") {
            header("Authorization", token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.toApiResult()

    suspend fun changePassword(token: String, request: UpdatePasswordRequest): ApiResult<BaseResponse<UserResponse>> =
        client.put("auth/me/password") {
            header("Authorization", token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.toApiResult()

    suspend fun forgotPassword(request: ForgotPasswordRequest): ApiResult<BaseResponse<JsonElement>> =
        client.post("auth/forgot-password") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.toApiResult()
}
