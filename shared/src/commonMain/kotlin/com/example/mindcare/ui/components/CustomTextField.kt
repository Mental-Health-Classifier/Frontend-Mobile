package com.example.mindcare.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.mindcare.ui.theme.BorderGray
import com.example.mindcare.ui.theme.ErrorRed
import com.example.mindcare.ui.theme.PrimaryGreen
import com.example.mindcare.ui.theme.TextGray
import kotlinx.coroutines.delay
import mindcare.shared.generated.resources.Res
import mindcare.shared.generated.resources.ic_hidden
import mindcare.shared.generated.resources.ic_visible
import org.jetbrains.compose.resources.painterResource

// Masking semua karakter kecuali yang terakhir diketik (mirip behaviour native Android EditText)
private class LastCharRevealTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val masked = if (text.isEmpty()) {
            ""
        } else {
            "•".repeat(text.length - 1) + text.last()
        }
        return TransformedText(AnnotatedString(masked), OffsetMapping.Identity)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var revealLastChar by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(value) {
        if (isPassword && !passwordVisible && value.isNotEmpty()) {
            revealLastChar = true
            delay(700)
            revealLastChar = false
        }
    }

    val transformation = when {
        !isPassword -> VisualTransformation.None
        passwordVisible -> VisualTransformation.None
        revealLastChar -> LastCharRevealTransformation()
        else -> PasswordVisualTransformation()
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = placeholder, color = TextGray) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        ),
        visualTransformation = transformation,
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(if (passwordVisible) Res.drawable.ic_hidden else Res.drawable.ic_visible),
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = TextGray
                    )
                }
            }
        },
        isError = isError,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            errorContainerColor = Color.White,
            unfocusedBorderColor = BorderGray,
            focusedBorderColor = PrimaryGreen,
            disabledBorderColor = BorderGray.copy(alpha = 0.5f),
            errorBorderColor = ErrorRed,
            cursorColor = PrimaryGreen,
            errorCursorColor = PrimaryGreen,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            errorTextColor = Color.Black
        )
    )
}
