package com.example.mindcare.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mindcare.navigation.Routes
import com.example.mindcare.ui.theme.PrimaryGreen
import com.example.mindcare.ui.theme.TextGray
import mindcare.shared.generated.resources.Res
import mindcare.shared.generated.resources.ic_menu
import mindcare.shared.generated.resources.ic_setting
import org.jetbrains.compose.resources.painterResource

@Composable
fun TopBar(
    navController: NavController,
    currentScreen: String,
    onMenuClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 4.dp)
        ) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_menu),
                    contentDescription = "Menu",
                    tint = PrimaryGreen
                )
            }
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dashboard",
                    fontWeight = if (currentScreen == "Dashboard") FontWeight.Bold else FontWeight.Medium,
                    fontSize = if (currentScreen == "Dashboard") 20.sp else 18.sp,
                    color = if (currentScreen == "Dashboard") Color.Black else TextGray,
                    modifier = Modifier.clickable {
                        if (currentScreen != "Dashboard") {
                            navController.navigate(Routes.DASHBOARD) {
                                popUpTo(Routes.DASHBOARD) { inclusive = true }
                            }
                        }
                    }
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "Chat",
                    fontWeight = if (currentScreen == "Chat") FontWeight.Bold else FontWeight.Medium,
                    fontSize = if (currentScreen == "Chat") 20.sp else 18.sp,
                    color = if (currentScreen == "Chat") Color.Black else TextGray,
                    modifier = Modifier.clickable {
                        if (currentScreen != "Chat") {
                            navController.navigate(Routes.CHAT)
                        }
                    }
                )
            }

            IconButton(
                onClick = { navController.navigate(Routes.SETTINGS) },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_setting),
                    contentDescription = "Settings",
                    tint = PrimaryGreen
                )
            }
        }
    }
}
