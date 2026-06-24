package com.example.mindcare

import androidx.compose.runtime.Composable
import com.example.mindcare.navigation.AppNavigation
import com.example.mindcare.ui.theme.MindCareTheme

@Composable
fun App() {
    MindCareTheme {
        AppNavigation()
    }
}
