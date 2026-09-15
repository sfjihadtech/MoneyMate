package com.moneymate.app.feature.main.ui

import com.moneymate.app.core.localization.tr

// =============================================================================
// File: MainAppScreen.kt
// Purpose: Authenticated application shell, navigation tabs, floating action behavior, and main screen routing.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.moneymate.app.R
import com.moneymate.app.feature.main.MainTab
import com.moneymate.app.feature.main.MoneyMateState
import com.moneymate.app.feature.main.ToolPage
import com.moneymate.app.ui.theme.LocalMoneyMateTokens


// -----------------------------------------------------------------------------
// Section: MoneyMateMainApp
// Purpose: Formatting/helper logic for Money Mate Main App.
// -----------------------------------------------------------------------------
@Composable
fun MoneyMateMainApp(
    onSignOut: () -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    guestMode: Boolean = false
) {
    val context = LocalContext.current
    val c = LocalMoneyMateTokens.current
    val state = remember(guestMode) { MoneyMateState(context.applicationContext, guestMode) }
    var tab by remember { mutableStateOf(MainTab.HOME) }
    var toolPage by remember { mutableStateOf(ToolPage.NONE) }
    var showAdd by remember { mutableStateOf(false) }
    var editTx by remember { mutableStateOf<com.moneymate.app.data.model.Transaction?>(null) }
    var showTransfer by remember { mutableStateOf(false) }
    var showLanguage by remember { mutableStateOf(false) }
    var showCurrency by remember { mutableStateOf(false) }
    var showPlans by remember { mutableStateOf(false) }
    var showFilter by remember { mutableStateOf(false) }
    var filterInitialType by remember { mutableStateOf("All") }
    var activityFilter by remember { mutableStateOf("all") }
    var locked by remember { mutableStateOf(state.prefs.hasPin()) }
    var pin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var backgroundAt by remember { mutableLongStateOf(0L) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) backgroundAt = System.currentTimeMillis()
            if (event == Lifecycle.Event.ON_START && state.prefs.hasPin() && backgroundAt > 0) {
                val awayMs = System.currentTimeMillis() - backgroundAt
                if (awayMs >= state.prefs.autoLockMinutes * 60_000L) locked = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) { state.loadAll(true) }
    LaunchedEffect(state.error) { state.error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); state.error = null } }
    LaunchedEffect(state.message) { state.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); state.message = null } }
    LaunchedEffect(state.lockRequested) {
        if (state.lockRequested) { locked = true; state.lockRequested = false }
    }

    val openTool: (ToolPage) -> Unit = { page ->
        when (page) {
            ToolPage.LANGUAGE -> showLanguage = true
            ToolPage.CURRENCY -> showCurrency = true
            ToolPage.PLANS -> showPlans = true
            ToolPage.ADVANCED_FILTERS -> { filterInitialType = "All"; showFilter = true }
            else -> toolPage = page
        }
    }

    Scaffold(
        containerColor = c.background,

        bottomBar = {
            if (toolPage == ToolPage.NONE) {
                HtmlBottomNavigation(
                    state = state,
                    selected = tab,
                    onSelect = { tab = it },
                    onAdd = { showAdd = true }
                )
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (toolPage != ToolPage.NONE) {
                ToolRouter(
                    toolPage,
                    state,
                    onBack = { toolPage = ToolPage.NONE },
                    onOpenProfileTab = { toolPage = ToolPage.NONE; tab = MainTab.PROFILE },
                    onDarkModeChange = onDarkModeChange,
                    onSignOut = {
                        state.repository.logout()
                        onSignOut()
                    }
                )
            } else {
                when (tab) {
                    MainTab.HOME -> HomeTab(
                        state = state,
                        onOpenTool = openTool,
                        onAdd = { showAdd = true },
                        onViewActivity = { tab = MainTab.ACTIVITY },
                        onProfile = { tab = MainTab.PROFILE }
                    )
                    MainTab.ACTIVITY -> TransactionsTab(state, { showAdd = true }, { editTx = it }, activityFilter, { current -> filterInitialType = current; showFilter = true })
                    MainTab.ANALYTICS -> AnalyticsTab(state) { openTool(it) }
                    MainTab.PROFILE -> ProfileTab(
                        state = state,
                        onOpenTool = openTool,
                        onDarkModeChange = onDarkModeChange,
                        onSignOut = { state.repository.logout(); onSignOut() }
                    )
                }
            }

            if (state.loading) {
                Box(Modifier.fillMaxSize().background(c.background.copy(alpha = .76f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = c.action)
                }
            }
        }
    }

    if (showAdd) AddTransactionDialog(state) { showAdd = false }
    editTx?.let { tx -> AddTransactionDialog(state, tx) { editTx = null } }
    if (showTransfer) TransferDialog(state) { showTransfer = false }
    if (showLanguage) LanguageSheet(state) { showLanguage = false }
    if (showCurrency) CurrencySheet(state) { showCurrency = false }
    if (showPlans) PlansSheet(state) { showPlans = false }
    if (showFilter) FilterSheet(filterInitialType, { showFilter = false }) { applied -> filterInitialType = applied; activityFilter = applied }

    if (locked && state.prefs.hasPin()) {
        LockOverlay(
            state = state,
            pin = pin,
            pinError = pinError,
            onDigit = { digit ->
                if (pin.length < 6) {
                    pin += digit
                    if (pin.length >= 4 && state.prefs.verifyPin(pin)) {
                        locked = false; pin = ""; pinError = false
                    } else if (pin.length == 6) {
                        pinError = true; pin = ""
                    }
                }
            },
            onBackspace = { pin = pin.dropLast(1); pinError = false },
            onDeviceAuthSuccess = { locked = false; pin = ""; pinError = false }
        )
    }
}


// -----------------------------------------------------------------------------
// Section: HtmlBottomNavigation
// Purpose: Navigation/routing logic for Html Bottom Navigation.
// -----------------------------------------------------------------------------
@Composable
private fun HtmlBottomNavigation(
    state: MoneyMateState,
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    onAdd: () -> Unit
) {
    val c = LocalMoneyMateTokens.current
    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(96.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(70.dp).align(Alignment.BottomCenter),
            color = c.surface,
            shadowElevation = 8.dp
        ) {
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                HtmlNavItem(R.drawable.nav_home, if(state.language=="bn")"হোম" else "Home", selected == MainTab.HOME, Modifier.weight(1f)) { onSelect(MainTab.HOME) }
                HtmlNavItem(R.drawable.nav_activity, if(state.language=="bn")"অ্যাক্টিভিটি" else "Activity", selected == MainTab.ACTIVITY, Modifier.weight(1f)) { onSelect(MainTab.ACTIVITY) }
                Spacer(Modifier.weight(1f))
                HtmlNavItem(R.drawable.nav_insights, if(state.language=="bn")"ইনসাইটস" else "Insights", selected == MainTab.ANALYTICS, Modifier.weight(1f)) { onSelect(MainTab.ANALYTICS) }
                HtmlNavItem(R.drawable.nav_profile, if(state.language=="bn")"প্রোফাইল" else "Profile", selected == MainTab.PROFILE, Modifier.weight(1f)) { onSelect(MainTab.PROFILE) }
            }
        }

        Surface(
            modifier = Modifier.size(58.dp).align(Alignment.TopCenter),
            shape = RoundedCornerShape(20.dp),
            color = Color.Transparent,
            shadowElevation = 8.dp,
            onClick = onAdd
        ) {
            Box(
                Modifier.background(Brush.linearGradient(listOf(c.action, c.brand))),
                contentAlignment = Alignment.Center
            ) {
                Image(painter = painterResource(R.drawable.nav_fab), contentDescription = "Add transaction", colorFilter = ColorFilter.tint(Color.White), modifier = Modifier.size(29.dp))
            }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: HtmlNavItem
// Purpose: Encapsulates the Html Nav Item section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun HtmlNavItem(
    iconRes: Int,
    label: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val c = LocalMoneyMateTokens.current
    val tint = if (selected) c.action else c.mutedText
    Column(
        modifier.clickable(onClick = onClick).padding(top = 8.dp, bottom = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = label,
            colorFilter = ColorFilter.tint(tint),
            modifier = Modifier.size(23.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(label, color = tint, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
    }
}


// -----------------------------------------------------------------------------
// Section: LockOverlay
// Purpose: Encapsulates the Lock Overlay section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun LockOverlay(
    state: MoneyMateState,
    pin: String,
    pinError: Boolean,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onDeviceAuthSuccess: () -> Unit
) {
    val c = LocalMoneyMateTokens.current
    val context = LocalContext.current
    val activity = context as? Activity
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) onDeviceAuthSuccess()
    }

    Surface(Modifier.fillMaxSize(), color = c.background) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(Modifier.size(80.dp).background(c.lightAction, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Lock, null, tint = c.action, modifier = Modifier.size(34.dp))
            }
            Spacer(Modifier.height(18.dp))
            Text(tr("MoneyMate Locked"), color = c.primaryText, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Text(tr("Enter your PIN to continue"), color = c.secondaryText, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                repeat(6) { index -> Box(Modifier.size(14.dp).background(if (index < pin.length) c.action else c.border, CircleShape)) }
            }
            if (pinError) Text(tr("Incorrect PIN"), color = c.error, modifier = Modifier.padding(top = 8.dp), fontSize = 12.sp)
            Spacer(Modifier.height(26.dp))
            listOf(listOf("1","2","3"), listOf("4","5","6"), listOf("7","8","9"), listOf("BIO","0","⌫")).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(vertical = 7.dp)) {
                    row.forEach { key ->
                        if (key == "BIO") {
                            if (state.prefs.biometricEnabled && activity != null) {
                                Surface(
                                    modifier = Modifier.size(64.dp), shape = CircleShape, color = c.surface,
                                    border = BorderStroke(1.dp, c.divider),
                                    onClick = {
                                        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                                        val intent = km.createConfirmDeviceCredentialIntent("Unlock MoneyMate", "")
                                        if (intent != null) launcher.launch(intent)
                                    }
                                ) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Fingerprint, null, tint = c.action, modifier = Modifier.size(28.dp)) } }
                            } else Spacer(Modifier.size(64.dp))
                        } else Surface(
                            modifier = Modifier.size(64.dp), shape = CircleShape, color = c.surface,
                            border = BorderStroke(1.dp, c.divider),
                            onClick = { if (key == "⌫") onBackspace() else onDigit(key) }
                        ) { Box(contentAlignment = Alignment.Center) { Text(key, color = c.primaryText, fontSize = 20.sp, fontWeight = FontWeight.SemiBold) } }
                    }
                }
            }
        }
    }
}
