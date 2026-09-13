package com.moneymate.app.feature.auth.ui

// =============================================================================
// File: ResetPasswordScreen.kt
// Purpose: Password-reset screen for setting and confirming a new password.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneymate.app.data.repository.MoneyMateRepository
import com.moneymate.app.data.repository.RepoResult
import com.moneymate.app.ui.theme.LocalMoneyMateTokens
import kotlinx.coroutines.launch


// -----------------------------------------------------------------------------
// Section: ResetPasswordScreen
// Purpose: Renders the Reset Password Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
fun ResetPasswordScreen(token: String, onBack: () -> Unit, onSuccess: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    val context = LocalContext.current
    val repo = remember(context) { MoneyMateRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var valid by remember { mutableStateOf<Boolean?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(token) {
        valid = when (repo.verifyReset(token)) {
            is RepoResult.Success -> true
            is RepoResult.Error -> false
        }
        if (valid == false) error = "This reset link is invalid or expired."
    }

    Column(Modifier.fillMaxSize().background(c.background)) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 14.dp, vertical = 10.dp)) { AuthBackButton(onBack) }
        Column(Modifier.fillMaxWidth().padding(horizontal = 26.dp, vertical = 8.dp)) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { AuthIllustration(Icons.Filled.Lock) }
            Spacer(Modifier.height(20.dp))
            Text("Reset Password", color = c.primaryText, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            Text("Choose a strong new password.", color = c.secondaryText, fontSize = 13.5.sp)
            Spacer(Modifier.height(26.dp))
            when (valid) {
                null -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.action) }
                false -> Text(error.orEmpty(), color = c.error, fontSize = 13.sp)
                true -> {
                    AuthField("New Password", password, { password = it; error = null }, "••••••••", Icons.Filled.Lock, enabled = !loading, passwordToggle = true)
                    Spacer(Modifier.height(14.dp))
                    AuthField("Confirm Password", confirm, { confirm = it; error = null }, "••••••••", Icons.Filled.Lock, enabled = !loading, passwordToggle = true)
                    error?.let { Spacer(Modifier.height(8.dp)); Text(it, color = c.error, fontSize = 12.sp) }
                    Spacer(Modifier.height(18.dp))
                    AuthPrimaryButton(if (loading) "Resetting…" else "Reset Password", enabled = !loading) {
                        error = when {
                            password.length < 10 -> "Password must contain at least 10 characters."
                            password != confirm -> "Passwords do not match."
                            else -> null
                        }
                        if (error != null) return@AuthPrimaryButton
                        loading = true
                        scope.launch {
                            when (val result = repo.resetPassword(token = token, password = password)) {
                                is RepoResult.Success -> onSuccess()
                                is RepoResult.Error -> error = result.message
                            }
                            loading = false
                        }
                    }
                }
            }
        }
    }
}
