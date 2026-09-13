package com.moneymate.app.feature.auth.ui

// =============================================================================
// File: SignInScreen.kt
// Purpose: Sign-in screen that validates credentials and starts an authenticated session.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.CircularProgressIndicator
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
// Section: SignInScreen
// Purpose: Renders the Sign In Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
fun SignInScreen(
    onBack: () -> Unit,
    onSignedIn: () -> Unit,
    onCreateAccount: () -> Unit,
    onForgotPassword: () -> Unit
) {
    val c = LocalMoneyMateTokens.current
    val context = LocalContext.current
    val repo = remember(context) { MoneyMateRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var loading by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var apiError by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().background(c.background)) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 14.dp, vertical = 10.dp)) {
            AuthBackButton(onBack)
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 26.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { AuthIllustration(Icons.Filled.LockOpen) }
            Spacer(Modifier.height(20.dp))
            Text("Welcome back", color = c.primaryText, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            Text("Sign In", color = c.secondaryText, fontSize = 13.5.sp)
            Spacer(Modifier.height(26.dp))

            AuthField(
                "Email Address", email,
                { email = it; emailError = null; apiError = null },
                "you@example.com", Icons.Filled.Email,
                enabled = !loading, error = emailError,
                keyboardType = KeyboardType.Email, imeAction = ImeAction.Next
            )
            Spacer(Modifier.height(14.dp))
            AuthField(
                "Password", password,
                { password = it; passwordError = null; apiError = null },
                "••••••••", Icons.Filled.Lock,
                enabled = !loading, error = passwordError,
                keyboardType = KeyboardType.Password, imeAction = ImeAction.Done,
                passwordToggle = true
            )
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                AuthCheckboxRow(rememberMe, "Remember Me") { rememberMe = !rememberMe }
                Spacer(Modifier.weight(1f))
                Text(
                    "Forgot Password?",
                    color = c.action,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(enabled = !loading, onClick = onForgotPassword)
                )
            }
            Spacer(Modifier.height(20.dp))
            apiError?.let { Text(it, color = c.error, fontSize = 12.sp); Spacer(Modifier.height(8.dp)) }

            AuthPrimaryButton(if (loading) "Signing in…" else "Sign In", enabled = !loading) {
                emailError = if (!email.contains('@')) "Enter a valid email address" else null
                passwordError = if (password.isBlank()) "Enter your password" else null
                if (emailError != null || passwordError != null) return@AuthPrimaryButton
                loading = true
                scope.launch {
                    when (val result = repo.login(email, password, rememberMe)) {
                        is RepoResult.Success -> onSignedIn()
                        is RepoResult.Error -> apiError = result.message
                    }
                    loading = false
                }
            }
            if (loading) {
                Spacer(Modifier.height(10.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = c.action, modifier = Modifier.size(20.dp), strokeWidth = 2.dp) }
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("Don't have an account? ", color = c.secondaryText, fontSize = 13.sp)
                Text("Create Account", color = c.action, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onCreateAccount))
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
