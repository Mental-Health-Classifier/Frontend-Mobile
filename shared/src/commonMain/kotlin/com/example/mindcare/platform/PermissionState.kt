package com.example.mindcare.platform

import androidx.compose.runtime.Composable

@Composable
expect fun rememberRequestAudioPermission(onResult: (Boolean) -> Unit): () -> Unit

@Composable
expect fun rememberRequestNotificationPermission(onResult: (Boolean) -> Unit): () -> Unit
