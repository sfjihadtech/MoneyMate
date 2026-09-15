package com.moneymate.app.feature.auth.ui

import com.moneymate.app.core.localization.tr

// =============================================================================
// File: ForgotPasswordScreen.kt
// Purpose: Forgot-password screen that starts the password-reset flow.
// Notes:
// - Shows professional success/error feedback.
// - Never exposes raw backend messages.
// - Successful requests start a 90-second resend countdown.
// - Backend enforces the final cooldown and 12-hour request limit.
// =============================================================================

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneymate.app.data.repository.MoneyMateRepository
import com.moneymate.app.data.repository.RepoResult
import com.moneymate.app.ui.theme.LocalMoneyMateTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


// =============================================================================
// Feedback State
// =============================================================================

private enum class ForgotPasswordFeedback {
    SUCCESS,
    ERROR,
    LIMIT_REACHED
}


// =============================================================================
// Forgot Password Screen
// =============================================================================

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onResetTokenReady: (String) -> Unit
) {
    val c = LocalMoneyMateTokens.current
    val context = LocalContext.current

    val repo = remember(context) {
        MoneyMateRepository(context.applicationContext)
    }

    val scope = rememberCoroutineScope()


    // -------------------------------------------------------------------------
    // Form State
    // -------------------------------------------------------------------------

    var email by remember {
        mutableStateOf("")
    }

    var loading by remember {
        mutableStateOf(false)
    }

    var fieldError by remember {
        mutableStateOf<String?>(null)
    }


    // -------------------------------------------------------------------------
    // Feedback State
    // -------------------------------------------------------------------------

    var feedback by remember {
        mutableStateOf<ForgotPasswordFeedback?>(null)
    }


    // -------------------------------------------------------------------------
    // Resend State
    // -------------------------------------------------------------------------

    var cooldownSeconds by remember {
        mutableIntStateOf(0)
    }

    var hasSentResetLink by remember {
        mutableStateOf(false)
    }


    // -------------------------------------------------------------------------
    // Countdown
    // -------------------------------------------------------------------------

    LaunchedEffect(cooldownSeconds) {
        if (cooldownSeconds > 0) {
            delay(1000L)

            if (cooldownSeconds > 0) {
                cooldownSeconds -= 1
            }
        }
    }


    // -------------------------------------------------------------------------
    // Button Text
    // -------------------------------------------------------------------------

    val buttonText =
        when {
            loading -> {
                "Sending…"
            }

            cooldownSeconds > 0 -> {
                val minutes = cooldownSeconds / 60
                val seconds = cooldownSeconds % 60

                "Resend in %02d:%02d".format(
                    minutes,
                    seconds
                )
            }

            hasSentResetLink -> {
                "Resend Reset Link"
            }

            else -> {
                "Send Reset Link"
            }
        }


    // =========================================================================
    // Screen
    // =========================================================================

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(c.background)
    ) {

        // ---------------------------------------------------------------------
        // Back Button
        // ---------------------------------------------------------------------

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(
                        horizontal = 14.dp,
                        vertical = 10.dp
                    )
        ) {
            AuthBackButton(onBack)
        }


        // ---------------------------------------------------------------------
        // Main Content
        // ---------------------------------------------------------------------

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 26.dp,
                        vertical = 8.dp
                    )
        ) {

            // -----------------------------------------------------------------
            // Illustration
            // -----------------------------------------------------------------

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AuthIllustration(
                    Icons.Filled.Lock
                )
            }


            Spacer(
                Modifier.height(20.dp)
            )


            // -----------------------------------------------------------------
            // Title
            // -----------------------------------------------------------------

            Text(
                text = tr("Forgot Password?"),
                color = c.primaryText,
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold
            )


            Spacer(
                Modifier.height(6.dp)
            )


            Text(
                text = tr(
                    "We'll email you a secure link to reset your password."
                ),
                color = c.secondaryText,
                fontSize = 13.5.sp
            )


            Spacer(
                Modifier.height(26.dp)
            )


            // -----------------------------------------------------------------
            // Email Field
            // -----------------------------------------------------------------

            AuthField(
                label = "Email Address",
                value = email,
                onValueChange = {
                    email = it
                    fieldError = null

                    // Clear previous feedback when email changes.
                    feedback = null
                },
                placeholder = "you@example.com",
                icon = Icons.Filled.Email,
                enabled =
                    !loading &&
                            cooldownSeconds == 0,
                error = fieldError,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            )


            Spacer(
                Modifier.height(18.dp)
            )


            // -----------------------------------------------------------------
            // Send / Resend Button
            // -----------------------------------------------------------------

            AuthPrimaryButton(
                text = buttonText,
                enabled =
                    !loading &&
                            cooldownSeconds == 0
            ) {

                // -------------------------------------------------------------
                // Validate Email
                // -------------------------------------------------------------

                if (
                    !email.contains("@") ||
                    email.substringAfter(
                        "@",
                        ""
                    ).isBlank()
                ) {
                    fieldError =
                        "Enter a valid email address"

                    feedback = null

                    return@AuthPrimaryButton
                }


                // -------------------------------------------------------------
                // Start Request
                // -------------------------------------------------------------

                loading = true
                fieldError = null
                feedback = null


                scope.launch {

                    when (
                        val result =
                            repo.forgotPassword(email)
                    ) {

                        // =====================================================
                        // SUCCESS
                        // =====================================================

                        is RepoResult.Success -> {

                            feedback =
                                ForgotPasswordFeedback.SUCCESS

                            hasSentResetLink = true


                            // Use backend cooldown.
                            // 90 seconds is the safe fallback.
                            cooldownSeconds =
                                result.data.cooldownSeconds
                                    ?.coerceAtLeast(1)
                                    ?: 90


                            // IMPORTANT:
                            // Never automatically navigate to Reset Password.
                            // Reset Password opens only from the secure
                            // password-reset link received by email.
                            //
                            // onResetTokenReady is intentionally not called.
                        }


                        // =====================================================
                        // ERROR
                        // =====================================================

                        is RepoResult.Error -> {

                            when (result.errorCode) {

                                // ---------------------------------------------
                                // 12-hour maximum request limit reached
                                // ---------------------------------------------

                                "RESET_LIMIT_REACHED" -> {

                                    feedback =
                                        ForgotPasswordFeedback.LIMIT_REACHED

                                    cooldownSeconds = 0
                                }


                                // ---------------------------------------------
                                // Backend cooldown still active
                                // ---------------------------------------------

                                "RESET_COOLDOWN" -> {

                                    // This normally only happens if the local
                                    // countdown was bypassed by restarting the
                                    // app or making another direct request.
                                    //
                                    // Never expose the backend message.
                                    feedback =
                                        ForgotPasswordFeedback.ERROR

                                    cooldownSeconds = 0
                                }


                                // ---------------------------------------------
                                // Email/server/network/other failure
                                // ---------------------------------------------

                                else -> {

                                    feedback =
                                        ForgotPasswordFeedback.ERROR

                                    cooldownSeconds = 0
                                }
                            }
                        }
                    }


                    loading = false
                }
            }


            // -----------------------------------------------------------------
            // Professional Feedback Card
            // -----------------------------------------------------------------

            feedback?.let { currentFeedback ->

                Spacer(
                    Modifier.height(14.dp)
                )


                when (currentFeedback) {

                    // =========================================================
                    // SUCCESS CARD
                    // =========================================================

                    ForgotPasswordFeedback.SUCCESS -> {

                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color =
                                            c.success.copy(
                                                alpha = 0.10f
                                            ),
                                        shape =
                                            RoundedCornerShape(
                                                14.dp
                                            )
                                    )
                                    .padding(14.dp),
                            verticalAlignment =
                                Alignment.Top
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = c.success,
                                modifier =
                                    Modifier.size(21.dp)
                            )


                            Spacer(
                                Modifier.width(10.dp)
                            )


                            Column(
                                modifier =
                                    Modifier.weight(1f)
                            ) {

                                Text(
                                    text =
                                        tr("Check your email"),
                                    color = c.primaryText,
                                    fontSize = 13.5.sp,
                                    fontWeight =
                                        FontWeight.Bold
                                )


                                Spacer(
                                    Modifier.height(4.dp)
                                )


                                Text(
                                    text =
                                        tr(
                                            "We've sent a password reset link to your email. Please check your inbox and spam folder."
                                        ),
                                    color = c.secondaryText,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }


                    // =========================================================
                    // NORMAL ERROR CARD
                    // =========================================================

                    ForgotPasswordFeedback.ERROR -> {

                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color =
                                            c.error.copy(
                                                alpha = 0.09f
                                            ),
                                        shape =
                                            RoundedCornerShape(
                                                14.dp
                                            )
                                    )
                                    .padding(14.dp),
                            verticalAlignment =
                                Alignment.Top
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint = c.error,
                                modifier =
                                    Modifier.size(21.dp)
                            )


                            Spacer(
                                Modifier.width(10.dp)
                            )


                            Column(
                                modifier =
                                    Modifier.weight(1f)
                            ) {

                                Text(
                                    text =
                                        tr(
                                            "Unable to send reset link"
                                        ),
                                    color = c.primaryText,
                                    fontSize = 13.5.sp,
                                    fontWeight =
                                        FontWeight.Bold
                                )


                                Spacer(
                                    Modifier.height(4.dp)
                                )


                                Text(
                                    text =
                                        tr(
                                            "We couldn't send the reset link right now. Please try again shortly."
                                        ),
                                    color = c.secondaryText,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }


                    // =========================================================
                    // 12-HOUR LIMIT CARD
                    // =========================================================

                    ForgotPasswordFeedback.LIMIT_REACHED -> {

                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color =
                                            c.error.copy(
                                                alpha = 0.09f
                                            ),
                                        shape =
                                            RoundedCornerShape(
                                                14.dp
                                            )
                                    )
                                    .padding(14.dp),
                            verticalAlignment =
                                Alignment.Top
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint = c.error,
                                modifier =
                                    Modifier.size(21.dp)
                            )


                            Spacer(
                                Modifier.width(10.dp)
                            )


                            Column(
                                modifier =
                                    Modifier.weight(1f)
                            ) {

                                Text(
                                    text =
                                        tr(
                                            "Reset limit reached"
                                        ),
                                    color = c.primaryText,
                                    fontSize = 13.5.sp,
                                    fontWeight =
                                        FontWeight.Bold
                                )


                                Spacer(
                                    Modifier.height(4.dp)
                                )


                                Text(
                                    text =
                                        tr(
                                            "You've reached the maximum number of reset requests. Please try again later."
                                        ),
                                    color = c.secondaryText,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }


            Spacer(
                Modifier.height(20.dp)
            )


            // -----------------------------------------------------------------
            // Back to Sign In
            // -----------------------------------------------------------------

            Text(
                text = tr("Back to Sign In"),
                color = c.action,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier =
                    Modifier
                        .align(
                            Alignment.CenterHorizontally
                        )
                        .clickable(
                            onClick = onBack
                        )
            )
        }
    }
}