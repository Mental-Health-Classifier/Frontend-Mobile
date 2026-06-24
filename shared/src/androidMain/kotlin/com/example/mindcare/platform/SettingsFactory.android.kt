package com.example.mindcare.platform

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

actual fun createSettings(name: String): Settings =
    SharedPreferencesSettings(ContextHolder.context.getSharedPreferences(name, Context.MODE_PRIVATE))
