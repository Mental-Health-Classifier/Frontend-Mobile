package com.example.mindcare.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mindcare.data.AppSettings
import com.example.mindcare.data.api.AnalysisService
import com.example.mindcare.data.model.AnalysisSessionResponse
import com.example.mindcare.data.model.DashboardStatsResponse
import com.example.mindcare.navigation.Routes
import com.example.mindcare.ui.components.ChartLegendItem
import com.example.mindcare.ui.components.ColorAnxiety
import com.example.mindcare.ui.components.ColorDepression
import com.example.mindcare.ui.components.ColorStress
import com.example.mindcare.ui.components.DashStatCard
import com.example.mindcare.ui.components.TopBar
import com.example.mindcare.ui.components.WeeklyTrendChart
import com.example.mindcare.ui.theme.BackgroundGray
import com.example.mindcare.ui.theme.PrimaryGreen
import com.example.mindcare.ui.theme.TextGray
import com.example.mindcare.util.formatDateTime
import kotlinx.coroutines.launch
import mindcare.shared.generated.resources.Res
import mindcare.shared.generated.resources.ic_analysis
import mindcare.shared.generated.resources.ic_arrowcondition
import mindcare.shared.generated.resources.ic_brain
import mindcare.shared.generated.resources.ic_menu
import org.jetbrains.compose.resources.painterResource

private val CardGreen = Color(0xFF2D6A4F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val tokenHeader = remember { "Bearer ${AppSettings.getToken() ?: ""}" }

    var historyList by remember { mutableStateOf<List<AnalysisSessionResponse>>(emptyList()) }
    var dashboardStats by remember { mutableStateOf<DashboardStatsResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val historyResponse = AnalysisService.getAllSessions(tokenHeader)
            if (historyResponse.isSuccessful && historyResponse.body?.status == true) {
                historyList = historyResponse.body?.data ?: emptyList()
            }

            val statsResponse = AnalysisService.getDashboardStats(tokenHeader)
            if (statsResponse.isSuccessful && statsResponse.body?.status == true) {
                dashboardStats = statsResponse.body?.data
            }
        } catch (e: Exception) {
            // koneksi gagal — biarkan daftar/stats kosong
        } finally {
            isLoading = false
        }
    }

    val totalAnalyses = dashboardStats?.totalAnalyses7d ?: 0
    val lastCategory = dashboardStats?.lastSession?.category?.replaceFirstChar { it.uppercase() } ?: "-"
    val lastConfidence = ((dashboardStats?.lastSession?.confidence ?: 0f) * 100).toInt()
    val dominantCategory = dashboardStats?.dominantCondition?.category?.replaceFirstChar { it.uppercase() } ?: "-"
    val dominantAvgConfidence = ((dashboardStats?.dominantCondition?.avgConfidence ?: 0f) * 100).toInt()

    val dayLabels = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
    val last7DaysData = dashboardStats?.weeklyTrend?.map {
        Triple(it.stressAvg * 100, it.depressionAvg * 100, it.anxietyAvg * 100)
    } ?: emptyList()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(300.dp), drawerContainerColor = BackgroundGray) {
                Spacer(Modifier.height(24.dp))
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Riwayat Sesi", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { scope.launch { drawerState.close() } }) {
                        Icon(painterResource(Res.drawable.ic_menu), null, tint = PrimaryGreen)
                    }
                }
                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(CardGreen, RoundedCornerShape(12.dp))
                        .clickable {
                            scope.launch { drawerState.close() }
                            navController.navigate(Routes.CHAT)
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("+ ", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Analisis Baru", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(12.dp))

                LazyColumn(Modifier.padding(horizontal = 12.dp)) {
                    items(historyList.reversed().take(20)) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    scope.launch { drawerState.close() }
                                    val id = item.id ?: return@clickable
                                    navController.navigate(Routes.chatWithSession(id))
                                },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text(
                                    text = item.inputText ?: "",
                                    fontSize = 13.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val confidencePct = ((item.confidence ?: 0f) * 100).toInt()
                                    if (item.category != null && confidencePct > 0) {
                                        val categoryLabel = when (item.category.lowercase()) {
                                            "anxiety" -> "Kecemasan"
                                            "depression" -> "Depresi"
                                            "stress" -> "Stres"
                                            else -> item.category
                                        }
                                        Text(
                                            text = "$categoryLabel · $confidencePct%",
                                            fontSize = 13.sp,
                                            color = PrimaryGreen
                                        )
                                    } else {
                                        Text(
                                            text = "Tidak Terdeteksi",
                                            fontSize = 13.sp,
                                            color = TextGray
                                        )
                                    }
                                    val timeStr = formatDateTime(item.createdAt)
                                    if (timeStr.isNotEmpty()) {
                                        Text(text = timeStr, fontSize = 12.sp, color = TextGray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) {
        LazyColumn(
            Modifier.fillMaxSize().background(BackgroundGray),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                TopBar(
                    navController = navController,
                    currentScreen = "Dashboard",
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }

            item {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    Spacer(Modifier.height(24.dp))
                    Text("Dashboard Kesehatan Mental", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Lacak pola emosi dan pahami tren kesehatan mentalmu",
                        fontSize = 15.sp, color = TextGray,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                    )

                    if (isLoading) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                            CircularProgressIndicator(color = PrimaryGreen)
                        }
                    }
                    AnimatedVisibility(
                        visible = !isLoading,
                        enter = fadeIn(tween(350)) + slideInVertically(
                            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)
                        ) { it / 4 }
                    ) {
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                DashStatCard(
                                    label = "Status Terakhir",
                                    title = lastCategory,
                                    subtitle = "$lastConfidence% kepercayaan",
                                    icon = Res.drawable.ic_brain,
                                    modifier = Modifier.weight(1f)
                                )
                                DashStatCard(
                                    label = "Total Analisis",
                                    title = "$totalAnalyses",
                                    subtitle = "7 hari terakhir",
                                    icon = Res.drawable.ic_analysis,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Row(Modifier.fillMaxWidth()) {
                                DashStatCard(
                                    label = "Kondisi Dominan",
                                    title = dominantCategory,
                                    subtitle = "Rata-rata $dominantAvgConfidence% minggu ini",
                                    icon = Res.drawable.ic_arrowcondition,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.weight(1f))
                            }

                            Spacer(Modifier.height(20.dp))

                            Card(
                                Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        "Tren Kondisi (7 Hari Terakhir)",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(12.dp))

                                    if (last7DaysData.all { it.first == 0f && it.second == 0f && it.third == 0f }) {
                                        Box(Modifier.fillMaxWidth().height(180.dp), Alignment.Center) {
                                            Text("Belum ada data trend", color = TextGray)
                                        }
                                    } else {
                                        WeeklyTrendChart(data = last7DaysData, dayLabels = dayLabels)
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        ChartLegendItem(ColorStress, "Stres")
                                        Spacer(Modifier.width(16.dp))
                                        ChartLegendItem(ColorDepression, "Depresi")
                                        Spacer(Modifier.width(16.dp))
                                        ChartLegendItem(ColorAnxiety, "Kecemasan")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
