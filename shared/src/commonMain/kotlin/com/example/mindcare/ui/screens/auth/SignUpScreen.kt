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
import com.example.mindcare.data.model.RegisterRequest
import com.example.mindcare.navigation.Routes
import com.example.mindcare.ui.components.CustomTextField
import com.example.mindcare.ui.components.PrimaryButton
import com.example.mindcare.ui.components.dismissKeyboardOnTap
import kotlinx.coroutines.launch
import mindcare.shared.generated.resources.Res
import mindcare.shared.generated.resources.logo_transparent
import org.jetbrains.compose.resources.painterResource

@Composable
fun SignUpScreen(navController: NavController) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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

    val isButtonEnabled = name.isNotBlank() &&
            email.contains("@") &&
            password.length >= 6 &&
            confirmPassword == password &&
            !isLoading

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
                text = "Mulai perjalanan\nkesehatan mentalmu hari ini",
                fontSize = 15.sp, color = Color(0xFF527A6B),
                textAlign = TextAlign.Center, lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column {
                    Text(text = "Nama Lengkap", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(2.dp))
                    CustomTextField(value = name, onValueChange = { name = it; errorMessage = null }, placeholder = "Nama lengkap")
                }
                Column {
                    Text(text = "Alamat Email", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(2.dp))
                    CustomTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        placeholder = "email@kamu.com",
                        keyboardType = KeyboardType.Email
                    )
                }
                Column {
                    Text(text = "Password", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(2.dp))
                    CustomTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        placeholder = "••••••••",
                        isPassword = true
                    )
                }
                Column {
                    Text(text = "Konfirmasi Password", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(2.dp))
                    CustomTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMessage = null },
                        placeholder = "••••••••",
                        isPassword = true,
                        isError = confirmPassword.isNotEmpty() && confirmPassword != password
                    )
                }
            }

            if (errorMessage != null) {
                Text(text = errorMessage!!, color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = if (isLoading) "Memproses..." else "Buat Akun",
                enabled = isButtonEnabled,
                containerColor = Color(0xFF408A6E),
                contentColor = Color.White,
                onClick = {
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        try {
                            val response = AuthService.register(
                                RegisterRequest(
                                    name = name.trim(),
                                    email = email.trim(),
                                    password = password.trim()
                                )
                            )
                            if (response.isSuccessful && response.body?.status == true) {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(Routes.SIGN_UP) { inclusive = true }
                                }
                            } else {
                                errorMessage = response.body?.message ?: "Registration failed"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Connection error"
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
                Text(text = "Sudah punya akun?", fontSize = 13.sp, color = Color(0xFF527A6B))
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
                Text(text = "Masuk", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Kesehatan mentalmu adalah prioritas kami.\nSemua percakapan dienkripsi dan bersifat rahasia.",
            fontSize = 13.sp, color = Color(0xFF527A6B),
            textAlign = TextAlign.Center, lineHeight = 16.sp
        )
    }
}
