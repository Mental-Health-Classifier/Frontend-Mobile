package com.example.mindcare.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import kotlinx.coroutines.delay

@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
    charDelayMillis: Long = 45L,
    startDelayMillis: Long = 0L
) {
    var visibleChars by remember(text) { mutableStateOf(0) }
    var showCursor by remember(text) { mutableStateOf(true) }

    LaunchedEffect(text) {
        visibleChars = 0
        delay(startDelayMillis)
        while (visibleChars < text.length) {
            delay(charDelayMillis)
            visibleChars++
        }
    }

    LaunchedEffect(text) {
        while (visibleChars < text.length) {
            delay(450)
            showCursor = !showCursor
        }
        showCursor = false
    }

    val isTyping = visibleChars < text.length
    val displayed = text.take(visibleChars) + if (isTyping && showCursor) "|" else ""

    Text(
        text = displayed,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign
    )
}
