package com.moneymate.app.feature.auth.ui

import com.moneymate.app.core.localization.tr

// =============================================================================
// File: CreateAccountScreen.kt
// Purpose: Create-account screen with validation and password-strength feedback.
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
// Section: CreateAccountScreen
// Purpose: Renders the Create Account Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
fun CreateAccountScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    onSignIn: () -> Unit
) {
    val c = LocalMoneyMateTokens.current
    val context = LocalContext.current
    val repo = remember(context) { MoneyMateRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var terms by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().background(c.background)) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 14.dp, vertical = 10.dp)) { AuthBackButton(onBack) }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 26.dp, vertical = 8.dp)) {
            Text(tr("Create Account"), color = c.primaryText, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            Text(tr("The smart way to track spending, build budgets, and grow your savings — all in one place."), color = c.secondaryText, fontSize = 13.5.sp, lineHeight = 21.sp)
            Spacer(Modifier.height(26.dp))

            AuthField(
                "Full Name", name, { name = it; error = null }, "Enter your full name", Icons.Filled.Person,
                enabled = !loading, imeAction = ImeAction.Next
            )
            Spacer(Modifier.height(14.dp))
            AuthField(
                "Email Address", email, { email = it; error = null }, "you@example.com", Icons.Filled.Email,
                enabled = !loading, keyboardType = KeyboardType.Email, imeAction = ImeAction.Next
            )
            Spacer(Modifier.height(14.dp))
            AuthField(
                "Password", password, { password = it; error = null }, "••••••••", Icons.Filled.Lock,
                enabled = !loading, keyboardType = KeyboardType.Password, imeAction = ImeAction.Next,
                passwordToggle = true
            )
            PasswordStrengthHint(password)
            Spacer(Modifier.height(14.dp))
            AuthField(
                "Confirm Password", confirm, { confirm = it; error = null }, "••••••••", Icons.Filled.Lock,
                enabled = !loading, keyboardType = KeyboardType.Password, imeAction = ImeAction.Done,
                passwordToggle = true
            )
            Spacer(Modifier.height(14.dp))
            AuthCheckboxRow(terms, "I agree to the Terms & Conditions") { terms = !terms; error = null }
            error?.let { Spacer(Modifier.height(8.dp)); Text(it, color = c.error, fontSize = 12.sp) }
            Spacer(Modifier.height(18.dp))

            AuthPrimaryButton(if (loading) "Creating account…" else "Create Account", enabled = !loading) {
                val strong = password.length >= 10 && password.any(Char::isUpperCase) && password.any(Char::isLowerCase) && password.any(Char::isDigit) && password.any { !it.isLetterOrDigit() }
                error = when {
                    name.isBlank() -> "Enter your full name"
                    !email.contains('@') -> "Enter a valid email address"
                    !strong -> "Password must be 10+ characters with upper, lower, number and symbol"
                    password != confirm -> "Passwords do not match"
                    !terms -> "Please agree to the Terms & Conditions"
                    else -> null
                }
                if (error != null) return@AuthPrimaryButton
                loading = true
                scope.launch {
                    when (val result = repo.register(name, email, password)) {
                        is RepoResult.Success -> onCreated()
                        is RepoResult.Error -> error = result.message
                    }
                    loading = false
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(tr("Already have an account? "), color = c.secondaryText, fontSize = 13.sp)
                Text(tr("Sign In"), color = c.action, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onSignIn))
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
