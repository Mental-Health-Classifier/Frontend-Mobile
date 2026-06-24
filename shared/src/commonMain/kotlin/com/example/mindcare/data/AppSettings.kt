package com.example.mindcare.data

import com.example.mindcare.platform.createSettings

object AppSettings {
    private val settings = createSettings("auth_prefs")

    fun getToken(): String? = settings.getStringOrNull("access_token")

    fun setToken(token: String) {
        settings.putString("access_token", token)
    }

    fun clearToken() {
        settings.remove("access_token")
        settings.remove("token")
    }

    fun getBool(key: String, default: Boolean): Boolean = settings.getBoolean(key, default)

    fun setBool(key: String, value: Boolean) {
        settings.putBoolean(key, value)
    }
}
