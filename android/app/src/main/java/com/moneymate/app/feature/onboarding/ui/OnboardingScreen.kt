package com.moneymate.app.feature.onboarding.ui

import com.moneymate.app.core.localization.tr

// =============================================================================
// File: OnboardingScreen.kt
// Purpose: First-run onboarding flow that introduces MoneyMate before authentication.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneymate.app.ui.theme.LocalMoneyMateTokens


// -----------------------------------------------------------------------------
// Section: OnboardingScreen
// Purpose: Renders the Onboarding Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
fun OnboardingScreen(onFinished:()->Unit){
    val c=LocalMoneyMateTokens.current;var page by remember{mutableIntStateOf(0)};val pages=listOf("Take Control of Your Money" to "Track every income and expense with a clean, organized financial dashboard.","Build Better Financial Habits" to "Create budgets, monitor spending, and stay in control of your financial goals.","Grow Your Savings with Confidence" to "Set savings goals, monitor progress, and make smarter financial decisions every day.")
    Column(Modifier.fillMaxSize().background(c.background).padding(28.dp),horizontalAlignment=Alignment.CenterHorizontally){Row(Modifier.fillMaxWidth()){Spacer(Modifier.weight(1f));TextButton(onClick=onFinished){Text(tr("Skip"),color=c.action)}};Spacer(Modifier.weight(1f));Box(Modifier.size(120.dp).background(c.lightAction,CircleShape),contentAlignment=Alignment.Center){Text(listOf("$","%","↑")[page],color=c.action,fontSize=42.sp,fontWeight=FontWeight.Bold)};Spacer(Modifier.height(28.dp));Text(pages[page].first,color=c.primaryText,fontSize=27.sp,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center);Spacer(Modifier.height(12.dp));Text(pages[page].second,color=c.secondaryText,fontSize=14.sp,lineHeight=22.sp,textAlign=TextAlign.Center);Spacer(Modifier.weight(1f));Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){repeat(3){i->Box(Modifier.size(if(i==page)20.dp else 8.dp,8.dp).background(if(i==page)c.action else c.border,RoundedCornerShape(50)))}};Spacer(Modifier.height(20.dp));Button(onClick={if(page<2)page++ else onFinished()},modifier=Modifier.fillMaxWidth().height(54.dp)){Text(if(page<2)"Next" else "Get Started",fontWeight=FontWeight.Bold)}}
}
