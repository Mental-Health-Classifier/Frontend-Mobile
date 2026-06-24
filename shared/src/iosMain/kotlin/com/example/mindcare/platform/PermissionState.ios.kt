package com.example.mindcare.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

// Stub — selalu "granted" karena rekam audio & notifikasi belum aktif di iOS (lihat plan migrasi).
@Composable
actual fun rememberRequestAudioPermission(onResult: (Boolean) -> Unit): () -> Unit {
    val callback by rememberUpdatedState(onResult)
    return { callback(true) }
}

@Composable
actual fun rememberRequestNotificationPermission(onResult: (Boolean) -> Unit): () -> Unit {
    val callback by rememberUpdatedState(onResult)
    return { callback(true) }
}
