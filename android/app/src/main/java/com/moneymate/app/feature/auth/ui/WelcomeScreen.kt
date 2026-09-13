package com.moneymate.app.feature.auth.ui

// =============================================================================
// File: WelcomeScreen.kt
// Purpose: Welcome screen shown before authentication. Uses a clean solid brand background with no blue radial glow.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneymate.app.R
import com.moneymate.app.ui.theme.LocalMoneyMateTokens


// -----------------------------------------------------------------------------
// Section: WelcomeScreen
// Purpose: Renders the Welcome Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
fun WelcomeScreen(
    onSignIn: () -> Unit,
    onCreateAccount: () -> Unit
) {
    val c = LocalMoneyMateTokens.current
    Box(
        Modifier
            .fillMaxSize()
            .background(c.brand)
    ) {
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 30.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(96.dp),
                    shape = RoundedCornerShape(30.dp),
                    color = Color.White.copy(alpha = .14f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painterResource(R.drawable.moneymate_logo),
                            contentDescription = "MoneyMate logo",
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }
                Spacer(Modifier.height(22.dp))
                Text(
                    "Welcome to MoneyMate",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "The smart way to track spending, build budgets, and grow your savings — all in one place.",
                    color = Color.White.copy(alpha = .78f),
                    fontSize = 13.5.sp,
                    lineHeight = 21.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 290.dp)
                )
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp)
            ) {
                Button(
                    onClick = onSignIn,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = c.brand)
                ) { Text("Sign In", fontWeight = FontWeight.Bold) }

                OutlinedButton(
                    onClick = onCreateAccount,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = .42f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Text("Create Account", fontWeight = FontWeight.Bold) }

            }
        }
    }
}
