package com.example.mindcare.navigation

object Routes {
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val FORGOT_PASSWORD = "forgot_password"
    const val DASHBOARD = "dashboard"
    const val SETTINGS = "settings"
    const val CHAT = "chat"

    fun chatWithSession(sessionId: String) = "chat?sessionId=$sessionId"
}
