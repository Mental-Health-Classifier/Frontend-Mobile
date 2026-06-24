package com.example.mindcare.ui.screens.chat

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mindcare.data.AppSettings
import com.example.mindcare.data.api.AnalysisService
import com.example.mindcare.data.model.AnalysisSessionResponse
import com.example.mindcare.data.model.ChatRequest
import com.example.mindcare.data.model.WordScore
import com.example.mindcare.data.model.parseWordScores
import com.example.mindcare.navigation.Routes
import com.example.mindcare.platform.AudioRecorder
import com.example.mindcare.platform.rememberRequestAudioPermission
import com.example.mindcare.ui.components.ChatBubbleComponent
import com.example.mindcare.ui.components.ShimmerChatBubble
import com.example.mindcare.ui.components.TopBar
import com.example.mindcare.ui.components.TypewriterText
import com.example.mindcare.ui.components.VoiceWaveform
import com.example.mindcare.ui.components.dismissKeyboardOnTap
import com.example.mindcare.ui.theme.BackgroundGray
import com.example.mindcare.ui.theme.ButtonDisabledGray
import com.example.mindcare.ui.theme.PrimaryGreen
import com.example.mindcare.ui.theme.TextGray
import com.example.mindcare.util.formatDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mindcare.shared.generated.resources.Res
import mindcare.shared.generated.resources.ic_menu
import mindcare.shared.generated.resources.ic_mic
import mindcare.shared.generated.resources.ic_send
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

private fun buildMessageFromSession(session: AnalysisSessionResponse?, inputText: String): Message {
    val category = session?.category ?: ""
    val confidence = (session?.confidence ?: 0f) * 100f

    val depressionResult = session?.results?.find { it.resultType == "depression" }
    val anxietyResult = session?.results?.find { it.resultType == "anxiety" }
    val stressResult = session?.results?.find { it.resultType == "stress" }

    val depression = (depressionResult?.score ?: 0f) / 100f
    val anxiety = (anxietyResult?.score ?: 0f) / 100f
    val stress = (stressResult?.score ?: 0f) / 100f

    val depressionWords = parseWordScores(depressionResult?.detail)
    val anxietyWords = parseWordScores(anxietyResult?.detail)
    val stressWords = parseWordScores(stressResult?.detail)

    val dominantWords = when (category.lowercase()) {
        "anxiety" -> anxietyWords
        "depression" -> depressionWords
        "stress" -> stressWords
        else -> emptyList()
    }

    val keywords = dominantWords
        .filter { (it.score ?: 0f) > 0f }
        .sortedByDescending { it.score }
        .take(5)
        .mapNotNull { it.word }

    val categoryLabel = when (category.lowercase()) {
        "anxiety" -> "kecemasan"
        "depression" -> "depresi"
        "stress" -> "stres"
        else -> category
    }

    val isUndetected = category.isBlank() || (depression == 0f && anxiety == 0f && stress == 0f)

    val replyText = if (isUndetected) {
        "Analisis tidak dapat mendeteksi kondisi emosional yang signifikan dari teks ini."
    } else {
        buildString {
            append("Perasaan Anda menunjukkan tanda $categoryLabel ")
            append("dengan kepercayaan ${confidence.toInt()}%.")
            if (keywords.isNotEmpty()) {
                append(" Kata yang paling berpengaruh: ${keywords.joinToString(", ")}.")
            }
        }
    }

    return Message(
        text = replyText,
        isUser = false,
        depression = depression,
        anxiety = anxiety,
        stress = stress,
        dominantCategory = category,
        confidence = confidence,
        keywords = keywords,
        depressionWords = depressionWords,
        anxietyWords = anxietyWords,
        stressWords = stressWords,
        originalText = inputText
    )
}

