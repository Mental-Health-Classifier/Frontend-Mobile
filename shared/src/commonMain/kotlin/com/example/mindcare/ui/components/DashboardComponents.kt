package com.example.mindcare.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindcare.ui.theme.TextGray
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private val CardGreen = Color(0xFF2D6A4F)
val ColorStress = Color(0xFFE57373)
val ColorDepression = Color(0xFF64B5F6)
val ColorAnxiety = Color(0xFFFFB74D)

@Composable
fun DashStatCard(
    label: String,
    title: String,
    subtitle: String,
    icon: DrawableResource,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardGreen),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(Modifier.fillMaxWidth().padding(14.dp)) {
            Column {
                Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            }
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp).align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
fun ChartLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(width = 20.dp, height = 12.dp)) {
            drawLine(color, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = 3f)
            drawCircle(color, radius = 4f, center = Offset(size.width / 2, size.height / 2))
            drawCircle(Color.White, radius = 2.5f, center = Offset(size.width / 2, size.height / 2))
        }
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 13.sp, color = TextGray)
    }
}

@Composable
fun WeeklyTrendChart(
    data: List<Triple<Float, Float, Float>>,
    dayLabels: List<String>
) {
    val yMax = 100f
    val yMin = 0f
    val yLabels = listOf(100, 75, 50, 25, 0)

    Row(Modifier.fillMaxWidth().height(200.dp)) {
        Column(
            Modifier.width(28.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            yLabels.forEach { v ->
                Text("$v", fontSize = 10.sp, color = TextGray)
            }
        }

        Box(Modifier.weight(1f).fillMaxHeight()) {
            Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val stepX = w / (data.size - 1).coerceAtLeast(1)
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)

                yLabels.forEach { v ->
                    val y = h - ((v - yMin) / (yMax - yMin)) * h
                    drawLine(
                        Color(0xFFE0E0E0), Offset(0f, y), Offset(w, y),
                        strokeWidth = 1f, pathEffect = dashEffect
                    )
                }

                fun drawSeries(values: List<Float>, color: Color) {
                    if (values.size < 2) return
                    val path = Path()
                    values.forEachIndexed { i, v ->
                        val x = stepX * i
                        val y = h - ((v - yMin) / (yMax - yMin)) * h
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(path, color, style = Stroke(width = 2.5f))
                    values.forEachIndexed { i, v ->
                        val x = stepX * i
                        val y = h - ((v - yMin) / (yMax - yMin)) * h
                        drawCircle(color, radius = 5f, center = Offset(x, y))
                        drawCircle(Color.White, radius = 3f, center = Offset(x, y))
                    }
                }

                drawSeries(data.map { it.first }, ColorStress)
                drawSeries(data.map { it.second }, ColorDepression)
                drawSeries(data.map { it.third }, ColorAnxiety)
            }

            Row(
                Modifier.fillMaxWidth().align(Alignment.BottomStart),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayLabels.forEach { day ->
                    Text(day, fontSize = 10.sp, color = TextGray, modifier = Modifier.width(28.dp))
                }
            }
        }
    }
}
