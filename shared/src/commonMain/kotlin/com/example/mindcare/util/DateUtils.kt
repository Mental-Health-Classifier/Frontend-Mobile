package com.example.mindcare.util

import kotlinx.datetime.LocalDateTime

private val monthAbbrId = listOf(
    "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
    "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
)

// Format ISO-8601 datetime ("yyyy-MM-ddTHH:mm:ss...") menjadi "dd MMM, HH:mm" (lokal Indonesia)
fun formatDateTime(createdAt: String?): String {
    if (createdAt == null) return ""
    return try {
        val dt = LocalDateTime.parse(createdAt.take(19))
        val day = dt.dayOfMonth.toString().padStart(2, '0')
        val month = monthAbbrId[dt.monthNumber - 1]
        val hour = dt.hour.toString().padStart(2, '0')
        val minute = dt.minute.toString().padStart(2, '0')
        "$day $month, $hour:$minute"
    } catch (e: Exception) {
        ""
    }
}

// 1 = Senin ... 7 = Minggu, mengikuti ISO day number
fun parseDayOfWeek(createdAt: String?): Int? {
    if (createdAt == null) return null
    return try {
        val datePart = createdAt.take(10)
        val dt = LocalDateTime.parse("${datePart}T00:00:00")
        dt.dayOfWeek.ordinal + 1
    } catch (e: Exception) {
        null
    }
}
