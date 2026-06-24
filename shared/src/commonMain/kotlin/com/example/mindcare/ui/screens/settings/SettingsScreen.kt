package com.example.mindcare.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mindcare.data.AppSettings
import com.example.mindcare.data.api.AuthService
import com.example.mindcare.data.model.UpdateMeRequest
import com.example.mindcare.data.model.UpdatePasswordRequest
import com.example.mindcare.navigation.Routes
import com.example.mindcare.platform.ReminderScheduler
import com.example.mindcare.platform.rememberRequestNotificationPermission
import com.example.mindcare.ui.components.CustomTextField
import com.example.mindcare.ui.components.dismissKeyboardOnTap
import com.example.mindcare.ui.theme.BackgroundGray
import com.example.mindcare.ui.theme.PrimaryGreen
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    val tokenHeader = remember { "Bearer ${AppSettings.getToken() ?: ""}" }

    // Profile fields
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Password fields
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Preferences
    var saveHistory by remember { mutableStateOf(AppSettings.getBool("pref_save_history", false)) }
    var autoVoice by remember { mutableStateOf(AppSettings.getBool("pref_auto_voice", false)) }
    var notifications by remember { mutableStateOf(AppSettings.getBool("pref_notifications", false)) }

    val requestNotificationPermission = rememberRequestNotificationPermission { granted ->
        if (!granted) {
            notifications = false
            AppSettings.setBool("pref_notifications", false)
        } else {
            ReminderScheduler.schedule()
        }
    }

    var isLoadingProfile by remember { mutableStateOf(true) }
    var isSavingProfile by remember { mutableStateOf(false) }
    var isUpdatingPassword by remember { mutableStateOf(false) }
    var profileMessage by remember { mutableStateOf<String?>(null) }
    var profileMessageIsError by remember { mutableStateOf(false) }
    var passwordMessage by remember { mutableStateOf<String?>(null) }
    var passwordMessageIsError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val response = AuthService.getProfile(tokenHeader)
            if (response.isSuccessful && response.body?.status == true) {
                val user = response.body?.data
                fullName = user?.name ?: ""
                email = user?.email ?: ""
            }
        } catch (e: Exception) {
            // gagal memuat profil
        } finally {
            isLoadingProfile = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .dismissKeyboardOnTap()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.offset(x = (-12).dp)
            ) {
                Icon(Icons.Default.ArrowBack, "Back", tint = PrimaryGreen)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    AppSettings.clearToken()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                Icon(Icons.Default.ExitToApp, "Logout", tint = PrimaryGreen)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Keluar", color = PrimaryGreen, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Pengaturan", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(
            "Kelola akun dan preferensimu",
            fontSize = 15.sp, color = PrimaryGreen,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        SettingCard(title = "Manajemen Profil") {
            if (isLoadingProfile) {
                Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryGreen, modifier = Modifier.size(24.dp))
                }
            } else {
                Text("Nama Lengkap", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                CustomTextField(
                    value = fullName,
                    onValueChange = { fullName = it; profileMessage = null },
                    placeholder = "Nama lengkap"
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Alamat Email", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                CustomTextField(
                    value = email,
                    onValueChange = { email = it; profileMessage = null },
                    placeholder = "email@kamu.com",
                    keyboardType = KeyboardType.Email
                )

                profileMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = if (profileMessageIsError) Color.Red else PrimaryGreen,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingCard(title = "Ubah Kata Sandi") {
            Text("Kata Sandi Saat Ini", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            CustomTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it; passwordMessage = null },
                placeholder = "••••••••",
                isPassword = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text("Kata Sandi Baru", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            CustomTextField(
                value = newPassword,
                onValueChange = { newPassword = it; passwordMessage = null },
                placeholder = "••••••••",
                isPassword = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text("Konfirmasi Kata Sandi", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            CustomTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; passwordMessage = null },
                placeholder = "••••••••",
                isPassword = true,
                isError = confirmPassword.isNotEmpty() && confirmPassword != newPassword
            )

            passwordMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = it,
                    color = if (passwordMessageIsError) Color.Red else PrimaryGreen,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    when {
                        currentPassword.isBlank() -> {
                            passwordMessage = "Masukkan password saat ini"
                            passwordMessageIsError = true
                        }
                        newPassword.length < 6 -> {
                            passwordMessage = "Password baru minimal 6 karakter"
                            passwordMessageIsError = true
                        }
                        newPassword != confirmPassword -> {
                            passwordMessage = "Konfirmasi password tidak cocok"
                            passwordMessageIsError = true
                        }
                        else -> {
                            isUpdatingPassword = true
                            passwordMessage = null
                            scope.launch {
                                try {
                                    val response = AuthService.changePassword(
                                        tokenHeader,
                                        UpdatePasswordRequest(
                                            oldPassword = currentPassword,
                                            newPassword = newPassword
                                        )
                                    )
                                    if (response.isSuccessful && response.body?.status == true) {
                                        passwordMessage = "Password berhasil diperbarui!"
                                        passwordMessageIsError = false
                                        currentPassword = ""
                                        newPassword = ""
                                        confirmPassword = ""
                                    } else {
                                        passwordMessage = response.body?.message ?: "Gagal update password"
                                        passwordMessageIsError = true
                                    }
                                } catch (e: Exception) {
                                    passwordMessage = "Koneksi bermasalah: ${e.message}"
                                    passwordMessageIsError = true
                                } finally {
                                    isUpdatingPassword = false
                                }
                            }
                        }
                    }
                },
                enabled = !isUpdatingPassword,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                if (isUpdatingPassword) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Perbarui Kata Sandi", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingCard(title = "Preferensi") {
            PreferenceToggle(
                title = "Simpan Riwayat Analisis",
                subtitle = "Rekam hasil penilaian kesehatan mentalmu",
                checked = saveHistory,
                onCheckedChange = {
                    saveHistory = it
                    AppSettings.setBool("pref_save_history", it)
                }
            )
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 12.dp))
            PreferenceToggle(
                title = "Input Suara Otomatis",
                subtitle = "Mulai rekaman otomatis saat chat dibuka",
                checked = autoVoice,
                onCheckedChange = {
                    autoVoice = it
                    AppSettings.setBool("pref_auto_voice", it)
                }
            )
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 12.dp))
            PreferenceToggle(
                title = "Notifikasi",
                subtitle = "Terima pengingat untuk check-in rutin",
                checked = notifications,
                onCheckedChange = {
                    notifications = it
                    AppSettings.setBool("pref_notifications", it)
                    if (it) {
                        requestNotificationPermission()
                    } else {
                        ReminderScheduler.cancel()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = { navController.popBackStack() },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text("Batal", color = Color.Black)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = {
                    if (fullName.isBlank() || email.isBlank()) {
                        profileMessage = "Nama dan email tidak boleh kosong"
                        profileMessageIsError = true
                        return@Button
                    }
                    isSavingProfile = true
                    profileMessage = null
                    scope.launch {
                        try {
                            val response = AuthService.updateProfile(
                                tokenHeader,
                                UpdateMeRequest(name = fullName.trim(), email = email.trim())
                            )
                            if (response.isSuccessful && response.body?.status == true) {
                                profileMessage = "Profil berhasil disimpan!"
                                profileMessageIsError = false
                            } else {
                                profileMessage = response.body?.message ?: "Gagal menyimpan profil"
                                profileMessageIsError = true
                            }
                        } catch (e: Exception) {
                            profileMessage = "Koneksi bermasalah: ${e.message}"
                            profileMessageIsError = true
                        } finally {
                            isSavingProfile = false
                        }
                    }
                },
                enabled = !isSavingProfile,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.White),
                modifier = Modifier.height(48.dp)
            ) {
                if (isSavingProfile) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Check, "Save", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan")
                }
            }
        }
    }
}

@Composable
fun SettingCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
fun PreferenceToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 14.sp, color = PrimaryGreen.copy(alpha = 0.7f))
        }
        Box(
            modifier = Modifier
                .size(width = 48.dp, height = 28.dp)
                .background(
                    color = if (checked) PrimaryGreen else Color(0xFFE0E0E0),
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable { onCheckedChange(!checked) },
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .padding(3.dp)
                    .size(22.dp)
                    .background(Color.White, shape = RoundedCornerShape(11.dp))
            )
        }
    }
}
