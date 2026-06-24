package com.example.mindcare.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
expect fun appColorScheme(darkTheme: Boolean, dynamicColor: Boolean): ColorScheme
