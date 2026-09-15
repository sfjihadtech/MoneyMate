package com.moneymate.app.feature.auth.ui

import com.moneymate.app.core.localization.tr

// =============================================================================
// File: AuthComponents.kt
// Purpose: Reusable, professional authentication UI components shared by sign-in and account creation.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneymate.app.ui.theme.LocalMoneyMateTokens


// -----------------------------------------------------------------------------
// Section: AuthBackButton
// Purpose: Encapsulates the Auth Back Button section of this file.
// -----------------------------------------------------------------------------
@Composable
internal fun AuthBackButton(onClick: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    Surface(
        onClick = onClick,
        modifier = Modifier.size(42.dp),
        shape = RoundedCornerShape(13.dp),
        color = c.surface,
        border = BorderStroke(1.dp, c.border),
        shadowElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.ArrowBack, "Back", tint = c.primaryText, modifier = Modifier.size(21.dp))
        }
    }
}


// -----------------------------------------------------------------------------
// Section: AuthIllustration
// Purpose: Encapsulates the Auth Illustration section of this file.
// -----------------------------------------------------------------------------
@Composable
internal fun AuthIllustration(icon: ImageVector) {
    val c = LocalMoneyMateTokens.current
    Surface(
        modifier = Modifier.size(88.dp),
        shape = RoundedCornerShape(26.dp),
        color = c.lightAction,
        border = BorderStroke(1.dp, c.action.copy(alpha = .10f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = c.action, modifier = Modifier.size(40.dp))
        }
    }
}


// -----------------------------------------------------------------------------
// Section: AuthField
// Purpose: Encapsulates the Auth Field section of this file.
// -----------------------------------------------------------------------------
@Composable
internal fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    passwordToggle: Boolean = false
) {
    val c = LocalMoneyMateTokens.current
    var passwordVisible by remember { mutableStateOf(false) }
    val transformation = if (passwordToggle) {
        if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
    } else visualTransformation

    val trailing: (@Composable () -> Unit)? = when {
        error != null -> ({
            Icon(
                Icons.Filled.ErrorOutline,
                contentDescription = "Input error",
                tint = c.error,
                modifier = Modifier.size(20.dp)
            )
        })
        passwordToggle -> ({
            IconButton(
                onClick = { passwordVisible = !passwordVisible },
                enabled = enabled
            ) {
                Icon(
                    if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    if (passwordVisible) "Hide password" else "Show password",
                    tint = c.secondaryText,
                    modifier = Modifier.size(20.dp)
                )
            }
        })
        else -> null
    }

    Column(Modifier.fillMaxWidth()) {
        Text(
            text = tr(label),
            color = c.primaryText,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            leadingIcon = {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(11.dp),
                    color = if (error != null) c.lightError else c.lightAction
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (error != null) c.error else c.action,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            trailingIcon = trailing,
            placeholder = {
                Text(
                    text = tr(placeholder),
                    color = c.mutedText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            visualTransformation = transformation,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            textStyle = TextStyle(
                color = c.primaryText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 58.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = c.surface,
                unfocusedContainerColor = c.surface,
                disabledContainerColor = c.background,
                errorContainerColor = c.surface,
                focusedBorderColor = c.action,
                unfocusedBorderColor = c.border,
                disabledBorderColor = c.border.copy(alpha = .55f),
                errorBorderColor = c.error,
                focusedTextColor = c.primaryText,
                unfocusedTextColor = c.primaryText,
                disabledTextColor = c.mutedText,
                cursorColor = c.action,
                errorCursorColor = c.error,
                focusedLeadingIconColor = c.action,
                unfocusedLeadingIconColor = c.action
            ),
            isError = error != null
        )

        if (error != null) {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.padding(start = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.ErrorOutline, null, tint = c.error, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(5.dp))
                Text(error, color = c.error, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: PasswordStrengthHint
// Purpose: Encapsulates the Password Strength Hint section of this file.
// -----------------------------------------------------------------------------
@Composable
internal fun PasswordStrengthHint(password: String) {
    val c = LocalMoneyMateTokens.current
    val rules = listOf(
        password.length >= 10,
        password.any(Char::isUpperCase) && password.any(Char::isLowerCase),
        password.any(Char::isDigit),
        password.any { !it.isLetterOrDigit() }
    )
    val score = rules.count { it }
    val label = when (score) {
        0, 1 -> "Weak"
        2, 3 -> "Good"
        else -> "Strong"
    }
    val tone = when (score) {
        0, 1 -> c.error
        2, 3 -> c.warning
        else -> c.success
    }

    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(tr("Password strength"), color = c.mutedText, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text(tr(label), color = tone, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(7.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(4) { index ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(if (index < score) tone else c.border, RoundedCornerShape(100.dp))
                )
            }
        }
        Spacer(Modifier.height(7.dp))
        Text(
            "10+ characters • upper & lower case • number • symbol",
            color = c.mutedText,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}


// -----------------------------------------------------------------------------
// Section: AuthCheckboxRow
// Purpose: Encapsulates the Auth Checkbox Row section of this file.
// -----------------------------------------------------------------------------
@Composable
internal fun AuthCheckboxRow(
    checked: Boolean,
    label: String,
    onToggle: () -> Unit
) {
    val c = LocalMoneyMateTokens.current
    Row(
        Modifier.clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(20.dp),
            shape = RoundedCornerShape(6.dp),
            color = if (checked) c.action else c.surface,
            border = BorderStroke(1.5.dp, if (checked) c.action else c.border),
            onClick = onToggle
        ) {
            if (checked) Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.width(9.dp))
        Text(tr(label), color = c.secondaryText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}


// -----------------------------------------------------------------------------
// Section: AuthPrimaryButton
// Purpose: Encapsulates the Auth Primary Button section of this file.
// -----------------------------------------------------------------------------
@Composable
internal fun AuthPrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = c.action,
            contentColor = Color.White,
            disabledContainerColor = c.action.copy(alpha = .45f),
            disabledContentColor = Color.White.copy(alpha = .8f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp)
    ) {
        Text(tr(text), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
    }
}
