package com.moneymate.app.feature.auth.ui

// =============================================================================
// File: ForgotPasswordScreen.kt
// Purpose: Forgot-password screen that starts the password-reset flow.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneymate.app.data.repository.MoneyMateRepository
import com.moneymate.app.data.repository.RepoResult
import com.moneymate.app.ui.theme.LocalMoneyMateTokens
import kotlinx.coroutines.launch


// -----------------------------------------------------------------------------
// Section: ForgotPasswordScreen
// Purpose: Renders the Forgot Password Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onResetTokenReady: (String) -> Unit
) {
    val c = LocalMoneyMateTokens.current
    val context = LocalContext.current
    val repo = remember(context) { MoneyMateRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().background(c.background)) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 14.dp, vertical = 10.dp)) { AuthBackButton(onBack) }
        Column(Modifier.fillMaxWidth().padding(horizontal = 26.dp, vertical = 8.dp)) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { AuthIllustration(Icons.Filled.Lock) }
            Spacer(Modifier.height(20.dp))
            Text("Forgot Password?", color = c.primaryText, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            Text("We'll email you a secure link to reset your password.", color = c.secondaryText, fontSize = 13.5.sp)
            Spacer(Modifier.height(26.dp))
            AuthField("Email Address", email, { email = it; error = null; status = null }, "you@example.com", Icons.Filled.Email, enabled = !loading, error = error, keyboardType = KeyboardType.Email, imeAction = ImeAction.Done)
            Spacer(Modifier.height(18.dp))
            AuthPrimaryButton(if (loading) "Sending…" else "Send Reset Link", enabled = !loading) {
                if (!email.contains('@')) { error = "Enter a valid email address"; return@AuthPrimaryButton }
                loading = true
                scope.launch {
                    when (val result = repo.forgotPassword(email)) {
                        is RepoResult.Success -> {
                            status = result.message.ifBlank { "Password Reset Email Sent" }
                            result.data.resetToken?.takeIf { it.isNotBlank() }?.let(onResetTokenReady)
                        }
                        is RepoResult.Error -> error = result.message
                    }
                    loading = false
                }
            }
            status?.let { Spacer(Modifier.height(12.dp)); Text(it, color = c.success, fontSize = 12.sp) }
            Spacer(Modifier.height(20.dp))
            Text("Back to Sign In", color = c.action, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally).clickable(onClick = onBack))
        }
    }
}
