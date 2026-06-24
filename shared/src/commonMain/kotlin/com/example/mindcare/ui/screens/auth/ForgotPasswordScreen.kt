package com.example.mindcare.ui.screens.auth

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mindcare.data.api.AuthService
import com.example.mindcare.data.model.ForgotPasswordRequest
import com.example.mindcare.navigation.Routes
import com.example.mindcare.ui.components.CustomTextField
import com.example.mindcare.ui.components.PrimaryButton
import com.example.mindcare.ui.components.dismissKeyboardOnTap
import kotlinx.coroutines.launch
import mindcare.shared.generated.resources.Res
import mindcare.shared.generated.resources.logo_transparent
import org.jetbrains.compose.resources.painterResource

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var entered by remember { mutableStateOf(false) }
    val cardAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(400),
        label = "alpha"
    )
    val cardSlide by animateFloatAsState(
        targetValue = if (entered) 0f else 60f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "slide"
    )
    LaunchedEffect(Unit) { entered = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .dismissKeyboardOnTap()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = cardAlpha; translationY = cardSlide }
                .background(Color.White, shape = RoundedCornerShape(28.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(Res.drawable.logo_transparent),
                contentDescription = "MindCare Logo",
                modifier = Modifier.size(130.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Masukkan emailmu dan kami akan\nmengirim instruksi reset kata sandi.",
                fontSize = 15.sp, color = Color(0xFF678E82),
                textAlign = TextAlign.Center, lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (isSuccess) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Instruksi reset kata sandi telah dikirim ke $email. Periksa kotak masuk emailmu.",
                        fontSize = 14.sp,
                        color = Color(0xFF2D6A4F),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(56.dp)
                        .background(Color(0xFF408A6E), shape = RoundedCornerShape(16.dp))
                        .clickable { navController.navigate(Routes.LOGIN) { popUpTo(Routes.LOGIN) { inclusive = true } } },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Kembali ke Login", fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Alamat Email", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        placeholder = "email@kamu.com",
                        isError = errorMessage != null,
                        keyboardType = KeyboardType.Email
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                PrimaryButton(
                    text = if (isLoading) "Mengirim..." else "Kirim Instruksi Reset",
                    enabled = email.contains("@") && !isLoading,
                    containerColor = Color(0xFF408A6E),
                    contentColor = Color.White,
                    onClick = {
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            try {
                                val response = AuthService.forgotPassword(
                                    ForgotPasswordRequest(email.trim())
                                )
                                if (response.isSuccessful && response.body?.status == true) {
                                    isSuccess = true
                                } else {
                                    errorMessage = response.body?.message ?: "Gagal mengirim email reset"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Koneksi bermasalah. Coba lagi."
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFFFFF), shape = CircleShape)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(text = "Ingat kata sandi?", fontSize = 13.sp, color = Color(0xFF527A6B))
                }
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(56.dp)
                        .border(width = 1.dp, color = Color(0x80E0EBE7), shape = RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFFFFF), shape = RoundedCornerShape(16.dp))
                        .clickable { navController.popBackStack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Kembali ke Login", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(21.dp))
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = Color(0xFFE0EBE7).copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Kesehatan mentalmu adalah prioritas kami.\nSemua percakapan dienkripsi dan bersifat rahasia.",
            fontSize = 13.sp, color = Color(0xFF527A6B),
            textAlign = TextAlign.Center, lineHeight = 20.sp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        )
    }
}
