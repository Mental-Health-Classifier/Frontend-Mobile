package com.example.mindcare.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val barMultipliers = listOf(0.5f, 0.8f, 1f, 0.7f, 0.9f, 0.6f)

@Composable
fun VoiceWaveform(
    level: Float,
    color: Color,
    modifier: Modifier = Modifier,
    barCount: Int = barMultipliers.size
) {
    val clampedLevel = level.coerceIn(0f, 1f)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        repeat(barCount) { index ->
            val multiplier = barMultipliers[index % barMultipliers.size]
            val targetHeight = (4 + clampedLevel * 24f * multiplier).dp
            val animatedHeight by animateDpAsState(
                targetValue = targetHeight,
                animationSpec = tween(120),
                label = "voiceBar$index"
            )
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(animatedHeight)
                    .background(color, RoundedCornerShape(2.dp))
            )
        }
    }
}
