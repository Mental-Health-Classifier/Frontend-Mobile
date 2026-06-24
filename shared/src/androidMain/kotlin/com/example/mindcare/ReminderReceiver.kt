package com.example.mindcare

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.mindcare.shared.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, "mindcare_reminder")
            .setSmallIcon(R.drawable.ic_setting)
            .setContentTitle("MindCare — Check-in Harian")
            .setContentText("Bagaimana perasaanmu hari ini? Yuk ceritakan ke MindCare.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        nm.notify(1001, notification)
    }
}
