package com.example.mindcare.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LastSessionStat(
    val category: String? = null,
    val confidence: Float? = null
)

@Serializable
data class DominantConditionStat(
    val category: String? = null,
    @SerialName("avg_confidence") val avgConfidence: Float? = null
)

@Serializable
data class WeeklyTrendDay(
    val day: String,
    @SerialName("stress_avg") val stressAvg: Float = 0f,
    @SerialName("depression_avg") val depressionAvg: Float = 0f,
    @SerialName("anxiety_avg") val anxietyAvg: Float = 0f
)

@Serializable
data class DashboardStatsResponse(
    @SerialName("total_analyses_7d") val totalAnalyses7d: Int = 0,
    @SerialName("last_session") val lastSession: LastSessionStat? = null,
    @SerialName("dominant_condition") val dominantCondition: DominantConditionStat? = null,
    @SerialName("weekly_trend") val weeklyTrend: List<WeeklyTrendDay> = emptyList()
)