data class Message(
    val text: String,
    val isUser: Boolean,
    val depression: Float = 0f,
    val anxiety: Float = 0f,
    val stress: Float = 0f,
    val dominantCategory: String = "",
    val confidence: Float = 0f,
    val keywords: List<String> = emptyList(),
    val depressionWords: List<WordScore> = emptyList(),
    val anxietyWords: List<WordScore> = emptyList(),
    val stressWords: List<WordScore> = emptyList(),
    val originalText: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, initialSessionId: String? = null) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    var textInput by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Message>() }
    var isLoading by remember { mutableStateOf(false) }
    var analysisCompleted by remember { mutableStateOf(false) }
    var currentLoadingId by remember { mutableStateOf<String?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    val audioRecorder = remember { AudioRecorder() }
    var voiceLevel by remember { mutableStateOf(0f) }
    val textFieldFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(drawerState) {
        snapshotFlow { drawerState.isOpen }.collect { isOpen ->
            if (isOpen) {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (true) {
                val amplitude = audioRecorder.getAmplitude()
                voiceLevel = (amplitude / 24000f).coerceIn(0f, 1f)
                delay(100)
            }
        } else {
            voiceLevel = 0f
        }
    }

    val tokenHeader = remember { "Bearer ${AppSettings.getToken() ?: ""}" }
    val autoVoice = remember { AppSettings.getBool("pref_auto_voice", false) }
    val saveHistory = remember { AppSettings.getBool("pref_save_history", true) }

    LaunchedEffect(Unit) {
        val token = AppSettings.getToken() ?: ""
        if (token.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar("Sesi habis, silakan login ulang.") }
            navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
        }
    }

    var historyList by remember { mutableStateOf<List<AnalysisSessionResponse>>(emptyList()) }
    var isLoadingHistory by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (!saveHistory) {
            isLoadingHistory = false
            return@LaunchedEffect
        }
        try {
            val response = AnalysisService.getAllSessions(tokenHeader)
            if (response.isSuccessful && response.body?.status == true) {
                historyList = response.body?.data ?: emptyList()
            }
        } catch (e: Exception) {
            // gagal memuat riwayat — biarkan daftar kosong
        } finally {
            isLoadingHistory = false
        }
    }

    LaunchedEffect(initialSessionId) {
        val sessionId = initialSessionId ?: return@LaunchedEffect
        currentLoadingId = sessionId
        messages.clear()
        analysisCompleted = false
        isLoading = true
        try {
            val response = AnalysisService.getSessionById(tokenHeader, sessionId)
            if (sessionId != currentLoadingId) return@LaunchedEffect
            if (response.isSuccessful && response.body?.status == true) {
                val session = response.body?.data
                val inputText = session?.inputText ?: ""
                messages.add(Message(inputText, isUser = true))
                messages.add(buildMessageFromSession(session, inputText))
                analysisCompleted = true
            }
        } catch (e: Exception) {
            // gagal memuat sesi awal
        } finally {
            if (sessionId == currentLoadingId) isLoading = false
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    fun handleErrorResponse(code: Int, rawErr: String) {
        when (code) {
            401 -> {
                AppSettings.clearToken()
                messages.add(Message("Sesi habis. Mengarahkan ke login...", false))
                scope.launch {
                    delay(1500)
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                }
            }
            422 -> messages.add(Message("Format data tidak valid: $rawErr", false))
            500 -> messages.add(Message("Server bermasalah. Coba lagi nanti.", false))
            else -> messages.add(Message("Error $code: $rawErr", false))
        }
    }

    fun loadSession(sessionId: String, inputText: String) {
        currentLoadingId = sessionId
        messages.clear()
        analysisCompleted = false
        messages.add(Message(inputText, isUser = true))
        isLoading = true
        scope.launch {
            try {
                val response = AnalysisService.getSessionById(tokenHeader, sessionId)
                if (sessionId != currentLoadingId) return@launch
                if (response.isSuccessful && response.body?.status == true) {
                    val session = response.body?.data
                    messages.add(buildMessageFromSession(session, inputText))
                    analysisCompleted = true
                } else {
                    messages.add(Message("Gagal memuat sesi: ${response.code}", false))
                }
            } catch (e: Exception) {
                if (sessionId != currentLoadingId) return@launch
                messages.add(Message("Gagal terhubung: ${e.message}", false))
            } finally {
                if (sessionId == currentLoadingId) isLoading = false
            }
        }
    }

    fun sendTextMessage(inputText: String) {
        isLoading = true
        scope.launch {
            try {
                val response = AnalysisService.sendTextMessage(tokenHeader, ChatRequest(message = inputText))

                if (!response.isSuccessful || response.body?.status != true) {
                    handleErrorResponse(response.code, response.errorBody ?: "")
                    return@launch
                }

                val session = response.body?.data
                messages.add(buildMessageFromSession(session, inputText))
                analysisCompleted = true
            } catch (e: Exception) {
                messages.add(Message("Gagal terhubung: ${e.message}", false))
            } finally {
                isLoading = false
            }
        }
    }

    fun sendAudio(audioBytes: ByteArray) {
        messages.add(Message("[Mengirim rekaman audio...]", true))
        isLoading = true
        scope.launch {
            try {
                val response = AnalysisService.sendAudioMessage(tokenHeader, "voice_record.mp3", audioBytes)

                if (!response.isSuccessful || response.body?.status != true) {
                    handleErrorResponse(response.code, response.errorBody ?: "")
                    return@launch
                }

                val session = response.body?.data
                val inputText = session?.inputText ?: ""

                if (inputText.isBlank()) {
                    messages.add(Message("Audio diterima tapi teks tidak terdeteksi.", false))
                    return@launch
                }

                messages.add(Message("[Teks terdeteksi: \"$inputText\"]", false))
                messages.add(buildMessageFromSession(session, inputText))
                analysisCompleted = true
            } catch (e: Exception) {
                messages.add(Message("Koneksi bermasalah: ${e.message}", false))
            } finally {
                isLoading = false
            }
        }
    }

    fun startRecording() {
        if (isRecording) return
        val started = audioRecorder.start()
        if (started) {
            isRecording = true
            scope.launch { snackbarHostState.showSnackbar("Merekam suara...") }
        } else {
            scope.launch { snackbarHostState.showSnackbar("Gagal aktifkan mic.") }
        }
    }

    val requestAudioPermission = rememberRequestAudioPermission { granted ->
        if (granted) {
            startRecording()
        } else {
            scope.launch { snackbarHostState.showSnackbar("Izin mikrofon ditolak!") }
        }
    }

    fun toggleRecording() {
        if (isLoading) return
        if (!isRecording) {
            requestAudioPermission()
        } else {
            isRecording = false
            val bytes = audioRecorder.stop()
            if (bytes != null) {
                sendAudio(bytes)
            } else {
                scope.launch { snackbarHostState.showSnackbar("Gagal stop rekaman.") }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (autoVoice && initialSessionId == null && !isRecording) {
            delay(800)
            requestAudioPermission()
        } else if (initialSessionId == null) {
            delay(300)
            textFieldFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

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
                        .background(PrimaryGreen, RoundedCornerShape(12.dp))
                        .clickable {
                            scope.launch { drawerState.close() }
                            messages.clear()
                            textInput = ""
                            analysisCompleted = false
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

                if (!saveHistory) {
                    Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                        Text("Riwayat dinonaktifkan", color = TextGray, fontSize = 14.sp)
                    }
                } else if (isLoadingHistory) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen, modifier = Modifier.size(24.dp))
                    }
                } else {
                    LazyColumn(Modifier.padding(horizontal = 12.dp)) {
                        items(historyList.reversed().take(20), key = { it.id ?: it.hashCode() }) { item ->
                            var rawOffset by remember { mutableFloatStateOf(0f) }
                            val animatedOffset by animateFloatAsState(
                                targetValue = rawOffset,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                label = "swipe"
                            )
                            val swipeThreshold = 90f

                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(PrimaryGreen, RoundedCornerShape(12.dp))
                                        .padding(start = 16.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        "Buka →",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                                        .clickable {
                                            scope.launch { drawerState.close() }
                                            val id = item.id ?: return@clickable
                                            loadSession(id, item.inputText ?: "")
                                        }
                                        .pointerInput(item.id) {
                                            detectHorizontalDragGestures(
                                                onDragEnd = {
                                                    val id = item.id
                                                    if (rawOffset > swipeThreshold && id != null) {
                                                        scope.launch { drawerState.close() }
                                                        loadSession(id, item.inputText ?: "")
                                                    }
                                                    rawOffset = 0f
                                                },
                                                onDragCancel = { rawOffset = 0f },
                                                onHorizontalDrag = { _, delta ->
                                                    rawOffset = (rawOffset + delta).coerceIn(0f, 200f)
                                                }
                                            )
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    Column(Modifier.padding(14.dp)) {
                                        Text(
                                            text = item.inputText ?: "",
                                            fontSize = 15.sp,
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
                                                Text(
                                                    text = timeStr,
                                                    fontSize = 12.sp,
                                                    color = TextGray
                                                )
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
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    navController = navController,
                    currentScreen = "Chat",
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .background(BackgroundGray)
                    .dismissKeyboardOnTap()
                    .padding(innerPadding)
                    .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
            ) {

                if (messages.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Halo,", fontSize = 16.sp, color = PrimaryGreen, fontWeight = FontWeight.Medium)
                            TypewriterText(
                                text = "Bagaimana perasaanmu hari ini?",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen,
                                startDelayMillis = 300L
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(messages) { msg ->
                            ChatBubbleComponent(msg)
                        }
                        if (isLoading) {
                            item {
                                ShimmerChatBubble()
                            }
                        }
                    }
                }

                Surface(Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 8.dp) {
                    if (analysisCompleted) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .background(PrimaryGreen, RoundedCornerShape(16.dp))
                                .clickable {
                                    messages.clear()
                                    textInput = ""
                                    analysisCompleted = false
                                }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "+ Analisis Baru",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isRecording) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(52.dp)
                                        .background(BackgroundGray, RoundedCornerShape(16.dp))
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Merekam...", color = TextGray, fontSize = 14.sp)
                                    VoiceWaveform(
                                        level = voiceLevel,
                                        color = PrimaryGreen,
                                        modifier = Modifier.height(28.dp)
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    IconButton(onClick = { toggleRecording() }) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_mic),
                                            contentDescription = "Stop",
                                            tint = Color.Red,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            } else {
                                TextField(
                                    value = textInput,
                                    onValueChange = { textInput = it },
                                    placeholder = {
                                        Text(
                                            if (isLoading) "Menganalisis..." else "Tulis perasaan kamu...",
                                            color = TextGray
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { toggleRecording() }) {
                                            Icon(
                                                painter = painterResource(Res.drawable.ic_mic),
                                                contentDescription = "Record",
                                                tint = TextGray,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    },
                                    modifier = Modifier.weight(1f).focusRequester(textFieldFocusRequester),
                                    enabled = !isLoading,
                                    maxLines = 5,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = BackgroundGray,
                                        unfocusedContainerColor = BackgroundGray,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        cursorColor = PrimaryGreen
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }

                            val sendButtonDisabled = isLoading || isRecording
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(
                                        if (sendButtonDisabled) ButtonDisabledGray else PrimaryGreen,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable(enabled = !sendButtonDisabled) {
                                        val currentText = textInput.trim()
                                        if (currentText.isNotBlank()) {
                                            messages.add(Message(currentText, true))
                                            textInput = ""
                                            sendTextMessage(currentText)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painterResource(Res.drawable.ic_send), "Send",
                                    tint = if (sendButtonDisabled) TextGray else Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
