package com.example.mindcare.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindcare.data.model.WordScore
import com.example.mindcare.ui.screens.chat.Message
import com.example.mindcare.ui.theme.PrimaryGreen

@Composable
fun ChatBubbleComponent(message: Message, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(220),
        label = "bubbleAlpha"
    )
    val slideY by animateFloatAsState(
        targetValue = if (visible) 0f else 18f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bubbleSlide"
    )

    Column(
        modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha; translationY = slideY },
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 0.dp,
                bottomEnd = if (message.isUser) 0.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) PrimaryGreen else Color(0xFFEBF3F0)
            ),
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Text(
                message.text,
                color = if (message.isUser) Color.White else Color.Black,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(12.dp)
            )
        }

        val depressionPct = (message.depression * 100).toInt()
        val anxietyPct = (message.anxiety * 100).toInt()
        val stressPct = (message.stress * 100).toInt()
        val allZero = depressionPct == 0 && anxietyPct == 0 && stressPct == 0

        if (!message.isUser && allZero && message.dominantCategory.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Card(
                modifier = Modifier.width(290.dp).padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⚠", fontSize = 24.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Tidak Terdeteksi",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Text(
                            "Analisis tidak menghasilkan skor yang signifikan.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        if (!message.isUser && !allZero && (message.depression > 0f || message.anxiety > 0f || message.stress > 0f)) {
            Spacer(Modifier.height(6.dp))
            Card(
                modifier = Modifier.width(290.dp).padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(Modifier.padding(12.dp)) {

                    val dominantColor = when (message.dominantCategory.lowercase()) {
                        "depression" -> Color(0xFF1E88E5)
                        "anxiety" -> Color(0xFF9575CD)
                        "stress" -> Color(0xFFE57373)
                        else -> PrimaryGreen
                    }
                    val dominantLabel = when (message.dominantCategory.lowercase()) {
                        "anxiety" -> "Kecemasan"
                        "depression" -> "Depresi"
                        "stress" -> "Stres"
                        else -> message.dominantCategory
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(dominantColor.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                            .border(1.dp, dominantColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Hasil Dominan",
                                    fontSize = 11.sp,
                                    color = dominantColor.copy(alpha = 0.7f)
                                )
                                Text(
                                    dominantLabel.uppercase(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = dominantColor
                                )
                            }
                            Text(
                                "${message.confidence.toInt()}%",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = dominantColor
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    Text("Probabilitas:", fontSize = 13.sp, color = Color.Gray)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp))
                    ) {
                        if (message.depression > 0f)
                            Box(Modifier.weight(message.depression).fillMaxHeight().background(Color(0xFF1E88E5)))
                        if (message.anxiety > 0f)
                            Box(Modifier.weight(message.anxiety).fillMaxHeight().background(Color(0xFF9575CD)))
                        if (message.stress > 0f)
                            Box(Modifier.weight(message.stress).fillMaxHeight().background(Color(0xFFE57373)))
                    }
                    Spacer(Modifier.height(10.dp))

                    EmotionLegendItem(Color(0xFF1E88E5), "Depresi", message.depression)
                    Spacer(Modifier.height(4.dp))
                    EmotionLegendItem(Color(0xFF9575CD), "Kecemasan", message.anxiety)
                    Spacer(Modifier.height(4.dp))
                    EmotionLegendItem(Color(0xFFE57373), "Stres", message.stress)

                    if (message.keywords.isNotEmpty() || message.depressionWords.isNotEmpty() ||
                        message.anxietyWords.isNotEmpty() || message.stressWords.isNotEmpty()
                    ) {

                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        Spacer(Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("ⓘ", fontSize = 14.sp, color = PrimaryGreen)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Penjelasan (Explainable AI)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.Black
                            )
                        }
                        Spacer(Modifier.height(8.dp))

                        if (message.keywords.isNotEmpty()) {
                            Text("Kata kunci terdeteksi:", fontSize = 13.sp, color = Color.Gray)
                            Spacer(Modifier.height(4.dp))
                            KeywordChipRow(message.keywords)
                            Spacer(Modifier.height(8.dp))
                        }

                        if (message.originalText.isNotBlank()) {
                            HighlightedText(
                                originalText = message.originalText,
                                depressionWords = message.depressionWords,
                                anxietyWords = message.anxietyWords,
                                stressWords = message.stressWords
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmotionLegendItem(color: Color, label: String, percentage: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text("$label ${(percentage * 100).toInt()}%", fontSize = 14.sp, color = Color.DarkGray)
    }
}

@Composable
fun KeywordChipRow(keywords: List<String>) {
    Column {
        keywords.chunked(3).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                row.forEach { kw ->
                    Box(
                        Modifier
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(kw, fontSize = 12.sp, color = PrimaryGreen, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HighlightedText(
    originalText: String,
    depressionWords: List<WordScore>,
    anxietyWords: List<WordScore>,
    stressWords: List<WordScore>
) {
    val depressionColor = Color(0xFF1E88E5)
    val anxietyColor = Color(0xFF9575CD)
    val stressColor = Color(0xFFE57373)

    data class WordLabel(val color: Color, val score: Float)
    val wordColorMap = mutableMapOf<String, WordLabel>()

    fun processWords(words: List<WordScore>, color: Color) {
        words.forEach { ws ->
            val word = ws.word?.lowercase() ?: return@forEach
            val score = ws.score ?: 0f
            if (score > 0f) {
                val existing = wordColorMap[word]
                if (existing == null || score > existing.score) {
                    wordColorMap[word] = WordLabel(color, score)
                }
            }
        }
    }

    processWords(depressionWords, depressionColor)
    processWords(anxietyWords, anxietyColor)
    processWords(stressWords, stressColor)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        val lines = originalText.split("\n")

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            lines.forEachIndexed { lineIndex, line ->
                if (line.isBlank()) {
                    if (lineIndex > 0) Spacer(Modifier.height(4.dp))
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        line.split(" ").filter { it.isNotEmpty() }.forEach { word ->
                            val cleanWord = word.lowercase().trim(',', '.', '!', '?', ';', ':')
                            val wordLabel = wordColorMap[cleanWord]
                            if (wordLabel != null) {
                                Box(
                                    modifier = Modifier
                                        .background(wordLabel.color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = word,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = wordLabel.color
                                    )
                                }
                            } else {
                                Text(text = word, fontSize = 14.sp, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
