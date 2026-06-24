package com.example.mindcare.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonElement

@Serializable
data class BaseResponse<T>(
    val status: Boolean,
    val message: String,
    val data: T? = null,
    val errors: JsonElement? = null
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class ForgotPasswordRequest(
    val email: String
)
