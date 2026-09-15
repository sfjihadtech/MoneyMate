package com.moneymate.app.feature.auth.ui

import com.moneymate.app.core.localization.tr

// =============================================================================
// File: PasswordResetSuccessScreen.kt
// Purpose: Confirmation screen shown after a successful password reset.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneymate.app.ui.theme.LocalMoneyMateTokens


// -----------------------------------------------------------------------------
// Section: PasswordResetSuccessScreen
// Purpose: Renders the Password Reset Success Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
fun PasswordResetSuccessScreen(onBackToSignIn: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    Column(
        Modifier.fillMaxSize().background(c.background).padding(horizontal = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AuthIllustration(Icons.Filled.MarkEmailRead)
        Spacer(Modifier.height(22.dp))
        Text(tr("Password Reset Link Sent"), color = c.primaryText, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(tr("Your password has been updated. You can now sign in with your new password."), color = c.secondaryText, fontSize = 13.5.sp, lineHeight = 21.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        AuthPrimaryButton("Back to Sign In", onClick = onBackToSignIn)
    }
}
