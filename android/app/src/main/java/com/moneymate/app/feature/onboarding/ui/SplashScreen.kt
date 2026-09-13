package com.moneymate.app.feature.onboarding.ui

// =============================================================================
// File: SplashScreen.kt
// Purpose: Startup splash screen displayed while initial app state is prepared.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneymate.app.R
import com.moneymate.app.ui.theme.LocalMoneyMateTokens
import kotlinx.coroutines.delay


// -----------------------------------------------------------------------------
// Section: SplashScreen
// Purpose: Renders the Splash Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
fun SplashScreen(onFinished:()->Unit){val c=LocalMoneyMateTokens.current;val alpha=remember{Animatable(0f)};LaunchedEffect(Unit){alpha.animateTo(1f,tween(500));delay(900);onFinished()};Box(Modifier.fillMaxSize().background(c.brand),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.alpha(alpha.value)){Box(Modifier.size(116.dp).background(Color.White.copy(alpha=.12f),RoundedCornerShape(34.dp)),contentAlignment=Alignment.Center){Image(painterResource(R.drawable.moneymate_logo),"MoneyMate",Modifier.size(88.dp))};Spacer(Modifier.height(22.dp));Text("MoneyMate",color=Color.White,fontSize=30.sp,fontWeight=FontWeight.Bold);Text("Track Smarter. Spend Better. Grow Wealth.",color=Color.White.copy(alpha=.78f),fontSize=12.sp)}}}
