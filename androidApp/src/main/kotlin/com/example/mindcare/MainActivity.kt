package com.example.mindcare

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.mindcare.platform.ContextHolder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ContextHolder.context = this
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        createNotificationChannel()

        setContent {
            App()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "mindcare_reminder",
                "Pengingat Check-in",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Pengingat harian untuk check-in kesehatan mental"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
