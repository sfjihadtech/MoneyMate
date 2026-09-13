package com.moneymate.app.feature.main.ui

// =============================================================================
// File: ToolScreens.kt
// Purpose: Secondary financial tools, settings, security, backup/export, premium, and utility screens.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneymate.app.core.common.CategoryCatalog
import com.moneymate.app.core.common.displayDate
import com.moneymate.app.core.common.money
import com.moneymate.app.data.local.RecurringTemplate
import com.moneymate.app.data.model.*
import com.moneymate.app.data.repository.RepoResult
import com.moneymate.app.feature.main.MoneyMateState
import com.moneymate.app.feature.main.ToolPage
import com.moneymate.app.ui.theme.LocalMoneyMateTokens
import com.moneymate.app.ui.theme.PremiumThemes
import com.moneymate.app.R
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset


// -----------------------------------------------------------------------------
// Section: ToolRouter
// Purpose: Navigation/routing logic for Tool Router.
// -----------------------------------------------------------------------------
@Composable
fun ToolRouter(
    page: ToolPage,
    state: MoneyMateState,
    onBack: () -> Unit,
    onOpenProfileTab: () -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onSignOut: () -> Unit
) {
    when (page) {
        ToolPage.ACCOUNTS -> AccountsScreen(state, onBack)
        ToolPage.BUDGETS -> BudgetsScreen(state, onBack)
        ToolPage.GOALS -> GoalsScreen(state, onBack)
        ToolPage.BILLS -> BillsScreen(state, onBack)
        ToolPage.CALENDAR -> CalendarScreen(state, onBack)
        ToolPage.NOTIFICATIONS -> NotificationsScreen(state, onBack)
        ToolPage.ADVANCED_ANALYTICS -> AdvancedAnalyticsScreen(state, onBack)
        ToolPage.AI_INSIGHTS -> AiInsightsScreen(state, onBack)
        ToolPage.RECURRING -> RecurringScreen(state, onBack)
        ToolPage.ADVANCED_FILTERS -> AdvancedFiltersScreen(state, onBack)
        ToolPage.APPEARANCE -> AppearanceScreen(state, onBack, onDarkModeChange)
        ToolPage.LANGUAGE -> LanguageScreen(state, onBack)
        ToolPage.CURRENCY -> CurrencyScreen(state, onBack)
        ToolPage.ACCESSIBILITY -> AccessibilityScreen(state, onBack)
        ToolPage.PREMIUM_THEMES -> PremiumThemesScreen(state, onBack)
        ToolPage.PREMIUM_ICONS -> PremiumIconsScreen(state, onBack)
        ToolPage.SECURITY -> SecurityScreen(state, onBack, onSignOut)
        ToolPage.BACKUP -> BackupScreen(state, onBack)
        ToolPage.RESTORE -> RestoreScreen(state, onBack)
        ToolPage.EXPORT -> ExportScreen(state, onBack)
        ToolPage.IMPORT -> ImportScreen(state, onBack)
        ToolPage.EDIT_PROFILE -> EditProfileScreen(state, onBack)
        ToolPage.PLANS -> PlansScreen(state, onBack)
        ToolPage.QUICK_PROFILE -> QuickProfileScreen(state, onBack, onOpenProfileTab)
        ToolPage.ANALYTICS_FULL -> FinancialHealthScreen(state, onBack)
        ToolPage.HELP -> HelpCenterScreen(onBack)
        ToolPage.FAQ -> StaticInfoScreen("Frequently Asked Questions", faqText, onBack)
        ToolPage.CONTACT -> ContactScreen(onBack)
        ToolPage.PRIVACY -> StaticInfoScreen("Privacy", privacyText, onBack)
        ToolPage.TERMS -> StaticInfoScreen("Terms of Service", termsText, onBack)
        ToolPage.LICENSES -> StaticInfoScreen("Open Source Licenses", licensesText, onBack)
        ToolPage.ABOUT -> AboutScreen(onBack)
        else -> StaticInfoScreen("MoneyMate", "This feature is available from the main app.", onBack)
    }
}



// -----------------------------------------------------------------------------
// Section: QuickProfileScreen
// Purpose: Renders the Quick Profile Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun QuickProfileScreen(
    state: MoneyMateState,
    onBack: () -> Unit,
    onOpenFullSettings: () -> Unit
) {
    val c = LocalMoneyMateTokens.current
    val name = state.user?.name ?: if (state.guestMode) "Guest User" else "MoneyMate User"
    val initials = name.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }.ifBlank { "MM" }
    Column(Modifier.fillMaxSize().background(c.background)) {
        PageTitle("Profile", onBack = onBack)
        Column(Modifier.padding(horizontal = 20.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(64.dp).background(c.action, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, color = androidx.compose.ui.graphics.Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(name, color = c.primaryText, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                    Text("Member since Feb 2024", color = c.mutedText, fontSize = 12.5.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            PrimaryButton("Open full settings", onClick = onOpenFullSettings)
        }
    }
}


// -----------------------------------------------------------------------------
// Section: FinancialHealthScreen
// Purpose: Renders the Financial Health Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun FinancialHealthScreen(state: MoneyMateState, onBack: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    val income = state.monthly.summary.totalIncome
    val expense = state.monthly.summary.totalExpense
    val savingsRate = if (income > 0) (((income - expense) / income) * 100).coerceIn(0.0, 100.0) else 0.0
    val exceeded = state.budgetProgress.budgets.count { it.status == "exceeded" }
    val adherence = (100 - exceeded * 14).coerceIn(0, 100)
    val overdue = state.bills.count { it.status.equals("overdue", true) }
    val onTime = (100 - overdue * 8).coerceIn(0, 100)
    val score = (72 + (savingsRate * .22).toInt() - exceeded * 8 - overdue * 10).coerceIn(0, 100)
    val emergencyCoverage = if (expense > 0) state.dashboard.summary.totalSavings / expense else 0.0

    LazyColumn(Modifier.fillMaxSize().background(c.background), contentPadding = PaddingValues(bottom = 30.dp)) {
        item { PageTitle("Financial Health", onBack = onBack) }
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                HtmlCard(Modifier.padding(top = 10.dp), padding = PaddingValues(30.dp)) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(130.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { score / 100f },
                                modifier = Modifier.fillMaxSize(),
                                color = c.action,
                                trackColor = c.divider,
                                strokeWidth = 12.dp
                            )
                            Text(score.toString(), color = c.primaryText, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(if (score >= 65) "Good standing" else "Needs attention", color = c.primaryText, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                        Text(
                            "Score based on savings rate, budget adherence and bill payment history.",
                            color = c.mutedText,
                            fontSize = 12.5.sp,
                            modifier = Modifier.padding(top = 6.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                Spacer(Modifier.height(22.dp))
                HtmlCard(padding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                    HealthMetricRow("Savings rate", "${savingsRate.toInt()}%", true)
                    HtmlDivider()
                    HealthMetricRow("Budget adherence", "$adherence%")
                    HtmlDivider()
                    HealthMetricRow("On-time bill payments", "$onTime%")
                    HtmlDivider()
                    HealthMetricRow("Emergency fund coverage", String.format(java.util.Locale.US, "%.1f mo", emergencyCoverage))
                }
            }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: HealthMetricRow
// Purpose: Encapsulates the Health Metric Row section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun HealthMetricRow(label: String, value: String, positive: Boolean = false) {
    val c = LocalMoneyMateTokens.current
    Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = c.secondaryText, fontSize = 13.5.sp, modifier = Modifier.weight(1f))
        Text(value, color = if (positive) c.success else c.primaryText, fontWeight = FontWeight.ExtraBold)
    }
}


// -----------------------------------------------------------------------------
// Section: AccountsScreen
// Purpose: Renders the Accounts Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun AccountsScreen(state: MoneyMateState, onBack: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    var add by remember { mutableStateOf(false) }
    var transfer by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(c.background)) {
        HtmlTopBar("Accounts", onBack = onBack) {
            HtmlIconButton(Icons.Filled.Add, "Link account", { add = true })
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 40.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = c.lightAction
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("Net worth", color = c.secondaryText, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            money(state.accounts.sumOf { it.balance.toDoubleOrNull() ?: 0.0 }, state.currency),
                            color = c.primaryText,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            if (state.accounts.isEmpty()) {
                item { EmptyState("No accounts", "Create an account to start tracking money.") }
            } else {
                items(state.accounts, key = { it.id }) { account ->
                    HtmlCard(Modifier.padding(bottom = 10.dp), padding = PaddingValues(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(44.dp).background(accountTint(account.type, c), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(accountIcon(account.type), null, tint = accountAccent(account.type, c), modifier = Modifier.size(21.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(account.name, color = c.primaryText, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                                Text(accountTypeLabel(account.type), color = c.mutedText, fontSize = 12.sp)
                            }
                            Text(
                                money(account.balance, account.currency),
                                color = if ((account.balance.toDoubleOrNull() ?: 0.0) < 0) c.error else c.primaryText,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.5.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(2.dp))
                OutlinedButton(
                    onClick = { transfer = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.SwapHoriz, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("Transfer between accounts", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    if (add) AccountDialog(state) { add = false }
    if (transfer) TransferDialog(state) { transfer = false }
}


// -----------------------------------------------------------------------------
// Section: BudgetsScreen
// Purpose: Renders the Budgets Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun BudgetsScreen(state: MoneyMateState, onBack: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    var add by remember { mutableStateOf(false) }
    val totalBudget = state.budgetProgress.totalBudget
    val totalSpent = state.budgetProgress.totalSpent
    val totalPct = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f

    Column(Modifier.fillMaxSize().background(c.background)) {
        HtmlTopBar("Budgets", onBack = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 40.dp)
        ) {
            item {
                HtmlCard(padding = PaddingValues(18.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Total monthly budget", color = c.secondaryText, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text("${money(totalSpent, state.currency)} / ${money(totalBudget, state.currency)}", color = c.primaryText, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.height(8.dp))
                    ProgressLine(totalPct, warning = totalPct >= .8f && totalPct < 1f, exceeded = totalPct >= 1f)
                }
                Spacer(Modifier.height(16.dp))
                HtmlSectionHeader("Categories", "+ New budget", onAction = { add = true })
                Spacer(Modifier.height(8.dp))
            }

            if (state.budgetProgress.budgets.isEmpty()) {
                item { EmptyState("No budgets yet", "Create a monthly category budget to stay in control.") }
            } else {
                items(state.budgetProgress.budgets, key = { it.budgetId }) { p ->
                    HtmlCard(Modifier.padding(bottom = 12.dp), padding = PaddingValues(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(38.dp).background(c.lightAction, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.PieChart, null, tint = c.action, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(p.categoryName, color = c.primaryText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${money(p.spent, state.currency)} of ${money(p.budgetAmount, state.currency)}", color = c.mutedText, fontSize = 12.sp)
                            }
                            Text(
                                "${p.percentage.toInt()}%",
                                color = when (p.status) { "exceeded" -> c.error; "warning" -> c.warning; else -> c.primaryText },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        ProgressLine((p.percentage / 100).toFloat(), p.status == "warning", p.status == "exceeded")
                        Spacer(Modifier.height(7.dp))
                        Text("${money(p.remaining, state.currency)} Remaining", color = c.mutedText, fontSize = 11.5.sp)
                    }
                }
            }
        }
    }
    if (add) BudgetDialog(state) { add = false }
}


// -----------------------------------------------------------------------------
// Section: GoalsScreen
// Purpose: Renders the Goals Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun GoalsScreen(state: MoneyMateState, onBack: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    var add by remember { mutableStateOf(false) }
    var contribute by remember { mutableStateOf<SavingsGoal?>(null) }

    Column(Modifier.fillMaxSize().background(c.background)) {
        HtmlTopBar("Savings Goals", onBack = onBack) {
            HtmlIconButton(Icons.Filled.Add, "Add goal", { add = true })
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 40.dp)
        ) {
            if (state.savingsGoals.isEmpty()) {
                item { EmptyState("No goals yet", "Create your first savings goal and start tracking progress.") }
            } else {
                items(state.savingsGoals, key = { it.id }) { goal ->
                    val target = goal.targetAmount.toDoubleOrNull() ?: 0.0
                    val saved = goal.savedAmount.toDoubleOrNull() ?: 0.0
                    val pct = if (target > 0) (saved / target).coerceIn(0.0, 1.0) else 0.0
                    HtmlCard(Modifier.padding(bottom = 14.dp), padding = PaddingValues(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { pct.toFloat() },
                                    modifier = Modifier.fillMaxSize(),
                                    color = c.action,
                                    trackColor = c.divider,
                                    strokeWidth = 6.dp
                                )
                                Text("${(pct * 100).toInt()}%", color = c.primaryText, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(goal.name, color = c.primaryText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("Expected completion: ${displayDate(goal.targetDate)}", color = c.mutedText, fontSize = 12.sp, modifier = Modifier.padding(top = 1.dp))
                            }
                            Box(Modifier.size(34.dp).background(c.lightAction, RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Flag, null, tint = c.action, modifier = Modifier.size(17.dp))
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(Modifier.fillMaxWidth()) {
                            GoalMetric("Saved", money(saved, state.currency), Modifier.weight(1f))
                            GoalMetric("Remaining", money((target - saved).coerceAtLeast(0.0), state.currency), Modifier.weight(1f))
                            GoalMetric("Target", money(target, state.currency), Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { contribute = goal },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(13.dp)
                        ) {
                            Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Add funds", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
    if (add) GoalDialog(state) { add = false }
    contribute?.let { ContributionDialog(state, it) { contribute = null } }
}


// -----------------------------------------------------------------------------
// Section: GoalMetric
// Purpose: Encapsulates the Goal Metric section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun GoalMetric(label: String, value: String, modifier: Modifier = Modifier) {
    val c = LocalMoneyMateTokens.current
    Column(modifier) {
        Text(label, color = c.mutedText, fontSize = 11.5.sp)
        Text(value, color = c.primaryText, fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 2.dp))
    }
}


// -----------------------------------------------------------------------------
// Section: BillsScreen
// Purpose: Renders the Bills Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun BillsScreen(state: MoneyMateState, onBack: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf("all") }
    var add by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    fun effectiveStatus(b: Bill): String {
        val due = runCatching { LocalDate.parse(b.dueDate.take(10)) }.getOrNull()
        return if (b.status == "upcoming" && due != null && due.isBefore(today)) "overdue" else b.status
    }
    val visible = state.bills.filter { tab == "all" || effectiveStatus(it) == tab }

    Column(Modifier.fillMaxSize().background(c.background)) {
        HtmlTopBar("Bills", onBack = onBack) {
            HtmlIconButton(Icons.Filled.Add, "Add bill", { add = true })
        }
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("all" to "All", "upcoming" to "Upcoming", "overdue" to "Overdue", "paid" to "Paid").forEach { (key, label) ->
                HtmlChip(label, tab == key, { tab = key })
            }
        }
        if (visible.isEmpty()) {
            EmptyState("No bills here", "There's nothing in this category right now.")
        } else {
            LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 40.dp)) {
                items(visible, key = { it.id }) { bill ->
                    val status = effectiveStatus(bill)
                    val accent = when (status) { "paid" -> c.success; "overdue" -> c.error; else -> c.action }
                    HtmlCard(Modifier.padding(bottom = 10.dp), padding = PaddingValues(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).background(accent.copy(alpha = .12f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.ReceiptLong, null, tint = accent, modifier = Modifier.size(21.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(bill.name, color = c.primaryText, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Due Date: ${displayDate(bill.dueDate)}", color = c.mutedText, fontSize = 12.sp, modifier = Modifier.padding(top = 1.dp))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(money(bill.amount, state.currency), color = c.primaryText, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                Surface(shape = RoundedCornerShape(8.dp), color = accent.copy(alpha = .12f), modifier = Modifier.padding(top = 4.dp)) {
                                    Text(status.replaceFirstChar(Char::uppercase), color = accent, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
                                }
                            }
                        }
                        if (status != "paid") {
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        state.updateBill(bill.id, BillRequest(bill.name, bill.amount.toDoubleOrNull() ?: 0.0, bill.dueDate, "paid"))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(42.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("Mark as Paid", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
    if (add) BillDialog(state) { add = false }
}


// -----------------------------------------------------------------------------
// Section: CalendarScreen
// Purpose: Renders the Calendar Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun CalendarScreen(state: MoneyMateState, onBack: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    var month by remember { mutableStateOf(YearMonth.now()) }
    var selectedDay by remember { mutableStateOf(LocalDate.now()) }
    val first = month.atDay(1)
    val offset = first.dayOfWeek.value % 7
    val days = month.lengthOfMonth()
    val cells = List(offset) { null } + (1..days).map { month.atDay(it) }
    val padded = cells + List((7 - cells.size % 7) % 7) { null }

    fun hasIncome(d: LocalDate) = state.transactions.any { it.type == "income" && it.occurredAt.take(10) == d.toString() }
    fun hasExpense(d: LocalDate) = state.transactions.any { it.type == "expense" && it.occurredAt.take(10) == d.toString() }
    fun hasBill(d: LocalDate) = state.bills.any { it.dueDate.take(10) == d.toString() }
    fun hasGoal(d: LocalDate) = state.savingsGoals.any { it.targetDate?.take(10) == d.toString() }

    Column(Modifier.fillMaxSize().background(c.background)) {
        HtmlTopBar("Calendar", onBack = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 40.dp)
        ) {
            item {
                HtmlCard(padding = PaddingValues(16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        HtmlIconButton(Icons.Filled.ChevronLeft, "Previous month", { month = month.minusMonths(1) })
                        Text(
                            month.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH) + " " + month.year,
                            color = c.primaryText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        HtmlIconButton(Icons.Filled.ChevronRight, "Next month", { month = month.plusMonths(1) })
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth()) {
                        listOf("S","M","T","W","T","F","S").forEach { day ->
                            Text(day, color = c.mutedText, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    padded.chunked(7).forEach { week ->
                        Row(Modifier.fillMaxWidth()) {
                            week.forEach { date ->
                                Box(
                                    Modifier.weight(1f).aspectRatio(1f).padding(2.dp)
                                        .then(if (date != null) Modifier.clickable { selectedDay = date } else Modifier),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (date != null) {
                                        val selected = date == selectedDay
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                Modifier.size(30.dp).background(if (selected) c.action else Color.Transparent, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(date.dayOfMonth.toString(), color = if (selected) Color.White else c.primaryText, fontSize = 12.sp, fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium)
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.height(6.dp)) {
                                                if (hasIncome(date)) Box(Modifier.size(4.dp).background(c.success, CircleShape))
                                                if (hasExpense(date)) Box(Modifier.size(4.dp).background(c.error, CircleShape))
                                                if (hasBill(date)) Box(Modifier.size(4.dp).background(c.action, CircleShape))
                                                if (hasGoal(date)) Box(Modifier.size(4.dp).background(c.warning, CircleShape))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CalendarLegend(c.success, "Income")
                        CalendarLegend(c.error, "Expense")
                        CalendarLegend(c.action, "Bills")
                        CalendarLegend(c.warning, "Goals")
                    }
                }
                Spacer(Modifier.height(22.dp))
                Text("Daily summary", color = c.primaryText, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(10.dp))
                HtmlCard(padding = PaddingValues(14.dp)) {
                    val txs = state.transactions.filter { it.occurredAt.take(10) == selectedDay.toString() }
                    val bills = state.bills.filter { it.dueDate.take(10) == selectedDay.toString() }
                    val goals = state.savingsGoals.filter { it.targetDate?.take(10) == selectedDay.toString() }
                    if (txs.isEmpty() && bills.isEmpty() && goals.isEmpty()) {
                        Text("No activity", color = c.primaryText, fontWeight = FontWeight.Bold)
                        Text("No transactions were recorded on this day.", color = c.mutedText, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))
                    } else {
                        txs.forEachIndexed { index, tx ->
                            TransactionRow(state, tx, null)
                            if (index < txs.lastIndex || bills.isNotEmpty() || goals.isNotEmpty()) HtmlDivider()
                        }
                        bills.forEach { b -> CalendarSummaryRow("Bill", b.name, money(b.amount, state.currency), c.action) }
                        goals.forEach { g -> CalendarSummaryRow("Goal", g.name, "Target", c.warning) }
                    }
                }
            }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: CalendarLegend
// Purpose: Encapsulates the Calendar Legend section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun CalendarLegend(color: Color, label: String) {
    val c = LocalMoneyMateTokens.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).background(color, CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(label, color = c.mutedText, fontSize = 9.5.sp)
    }
}


// -----------------------------------------------------------------------------
// Section: CalendarSummaryRow
// Purpose: Encapsulates the Calendar Summary Row section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun CalendarSummaryRow(kind: String, title: String, value: String, accent: Color) {
    val c = LocalMoneyMateTokens.current
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(34.dp).background(accent.copy(alpha = .12f), RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
            Text(kind.take(1), color = accent, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.width(10.dp))
        Text(title, color = c.primaryText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Text(value, color = c.secondaryText, fontSize = 12.sp)
    }
}


// -----------------------------------------------------------------------------
// Section: NotificationsScreen
// Purpose: Renders the Notifications Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun NotificationsScreen(state: MoneyMateState, onBack: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().background(c.background)) {
        HtmlTopBar("Notifications", onBack = onBack)
        if (state.notifications.isEmpty()) {
            EmptyState("No notifications", "You're all caught up.")
        } else {
            LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 40.dp)) {
                items(state.notifications, key = { it.id }) { n ->
                    val accent = notificationAccent(n.type, c)
                    Surface(
                        onClick = { if (!n.isRead) scope.launch { state.markNotificationRead(n.id) } },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = c.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, c.divider),
                        shadowElevation = 1.dp
                    ) {
                        Row(Modifier.fillMaxWidth()) {
                            Box(Modifier.width(3.dp).fillMaxHeight().background(accent))
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                                Box(Modifier.size(42.dp).background(accent.copy(alpha = .12f), RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                                    Icon(notificationIcon(n.type), null, tint = accent, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(11.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(n.title, color = c.primaryText, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                                    Surface(shape = RoundedCornerShape(8.dp), color = accent.copy(alpha = .12f), modifier = Modifier.padding(top = 5.dp)) {
                                        Text(notificationTypeLabel(n.type), color = accent, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
                                    }
                                    Text(n.message, color = c.mutedText, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 6.dp))
                                    Text(displayDate(n.createdAt), color = c.mutedText, fontSize = 10.5.sp, modifier = Modifier.padding(top = 6.dp))
                                }
                                if (!n.isRead) Box(Modifier.padding(top = 4.dp).size(7.dp).background(accent, CircleShape))
                            }
                        }
                    }
                }
            }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: accountTypeLabel
// Purpose: Encapsulates the account Type Label section of this file.
// -----------------------------------------------------------------------------
private fun accountTypeLabel(type: String): String = when (type.lowercase()) {
    "cash" -> "Cash"
    "bank", "checking" -> "Bank Account"
    "savings" -> "Savings"
    "credit", "credit_card" -> "Credit Card"
    else -> type.replace('_', ' ').replaceFirstChar(Char::uppercase)
}


// -----------------------------------------------------------------------------
// Section: accountAccent
// Purpose: Encapsulates the account Accent section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun accountAccent(type: String, c: com.moneymate.app.ui.theme.MoneyMateTokens): Color = when (type.lowercase()) {
    "cash" -> c.success
    "savings" -> c.action
    "credit", "credit_card" -> c.warning
    else -> c.brand
}


// -----------------------------------------------------------------------------
// Section: accountTint
// Purpose: Encapsulates the account Tint section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun accountTint(type: String, c: com.moneymate.app.ui.theme.MoneyMateTokens): Color = accountAccent(type, c).copy(alpha = .12f)


// -----------------------------------------------------------------------------
// Section: accountIcon
// Purpose: Encapsulates the account Icon section of this file.
// -----------------------------------------------------------------------------
private fun accountIcon(type: String) = when (type.lowercase()) {
    "cash" -> Icons.Filled.Payments
    "credit", "credit_card" -> Icons.Filled.CreditCard
    "savings" -> Icons.Filled.Savings
    else -> Icons.Filled.AccountBalance
}


// -----------------------------------------------------------------------------
// Section: notificationAccent
// Purpose: Encapsulates the notification Accent section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun notificationAccent(type: String, c: com.moneymate.app.ui.theme.MoneyMateTokens): Color = when (type.lowercase()) {
    "warning" -> c.warning
    "reminder" -> c.error
    "success" -> c.success
    else -> c.action
}


// -----------------------------------------------------------------------------
// Section: notificationIcon
// Purpose: Encapsulates the notification Icon section of this file.
// -----------------------------------------------------------------------------
private fun notificationIcon(type: String) = when (type.lowercase()) {
    "warning" -> Icons.Filled.Warning
    "reminder" -> Icons.Filled.Event
    "success" -> Icons.Filled.CheckCircle
    else -> Icons.Filled.Notifications
}


// -----------------------------------------------------------------------------
// Section: notificationTypeLabel
// Purpose: Encapsulates the notification Type Label section of this file.
// -----------------------------------------------------------------------------
private fun notificationTypeLabel(type: String): String = when (type.lowercase()) {
    "warning" -> "Budget Alert"
    "reminder" -> "Bill Reminder"
    "success" -> "Goal Achievement"
    else -> "Monthly Report"
}


// -----------------------------------------------------------------------------
// Section: AdvancedAnalyticsScreen
// Purpose: Renders the Advanced Analytics Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun AdvancedAnalyticsScreen(state:MoneyMateState,onBack:()->Unit){
    val c=LocalMoneyMateTokens.current;val income=state.monthly.summary.totalIncome;val expense=state.monthly.summary.totalExpense;val savingsRate=if(income>0)((income-expense)/income*100)else 0.0;val avg=state.transactions.filter{it.type=="expense"}.mapNotNull{it.amount.toDoubleOrNull()}.average().takeIf{!it.isNaN()}?:0.0;val biggest=state.transactions.filter{it.type=="expense"}.maxByOrNull{it.amount.toDoubleOrNull()?:0.0};val top=state.categoryBreakdown.categories.firstOrNull()
    LazyColumn(Modifier.fillMaxSize().background(c.background)){item{PageTitle("Advanced Analytics","Deeper signals from your MoneyMate data",onBack)};item{Row(Modifier.padding(horizontal=20.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)){StatCard("Savings Rate","${savingsRate.toInt()}%",savingsRate>=20,Modifier.weight(1f));StatCard("Avg Expense",money(avg,state.currency),false,Modifier.weight(1f))};Spacer(Modifier.height(10.dp));Row(Modifier.padding(horizontal=20.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)){StatCard("Top Category",top?.name?:"—",null,Modifier.weight(1f));StatCard("Largest Expense",biggest?.let{money(it.amount,state.currency)}?:"—",false,Modifier.weight(1f))}};item{Spacer(Modifier.height(18.dp));MMCard(Modifier.padding(horizontal=20.dp)){Text("Cash-flow health",color=c.primaryText,fontWeight=FontWeight.Bold);Spacer(Modifier.height(7.dp));ProgressLine((if(income>0)(income-expense).coerceAtLeast(0.0)/income else 0.0).toFloat());Spacer(Modifier.height(7.dp));Text("Net this month: ${money(income-expense,state.currency)}",color=c.secondaryText,fontSize=12.sp)}};item{Spacer(Modifier.height(12.dp));MMCard(Modifier.padding(horizontal=20.dp)){Text("Spending concentration",color=c.primaryText,fontWeight=FontWeight.Bold);Text(top?.let{"${it.name} represents ${it.percentage.toInt()}% of spending."}?:"Not enough spending data yet.",color=c.secondaryText,fontSize=12.sp)}}}
}


// -----------------------------------------------------------------------------
// Section: AiInsightsScreen
// Purpose: Renders the Ai Insights Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun AiInsightsScreen(state:MoneyMateState,onBack:()->Unit){
    val c=LocalMoneyMateTokens.current;val top=state.categoryBreakdown.categories.firstOrNull();val biggest=state.transactions.filter{it.type=="expense"}.maxByOrNull{it.amount.toDoubleOrNull()?:0.0};val income=state.monthly.summary.totalIncome;val expense=state.monthly.summary.totalExpense;val day=LocalDate.now().dayOfMonth.coerceAtLeast(1);val projected=expense/day*YearMonth.now().lengthOfMonth();val savingsRate=if(income>0)((income-expense)/income*100)else 0.0
    val insights=listOf(
        "Top Spending Category" to (top?.let{"${it.name} is your biggest category this month, making up about ${it.percentage.toInt()}% of total spending."}?:"Add a few more transactions to identify your top spending category."),
        "Largest Single Expense" to (biggest?.let{"${it.merchant?:"An expense"} is your largest single expense at ${money(it.amount,state.currency)}."}?:"No expense transactions yet."),
        "Projected Month-End Spend" to "At your current pace, you're on track to spend around ${money(projected,state.currency)} by month end.",
        (if(savingsRate>=20)"Healthy Savings Rate" else if(savingsRate<0)"Spending Exceeds Income" else "Savings Rate") to "${savingsRate.toInt()}% of this month's income remains after expenses."
    )
    LazyColumn(Modifier.fillMaxSize().background(c.background)){item{PageTitle("AI Financial Insights","Private, rules-based insights computed on device",onBack)};items(insights){(title,desc)->MMCard(Modifier.padding(horizontal=20.dp,vertical=5.dp)){Text(title,color=c.primaryText,fontWeight=FontWeight.Bold);Spacer(Modifier.height(5.dp));Text(desc,color=c.secondaryText,fontSize=13.sp)}};item{Text("These insights are informational and not financial advice.",color=c.mutedText,fontSize=11.sp,modifier=Modifier.padding(20.dp))}}
}


// -----------------------------------------------------------------------------
// Section: RecurringScreen
// Purpose: Renders the Recurring Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun RecurringScreen(state:MoneyMateState,onBack:()->Unit){
    val c=LocalMoneyMateTokens.current;var items by remember{mutableStateOf(state.recurring())};var showAdd by remember{mutableStateOf(false)}
    Column(Modifier.fillMaxSize().background(c.background)){PageTitle("Recurring Transactions","Due templates are posted when MoneyMate refreshes",onBack,"+ Add"){showAdd=true};if(items.isEmpty())EmptyState("No recurring transactions","Create a recurring salary, bill or expense template.")else LazyColumn{items(items,key={it.id}){r->MMCard(Modifier.padding(horizontal=20.dp,vertical=5.dp)){Row{Column(Modifier.weight(1f)){Text(r.title,color=c.primaryText,fontWeight=FontWeight.Bold);Text("${r.frequency.replaceFirstChar(Char::uppercase)} · next ${r.nextDate}",color=c.secondaryText,fontSize=12.sp)};Text(money(r.amount,state.currency),color=if(r.type=="income")c.success else c.error,fontWeight=FontWeight.Bold)};Row(verticalAlignment=Alignment.CenterVertically){Switch(checked=r.enabled,onCheckedChange={checked->items=items.map{if(it.id==r.id)it.copy(enabled=checked)else it};state.saveRecurring(items)});Spacer(Modifier.weight(1f));TextButton(onClick={items=items.filterNot{it.id==r.id};state.saveRecurring(items)}){Text("Delete",color=c.error)}}}}}}
    if(showAdd) RecurringDialog(state,onDismiss={showAdd=false},onSave={new->items=items+new;state.saveRecurring(items);showAdd=false})
}


// -----------------------------------------------------------------------------
// Section: RecurringDialog
// Purpose: Renders and manages the Recurring Dialog modal UI flow.
// -----------------------------------------------------------------------------
@Composable
private fun RecurringDialog(state:MoneyMateState,onDismiss:()->Unit,onSave:(RecurringTemplate)->Unit){
    var title by remember{mutableStateOf("")};var amount by remember{mutableStateOf("")};var type by remember{mutableStateOf("expense")};var frequency by remember{mutableStateOf("monthly")};var date by remember{mutableStateOf(LocalDate.now().plusDays(1).toString())};var account by remember{mutableStateOf(state.accounts.firstOrNull())};var category by remember{mutableStateOf(CategoryCatalog.expenseCategories(state.categories).firstOrNull())}
    AlertDialog(onDismissRequest=onDismiss,title={Text("Recurring Transaction")},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(title,{title=it},label={Text("Title")});OutlinedTextField(amount,{amount=it},label={Text("Amount")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal));Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("expense","income").forEach{FilterChip(selected=type==it,onClick={type=it;category=CategoryCatalog.ordered(state.categories,it).firstOrNull()},label={Text(it.replaceFirstChar(Char::uppercase))})}};PickerFieldPublic("Account",state.accounts,account,{it.name}){account=it};PickerFieldPublic("Category",CategoryCatalog.ordered(state.categories,type),category,{it.name}){category=it};Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("weekly","monthly","yearly").forEach{FilterChip(selected=frequency==it,onClick={frequency=it},label={Text(it.take(1).uppercase()+it.drop(1))})}};OutlinedTextField(date,{date=it},label={Text("Next date YYYY-MM-DD")})}},confirmButton={TextButton(onClick={val a=amount.toDoubleOrNull();if(title.isNotBlank()&&a!=null&&a>0&&account!=null&&runCatching{LocalDate.parse(date)}.isSuccess)onSave(RecurringTemplate(title=title.trim(),type=type,amount=a,accountId=account!!.id,categoryId=category?.id,frequency=frequency,nextDate=date))}){Text("Save")}},dismissButton={TextButton(onClick=onDismiss){Text("Cancel")}})
}


// -----------------------------------------------------------------------------
// Section: AdvancedFiltersScreen
// Purpose: Renders the Advanced Filters Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun AdvancedFiltersScreen(state:MoneyMateState,onBack:()->Unit){
    val c=LocalMoneyMateTokens.current;var query by remember{mutableStateOf("")};var type by remember{mutableStateOf("all")};var min by remember{mutableStateOf("")};var max by remember{mutableStateOf("")};var accountId by remember{mutableStateOf<Int?>(null)};var categoryId by remember{mutableStateOf<Int?>(null)}
    val result=state.transactions.filter{t->val a=t.amount.toDoubleOrNull()?:0.0;(type=="all"||t.type==type)&&(query.isBlank()||t.merchant.orEmpty().contains(query,true)||t.notes.orEmpty().contains(query,true))&&(min.toDoubleOrNull()?.let{a>=it}?:true)&&(max.toDoubleOrNull()?.let{a<=it}?:true)&&(accountId==null||t.accountId==accountId)&&(categoryId==null||t.categoryId==categoryId)}
    Column(Modifier.fillMaxSize().background(c.background)){PageTitle("Advanced Search & Filters","${result.size} matches",onBack);Column(Modifier.padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(query,{query=it},label={Text("Search")},modifier=Modifier.fillMaxWidth());Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("all","expense","income").forEach{Pill(it.replaceFirstChar(Char::uppercase),type==it){type=it}}};Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(min,{min=it},label={Text("Min")},modifier=Modifier.weight(1f),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal));OutlinedTextField(max,{max=it},label={Text("Max")},modifier=Modifier.weight(1f),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal))};PickerFieldPublic("Any account",listOf<Account?>(null)+state.accounts,accountId?.let{id->state.accounts.firstOrNull{it.id==id}},{it?.name?:"Any account"}){accountId=it?.id};PickerFieldPublic("Any category",listOf<Category?>(null)+CategoryCatalog.ordered(state.categories),categoryId?.let{id->state.categories.firstOrNull{it.id==id}},{it?.name?:"Any category"}){categoryId=it?.id}};Spacer(Modifier.height(8.dp));LazyColumn{items(result,key={it.id}){TransactionRow(state,it,null)}}}
}


// -----------------------------------------------------------------------------
// Section: AppearanceScreen
// Purpose: Renders the Appearance Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun AppearanceScreen(state:MoneyMateState,onBack:()->Unit,onDarkModeChange:(Boolean)->Unit){val c=LocalMoneyMateTokens.current;Column(Modifier.fillMaxSize().background(c.background)){PageTitle("Appearance","Default MoneyMate theme follows the supplied color system",onBack);MMCard(Modifier.padding(20.dp)){Row(verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Dark mode",color=c.primaryText,fontWeight=FontWeight.Bold);Text("Uses the PDF-defined dark companion surface hierarchy.",color=c.secondaryText,fontSize=12.sp)};Switch(checked=state.prefs.darkMode,onCheckedChange={state.prefs.darkMode=it;onDarkModeChange(it)})}}}}


// -----------------------------------------------------------------------------
// Section: LanguageScreen
// Purpose: Renders the Language Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun LanguageScreen(state:MoneyMateState,onBack:()->Unit){val c=LocalMoneyMateTokens.current;val scope=rememberCoroutineScope();Column(Modifier.fillMaxSize().background(c.background)){PageTitle("Language","Choose your preferred app/profile language",onBack);listOf("en" to "English","bn" to "বাংলা").forEach{(code,label)->MMCard(Modifier.padding(horizontal=20.dp,vertical=5.dp),onClick={scope.launch{val u=state.user?:return@launch;if(state.updateProfile(ProfileRequest(u.name?:"MoneyMate User",u.username,u.currency,code))){state.prefs.language=code}}}){Row{Text(label,color=c.primaryText,modifier=Modifier.weight(1f),fontWeight=FontWeight.SemiBold);if(state.language==code)Text("Selected",color=c.action)}}}}}


// -----------------------------------------------------------------------------
// Section: CurrencyScreen
// Purpose: Renders the Currency Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun CurrencyScreen(state:MoneyMateState,onBack:()->Unit){val c=LocalMoneyMateTokens.current;val scope=rememberCoroutineScope();val currencies=listOf("USD" to "US Dollar","BDT" to "Bangladeshi Taka","MYR" to "Malaysian Ringgit","EUR" to "Euro","GBP" to "British Pound","SGD" to "Singapore Dollar","INR" to "Indian Rupee");Column(Modifier.fillMaxSize().background(c.background)){PageTitle("Currency","Display currency; existing amounts are not converted",onBack);LazyColumn{items(currencies){(code,name)->MMCard(Modifier.padding(horizontal=20.dp,vertical=4.dp),onClick={scope.launch{val u=state.user?:return@launch;if(state.updateProfile(ProfileRequest(u.name?:"MoneyMate User",u.username,code,u.language))){state.prefs.currency=code}}}){Row{Column(Modifier.weight(1f)){Text(code,color=c.primaryText,fontWeight=FontWeight.Bold);Text(name,color=c.secondaryText,fontSize=12.sp)};if(state.currency==code)Text("Selected",color=c.action)}}}}}}


// -----------------------------------------------------------------------------
// Section: AccessibilityScreen
// Purpose: Renders the Accessibility Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun AccessibilityScreen(state: MoneyMateState, onBack: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    var high by remember { mutableStateOf(state.prefs.highContrast) }
    var large by remember { mutableStateOf(state.prefs.largeText) }
    var reduce by remember { mutableStateOf(state.prefs.reduceMotion) }

    Column(Modifier.fillMaxSize().background(c.background)) {
        PageTitle("Accessibility", onBack = onBack)
        Text(
            "These settings apply instantly and are saved on this device.",
            color = c.secondaryText,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
        )
        Spacer(Modifier.height(10.dp))
        MMCard(Modifier.padding(horizontal = 20.dp)) {
            AccessibilityRow(Icons.Filled.Contrast, "High Contrast", "Stronger borders and text contrast", high) {
                high = it; state.prefs.highContrast = it
            }
            HorizontalDivider(color = c.divider)
            AccessibilityRow(Icons.Filled.FormatSize, "Large Text", "Increase text size app-wide", large) {
                large = it; state.prefs.largeText = it
            }
            HorizontalDivider(color = c.divider)
            AccessibilityRow(Icons.Filled.MotionPhotosOff, "Reduce Motion", "Minimize animations and transitions", reduce) {
                reduce = it; state.prefs.reduceMotion = it
            }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: AccessibilityRow
// Purpose: Encapsulates the Accessibility Row section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun AccessibilityRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    val c = LocalMoneyMateTokens.current
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(38.dp).background(c.lightAction, RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = c.action, modifier = Modifier.size(19.dp)) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = c.primaryText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, color = c.secondaryText, fontSize = 11.5.sp)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}


// -----------------------------------------------------------------------------
// Section: PremiumThemesScreen
// Purpose: Renders the Premium Themes Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun PremiumThemesScreen(state: MoneyMateState, onBack: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    Column(Modifier.fillMaxSize().background(c.background)) {
        PremiumHeader("Premium Themes", onBack)
        LazyColumn(contentPadding = PaddingValues(start=20.dp,end=20.dp,bottom=28.dp)) {
            item {
                Text("Pick an accent theme for MoneyMate. Your choice applies instantly and is saved on this device.", color=c.secondaryText,fontSize=13.sp,modifier=Modifier.padding(vertical=12.dp))
            }
            items(PremiumThemes) { theme ->
                val selected=state.selectedPremiumTheme==theme.name
                Surface(
                    onClick={state.setPremiumTheme(theme.name)},
                    modifier=Modifier.fillMaxWidth().padding(bottom=12.dp),
                    shape=RoundedCornerShape(18.dp),
                    color=c.surface,
                    border=BorderStroke(if(selected)2.dp else 1.dp,if(selected)c.warning else c.divider),
                    shadowElevation=1.dp
                ) {
                    Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically){
                        PremiumHomeMini(theme)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)){
                            Text(theme.name,color=c.primaryText,fontSize=15.sp,fontWeight=FontWeight.ExtraBold)
                            Text("Full app re-theme",color=c.secondaryText,fontSize=11.5.sp)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){
                                listOf(theme.action,theme.brand,theme.success).forEach{col->Box(Modifier.size(14.dp).background(col,CircleShape))}
                            }
                        }
                        Box(Modifier.size(24.dp).background(if(selected)c.warning else c.background,CircleShape),contentAlignment=Alignment.Center){if(selected)Icon(Icons.Filled.Check,null,tint=Color.White,modifier=Modifier.size(15.dp))}
                    }
                }
            }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: PremiumHeader
// Purpose: Encapsulates the Premium Header section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun PremiumHeader(title:String,onBack:()->Unit){
    val c=LocalMoneyMateTokens.current
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().background(Brush.horizontalGradient(listOf(c.brand,c.primaryText))).padding(horizontal=14.dp,vertical=10.dp),
        verticalAlignment=Alignment.CenterVertically
    ){
        Surface(onClick=onBack,shape=RoundedCornerShape(11.dp),color=Color.White.copy(alpha=.10f),modifier=Modifier.size(44.dp)){Box(contentAlignment=Alignment.Center){Icon(Icons.Filled.ArrowBack,null,tint=Color.White)}}
        Spacer(Modifier.width(10.dp));Text(title,color=c.warning,fontSize=20.sp,fontWeight=FontWeight.ExtraBold)
    }
}


// -----------------------------------------------------------------------------
// Section: PremiumHomeMini
// Purpose: Encapsulates the Premium Home Mini section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun PremiumHomeMini(theme:com.moneymate.app.ui.theme.PremiumThemeSpec){
    Column(Modifier.width(96.dp).height(150.dp).clip(RoundedCornerShape(14.dp)).background(theme.background).border(BorderStroke(1.dp,theme.border),RoundedCornerShape(14.dp))){
        Column(Modifier.fillMaxWidth().height(66.dp).background(Brush.linearGradient(listOf(theme.action,theme.brand))).padding(8.dp)){
            Row(Modifier.fillMaxWidth()){Box(Modifier.width(20.dp).height(7.dp).background(Color.White.copy(.25f),RoundedCornerShape(4.dp)));Spacer(Modifier.weight(1f));Box(Modifier.size(12.dp).background(Color.White.copy(.25f),CircleShape))}
            Spacer(Modifier.height(8.dp));Text("BALANCE",color=Color.White.copy(.65f),fontSize=5.5.sp,fontWeight=FontWeight.Bold);Text("$12,480",color=Color.White,fontSize=11.sp,fontWeight=FontWeight.ExtraBold)
        }
        Row(Modifier.padding(8.dp),horizontalArrangement=Arrangement.spacedBy(5.dp)){repeat(3){Box(Modifier.size(16.dp).background(theme.lightAction,RoundedCornerShape(6.dp)),contentAlignment=Alignment.Center){Box(Modifier.size(6.dp).background(theme.action,RoundedCornerShape(2.dp)))}}}
        Column(Modifier.padding(horizontal=8.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){repeat(2){Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(14.dp).background(theme.lightAction,RoundedCornerShape(5.dp)));Spacer(Modifier.width(5.dp));Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)){Box(Modifier.fillMaxWidth(.7f).height(3.dp).background(theme.primaryText.copy(.55f),RoundedCornerShape(2.dp)));Box(Modifier.fillMaxWidth(.45f).height(3.dp).background(theme.mutedText.copy(.5f),RoundedCornerShape(2.dp)))};Spacer(Modifier.width(5.dp));Box(Modifier.width(16.dp).height(3.dp).background(theme.mutedText.copy(.45f),RoundedCornerShape(2.dp)))}}}
    }
}


// -----------------------------------------------------------------------------
// Section: PremiumIconsScreen
// Purpose: Renders the Premium Icons Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun PremiumIconsScreen(state: MoneyMateState, onBack: () -> Unit) {
    val c=LocalMoneyMateTokens.current
    data class IconChoice(val id:String,val label:String,val start:Color,val end:Color,val exclusive:Boolean=false)
    val choices=listOf(
        IconChoice("Classic White","Classic White",c.surface,c.background),
        IconChoice("Midnight","Midnight",PremiumThemes[0].brand,PremiumThemes[0].darkBackground),
        IconChoice("Gold Premium","Gold Premium",c.warning,PremiumThemes[0].warning,true),
        IconChoice("Forest","Forest",PremiumThemes[2].success,PremiumThemes[2].brand),
        IconChoice("Ocean","Ocean",PremiumThemes[4].action,PremiumThemes[4].brand),
        IconChoice("Sunset","Sunset",c.warning,c.error),
        IconChoice("Royal","Royal",PremiumThemes[1].action,PremiumThemes[1].brand),
        IconChoice("Rose","Rose",PremiumThemes[3].action,PremiumThemes[3].brand,true)
    )
    Column(Modifier.fillMaxSize().background(c.background)){
        PremiumHeader("Premium App Icons",onBack)
        LazyColumn(contentPadding=PaddingValues(horizontal=20.dp,vertical=12.dp)){
            item{Text("Choose an alternate icon for your MoneyMate home screen shortcut.",color=c.secondaryText,fontSize=13.sp,modifier=Modifier.padding(bottom=16.dp))}
            items(choices.chunked(2)){row->
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(14.dp)){
                    row.forEach{choice->
                        val selected=state.selectedAppIcon==choice.id || (choice.id=="Classic White"&&state.selectedAppIcon=="Classic")
                        Surface(onClick={state.setAppIcon(choice.id)},modifier=Modifier.weight(1f).height(178.dp),shape=RoundedCornerShape(20.dp),color=c.surface,border=BorderStroke(if(selected)2.dp else 1.dp,if(selected)c.warning else c.divider),shadowElevation=1.dp){
                            Box{
                                if(choice.exclusive) Surface(shape=RoundedCornerShape(7.dp),color=c.warning,modifier=Modifier.align(Alignment.TopCenter).offset(y=(-7).dp)){Text("EXCLUSIVE",color=c.brand,fontSize=8.5.sp,fontWeight=FontWeight.ExtraBold,modifier=Modifier.padding(horizontal=8.dp,vertical=3.dp))}
                                Column(Modifier.fillMaxSize().padding(14.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
                                    Box(Modifier.size(78.dp).background(Brush.linearGradient(listOf(choice.start,choice.end)),RoundedCornerShape(21.dp)),contentAlignment=Alignment.Center){
                                        androidx.compose.foundation.Image(painterResource(R.drawable.moneymate_logo),null,modifier=Modifier.size(54.dp))
                                        if(selected)Box(Modifier.align(Alignment.TopEnd).size(22.dp).background(c.warning,CircleShape),contentAlignment=Alignment.Center){Icon(Icons.Filled.Check,null,tint=c.brand,modifier=Modifier.size(14.dp))}
                                    }
                                    Spacer(Modifier.height(10.dp));Text(choice.label,color=c.primaryText,fontWeight=FontWeight.ExtraBold,fontSize=12.5.sp)
                                }
                            }
                        }
                    }
                    if(row.size==1)Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: SecurityScreen
// Purpose: Renders the Security Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun SecurityScreen(
    state: MoneyMateState,
    onBack: () -> Unit,
    onSignOut: () -> Unit
) {
    val c = LocalMoneyMateTokens.current
    var pinDialog by remember { mutableStateOf(false) }
    var passDialog by remember { mutableStateOf(false) }
    var bio by remember { mutableStateOf(state.prefs.biometricEnabled) }
    var auto by remember { mutableStateOf(state.prefs.autoLockMinutes) }
    var deleteAccountDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().background(c.background)) {
        PageTitle(
            "Security",
            "PIN, device authentication and password controls",
            onBack
        )
        MMCard(Modifier.padding(20.dp)) {
            MenuInline(
                "PIN Lock",
                if (state.prefs.hasPin()) "Enabled" else "Off"
            ) { pinDialog = true }
            HorizontalDivider(color = c.divider)
            SettingSwitch(
                "Biometric / Device authentication",
                "Requires a PIN lock first",
                bio
            ) { enabled ->
                if (state.prefs.hasPin()) {
                    bio = enabled
                    state.prefs.biometricEnabled = enabled
                }
            }
            HorizontalDivider(color = c.divider)
            Text("Auto Lock", color = c.primaryText, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(1, 5, 15, 30).forEach { minutes ->
                    Pill("$minutes min", auto == minutes) {
                        auto = minutes
                        state.prefs.autoLockMinutes = minutes
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { passDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Change Account Password") }
            if (state.prefs.hasPin()) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { state.lockRequested = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Lock MoneyMate Now") }
                TextButton(onClick = {
                    state.prefs.setPin(null)
                    state.prefs.biometricEnabled = false
                    bio = false
                }) { Text("Remove PIN Lock", color = c.error) }
            }

            HorizontalDivider(color = c.divider, modifier = Modifier.padding(vertical = 10.dp))
            Text("Danger Zone", color = c.error, fontWeight = FontWeight.Bold)
            Text(
                "Permanently deletes your MoneyMate account and all associated finance data.",
                color = c.secondaryText,
                fontSize = 11.sp
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { deleteAccountDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Delete Account Permanently", color = c.error) }
        }
    }

    if (pinDialog) PinDialog(state) { pinDialog = false }
    if (passDialog) ChangePasswordDialog(state) { passDialog = false }

    if (deleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { deleteAccountDialog = false },
            title = { Text("Delete MoneyMate account?") },
            text = {
                Text(
                    "This permanently deletes your profile, accounts, transactions, budgets, savings goals, bills and notifications. This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        if (state.deleteUserAccount()) {
                            deleteAccountDialog = false
                            onSignOut()
                        }
                    }
                }) { Text("Delete Permanently", color = c.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteAccountDialog = false }) { Text("Cancel") }
            }
        )
    }
}


// -----------------------------------------------------------------------------
// Section: PinDialog
// Purpose: Renders and manages the Pin Dialog modal UI flow.
// -----------------------------------------------------------------------------
@Composable
private fun PinDialog(state:MoneyMateState,onDismiss:()->Unit){var pin by remember{mutableStateOf("")};var confirm by remember{mutableStateOf("")};var error by remember{mutableStateOf<String?>(null)};AlertDialog(onDismissRequest=onDismiss,title={Text(if(state.prefs.hasPin())"Change PIN" else "Set PIN")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(pin,{pin=it.filter(Char::isDigit).take(6)},label={Text("4–6 digit PIN")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword));OutlinedTextField(confirm,{confirm=it.filter(Char::isDigit).take(6)},label={Text("Confirm PIN")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.NumberPassword));error?.let{Text(it,color=MaterialTheme.colorScheme.error)}}},confirmButton={TextButton(onClick={if(pin.length !in 4..6||pin!=confirm)error="PINs must match and contain 4–6 digits." else{state.prefs.setPin(pin);onDismiss()}}){Text("Save")}},dismissButton={TextButton(onClick=onDismiss){Text("Cancel")}})}


// -----------------------------------------------------------------------------
// Section: ChangePasswordDialog
// Purpose: Renders and manages the Change Password Dialog modal UI flow.
// -----------------------------------------------------------------------------
@Composable
private fun ChangePasswordDialog(state:MoneyMateState,onDismiss:()->Unit){val scope=rememberCoroutineScope();var old by remember{mutableStateOf("")};var next by remember{mutableStateOf("")};var error by remember{mutableStateOf<String?>(null)};AlertDialog(onDismissRequest=onDismiss,title={Text("Change Password")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(old,{old=it},label={Text("Current password")});OutlinedTextField(next,{next=it},label={Text("New password")});error?.let{Text(it,color=MaterialTheme.colorScheme.error)}}},confirmButton={TextButton(onClick={scope.launch{if(state.changePassword(old,next))onDismiss()else error=state.error}}){Text("Change")}},dismissButton={TextButton(onClick=onDismiss){Text("Cancel")}})}


// -----------------------------------------------------------------------------
// Section: BackupScreen
// Purpose: Renders the Backup Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun BackupScreen(state: MoneyMateState, onBack: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pending by remember { mutableStateOf<ByteArray?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    var lastBackup by remember { mutableStateOf("Last backup: today, 6:12 AM") }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null && pending != null) {
            context.contentResolver.openOutputStream(uri)?.use { it.write(pending!!) }
            status = "Backup completed just now"
            lastBackup = "Last backup: just now"
        }
        pending = null
    }

    Column(Modifier.fillMaxSize().background(c.background)) {
        PageTitle("Backup", onBack = onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            PremiumRibbon()
            Spacer(Modifier.height(14.dp))
            MMCard(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.CloudDone, null, tint = c.action, modifier = Modifier.size(34.dp))
                    Spacer(Modifier.height(10.dp))
                    Text("Cloud Backup", color = c.primaryText, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                    Text(lastBackup, color = c.mutedText, fontSize = 12.5.sp, modifier = Modifier.padding(top = 2.dp))
                }
            }
            Spacer(Modifier.height(18.dp))
            PrimaryButton("Back Up Now") {
                scope.launch {
                    when (val r = state.repository.downloadBackup()) {
                        is RepoResult.Success -> { pending = r.data; launcher.launch("moneymate-backup-${LocalDate.now()}.json") }
                        is RepoResult.Error -> status = r.message
                    }
                }
            }
            Spacer(Modifier.height(22.dp))
            Text("BACKUP HISTORY", color = c.mutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            MMCard {
                Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(38.dp).background(c.lightAction, RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.CloudDone, null, tint = c.action, modifier = Modifier.size(19.dp))
                    }
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)) {
                        Text("MoneyMate Backup", color = c.primaryText, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                        Text(lastBackup, color = c.secondaryText, fontSize = 11.5.sp)
                    }
                    Icon(Icons.Filled.CheckCircle, null, tint = c.success, modifier = Modifier.size(18.dp))
                }
            }
            status?.let { Text(it, color = c.secondaryText, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp)) }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: PremiumRibbon
// Purpose: Encapsulates the Premium Ribbon section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun PremiumRibbon() {
    val c = LocalMoneyMateTokens.current
    Row(
        Modifier.background(c.lightWarning, RoundedCornerShape(100.dp)).padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.WorkspacePremium, null, tint = c.warning, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(5.dp))
        Text("Premium Feature", color = c.warning, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}


// -----------------------------------------------------------------------------
// Section: RestoreScreen
// Purpose: Renders the Restore Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun RestoreScreen(state:MoneyMateState,onBack:()->Unit){val c=LocalMoneyMateTokens.current;val context=LocalContext.current;val scope=rememberCoroutineScope();var file by remember{mutableStateOf<File?>(null)};var status by remember{mutableStateOf<String?>(null)};var confirmed by remember{mutableStateOf(false)};val launcher=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null){val temp=File(context.cacheDir,"restore-${System.currentTimeMillis()}.json");context.contentResolver.openInputStream(uri)?.use{input->temp.outputStream().use{input.copyTo(it)}};file=temp;scope.launch{when(val r=state.repository.validateRestore(temp)){is RepoResult.Success->{status="Backup is valid and ready to restore.";confirmed=true};is RepoResult.Error->{status=r.message;confirmed=false}}}}};Column(Modifier.fillMaxSize().background(c.background)){PageTitle("Restore","Validate before replacing financial data",onBack);MMCard(Modifier.padding(20.dp)){Text("Restore replaces your current finance records atomically. Your login credentials are not replaced.",color=c.secondaryText,fontSize=13.sp);Spacer(Modifier.height(12.dp));OutlinedButton(onClick={launcher.launch(arrayOf("application/json","text/plain"))},modifier=Modifier.fillMaxWidth()){Text("Choose Backup File")};status?.let{Spacer(Modifier.height(10.dp));Text(it,color=if(confirmed)c.success else c.error)};if(confirmed&&file!=null){Spacer(Modifier.height(12.dp));PrimaryButton("Confirm RESTORE"){scope.launch{when(val r=state.repository.restore(file!!)){is RepoResult.Success->{status="Restore complete. ${r.data.restored?.transactions?:0} transactions restored.";confirmed=false;state.loadAll(false)};is RepoResult.Error->{status=r.message}}}}}}}}


// -----------------------------------------------------------------------------
// Section: ExportScreen
// Purpose: Renders the Export Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun ExportScreen(state: MoneyMateState, onBack: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    val context = LocalContext.current
    var format by remember { mutableStateOf("CSV") }
    var range by remember { mutableStateOf("This Month") }
    var csvPending by remember { mutableStateOf<String?>(null) }
    var pdfPending by remember { mutableStateOf<ByteArray?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null && csvPending != null) { context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(csvPending!!) }; status = "Export Ready" }
        csvPending = null
    }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null && pdfPending != null) { context.contentResolver.openOutputStream(uri)?.use { it.write(pdfPending!!) }; status = "Export Ready" }
        pdfPending = null
    }
    Column(Modifier.fillMaxSize().background(c.background)) {
        PageTitle("Export Data", onBack = onBack)
        Column(Modifier.padding(horizontal = 20.dp)) {
            Text("Format", color = c.primaryText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill("CSV", format == "CSV") { format = "CSV" }
                Pill("PDF", format == "PDF") { format = "PDF" }
            }
            Spacer(Modifier.height(18.dp))
            Text("Date Range", color = c.primaryText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("This Month", "Last Month").forEach { Pill(it, range == it) { range = it } }
            }
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf("This Year", "All Time").forEach { Pill(it, range == it) { range = it } }
            }
            Spacer(Modifier.height(22.dp))
            PrimaryButton("Generate Export") {
                if (format == "CSV") { csvPending = buildCsv(state); csvLauncher.launch("moneymate-${LocalDate.now()}.csv") }
                else { pdfPending = buildPdf(state); pdfLauncher.launch("moneymate-${LocalDate.now()}.pdf") }
            }
            status?.let { Text(it, color = c.success, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp)) }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: ImportScreen
// Purpose: Renders the Import Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun ImportScreen(state: MoneyMateState, onBack: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedText by remember { mutableStateOf<String?>(null) }
    var selectedName by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            selectedText = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }.orEmpty()
            selectedName = uri.lastPathSegment?.substringAfterLast('/') ?: "transactions.csv"
        }
    }
    Column(Modifier.fillMaxSize().background(c.background)) {
        PageTitle("Import Data", onBack = onBack)
        Column(Modifier.padding(horizontal = 20.dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = c.surface,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, c.border)
            ) {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.UploadFile, null, tint = c.mutedText, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(10.dp))
                    Text("Select a CSV file exported from MoneyMate or another finance app.", color = c.secondaryText, fontSize = 13.sp, lineHeight = 18.sp)
                    Spacer(Modifier.height(14.dp))
                    OutlinedButton(onClick = { launcher.launch(arrayOf("text/csv", "text/plain", "application/csv")) }) { Text("Choose File") }
                    selectedName?.let { Text(it, color = c.mutedText, fontSize = 12.5.sp, modifier = Modifier.padding(top = 10.dp)) }
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { scope.launch { status = importCsv(state, selectedText.orEmpty()); state.loadAll(false) } },
                enabled = selectedText != null,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) { Text("Import", fontWeight = FontWeight.Bold) }
            status?.let { Text(it, color = c.secondaryText, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp)) }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: EditProfileScreen
// Purpose: Renders the Edit Profile Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun EditProfileScreen(state: MoneyMateState, onBack: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    val scope = rememberCoroutineScope()
    val u = state.user
    var name by remember(u) { mutableStateOf(u?.name.orEmpty()) }
    var email by remember(u) { mutableStateOf(u?.email.orEmpty()) }
    var status by remember { mutableStateOf<String?>(null) }
    val initials = name.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }.ifBlank { "MM" }

    Column(Modifier.fillMaxSize().background(c.background)) {
        PageTitle("Edit Profile", onBack = onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Box(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 24.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.size(64.dp).background(c.action, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) { Text(initials, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold) }
            }
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = false
            )
            Spacer(Modifier.height(16.dp))
            PrimaryButton("Save Changes") {
                scope.launch {
                    val current = state.user ?: return@launch
                    status = if (state.updateProfile(ProfileRequest(name.trim(), current.username, current.currency, current.language))) {
                        "Profile updated"
                    } else state.error
                }
            }
            status?.let { Text(it, color = c.secondaryText, fontSize = 12.sp, modifier = Modifier.padding(top = 10.dp)) }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: PlansScreen
// Purpose: Renders the Plans Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun PlansScreen(state:MoneyMateState,onBack:()->Unit){val c=LocalMoneyMateTokens.current;var selected by remember{mutableStateOf("Yearly")};Column(Modifier.fillMaxSize().background(c.background)){PageTitle("Choose Your Plan","Select the MoneyMate Premium plan that fits you best.",onBack);listOf("Monthly" to "Flexible monthly access","6 Months" to "Save 24%","Yearly" to "Best Value · Save 32%").forEach{(plan,sub)->MMCard(Modifier.padding(horizontal=20.dp,vertical=5.dp),onClick={selected=plan}){Row{Column(Modifier.weight(1f)){Text(plan,color=c.primaryText,fontWeight=FontWeight.Bold);Text(sub,color=c.secondaryText,fontSize=12.sp)};RadioButton(selected=selected==plan,onClick={selected=plan})}}};Spacer(Modifier.height(12.dp));Box(Modifier.padding(horizontal=20.dp)){PrimaryButton("Continue to Subscription"){state.message="Google Play Billing must be configured with your real subscription product IDs before store release."}}}}


// -----------------------------------------------------------------------------
// Section: ContactScreen
// Purpose: Renders the Contact Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun ContactScreen(onBack: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    var subject by remember { mutableStateOf("General") }
    var message by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }
    val subjects = listOf("General", "Billing", "Bug Report", "Feature Request")

    Column(Modifier.fillMaxSize().background(c.background)) {
        PageTitle("Contact Support", onBack = onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Text(
                "We usually respond within 24 hours. You can also email us directly at support@moneymate.app.",
                color = c.secondaryText,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text("Subject", color = c.primaryText, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                subjects.take(2).forEach { Pill(it, subject == it) { subject = it } }
            }
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                subjects.drop(2).forEach { Pill(it, subject == it) { subject = it } }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = message,
                onValueChange = { message = it; sent = false },
                label = { Text("Message") },
                placeholder = { Text("Describe your issue or question…") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp),
                minLines = 5
            )
            Spacer(Modifier.height(16.dp))
            PrimaryButton("Send Message") { if (message.isNotBlank()) sent = true }
            if (sent) Text("Message Sent", color = c.success, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp))
        }
    }
}


// -----------------------------------------------------------------------------
// Section: HelpCenterScreen
// Purpose: Renders the Help Center Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun HelpCenterScreen(onBack: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    val groups = listOf(
        "Getting Started" to listOf(
            "Setting up your first account" to "Go to Settings → My Accounts → Add Account to create your first account, then start logging transactions from the + button.",
            "Choosing Guest Mode vs an account" to "Guest Mode lets you try MoneyMate instantly with data stored only on this device. Creating an account unlocks account-backed features."
        ),
        "Managing Transactions" to listOf(
            "Adding an income or expense" to "Tap the + button, choose Income or Expense, fill in the amount and category, then save.",
            "Editing or deleting a transaction" to "Open any transaction from your history and use Edit or Delete.",
            "Searching and filtering transactions" to "Use the search bar on Activity, or the filter control for advanced filtering."
        ),
        "Budgets & Goals" to listOf(
            "Creating a monthly budget" to "Go to Settings → Monthly Budget → New Budget, pick a category and a limit.",
            "Setting a savings goal" to "Go to Settings → Savings Goals to create a target and log contributions."
        ),
        "Backup & Security" to listOf(
            "Backing up your data" to "Use Settings → Backup for a full backup, or Export Data for a portable file.",
            "Securing the app with PIN or biometrics" to "Enable PIN Lock and Biometric Authentication from Settings → Security."
        ),
        "Premium & Billing" to listOf(
            "What does Premium include?" to "Advanced Analytics, AI Insights, Backup, PDF Export, Premium themes and icons, PIN/Biometric Lock, Recurring Transactions, Advanced Filters, and priority support.",
            "How do I cancel my subscription?" to "Subscriptions are managed through your app store account and can be cancelled there."
        )
    )
    Column(Modifier.fillMaxSize().background(c.background)) {
        PageTitle("Help Center", onBack = onBack)
        LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 2.dp)) {
            item { Text("Browse help topics by category, or visit Contact Support for direct help.", color = c.secondaryText, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)); Spacer(Modifier.height(12.dp)) }
            groups.forEach { (title, entries) ->
                item { Text(title.uppercase(), color = c.mutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)) }
                item {
                    MMCard {
                        entries.forEachIndexed { index, item ->
                            FaqRow(item.first, item.second)
                            if (index != entries.lastIndex) HorizontalDivider(color = c.divider)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}


// -----------------------------------------------------------------------------
// Section: FaqRow
// Purpose: Encapsulates the Faq Row section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun FaqRow(question: String, answer: String) {
    val c = LocalMoneyMateTokens.current
    var open by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().clickable { open = !open }.padding(vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(question, color = c.primaryText, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, modifier = Modifier.weight(1f))
            Icon(if (open) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null, tint = c.mutedText)
        }
        if (open) Text(answer, color = c.secondaryText, fontSize = 12.5.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 8.dp))
    }
}


// -----------------------------------------------------------------------------
// Section: AboutScreen
// Purpose: Renders the About Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun AboutScreen(onBack: () -> Unit) {
    val c = LocalMoneyMateTokens.current
    Column(Modifier.fillMaxSize().background(c.background)) {
        PageTitle("About MoneyMate", onBack = onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(76.dp).background(c.brand, RoundedCornerShape(22.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.TrendingUp, null, tint = Color.White, modifier = Modifier.size(36.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text("MoneyMate", color = c.primaryText, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Text("Track Smarter. Spend Better. Grow Wealth.", color = c.secondaryText, fontSize = 12.5.sp)
            Spacer(Modifier.height(20.dp))
            MMCard(Modifier.fillMaxWidth()) {
                AboutInfoRow("App Name", "MoneyMate")
                HorizontalDivider(color = c.divider)
                AboutInfoRow("Version", "2.5.0")
                HorizontalDivider(color = c.divider)
                AboutInfoRow("Category", "Finance")
                HorizontalDivider(color = c.divider)
                AboutInfoRow("Platform", "Android")
                HorizontalDivider(color = c.divider)
                AboutInfoRow("Supported Languages", "English, বাংলা")
            }
            Spacer(Modifier.height(18.dp))
            Text("© 2026 MoneyMate. All Rights Reserved.", color = c.mutedText, fontSize = 11.sp)
        }
    }
}


// -----------------------------------------------------------------------------
// Section: AboutInfoRow
// Purpose: Encapsulates the About Info Row section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun AboutInfoRow(label: String, value: String) {
    val c = LocalMoneyMateTokens.current
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(label, color = c.secondaryText, fontSize = 12.5.sp, modifier = Modifier.weight(1f))
        Text(value, color = c.primaryText, fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
    }
}


// -----------------------------------------------------------------------------
// Section: StaticInfoScreen
// Purpose: Renders the Static Info Screen user interface and handles its local interactions.
// -----------------------------------------------------------------------------
@Composable
private fun StaticInfoScreen(title:String,text:String,onBack:()->Unit){val c=LocalMoneyMateTokens.current;Column(Modifier.fillMaxSize().background(c.background)){PageTitle(title,null,onBack);Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)){MMCard{Text(text,color=c.secondaryText,fontSize=14.sp,lineHeight=21.sp)}}}}


// -----------------------------------------------------------------------------
// Section: SettingSwitch
// Purpose: Encapsulates the Setting Switch section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun SettingSwitch(title:String,subtitle:String,checked:Boolean,onChange:(Boolean)->Unit){val c=LocalMoneyMateTokens.current;Row(Modifier.fillMaxWidth().padding(vertical=10.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(title,color=c.primaryText,fontWeight=FontWeight.Bold);Text(subtitle,color=c.secondaryText,fontSize=11.sp)};Switch(checked=checked,onCheckedChange=onChange)}}

// -----------------------------------------------------------------------------
// Section: MenuInline
// Purpose: Encapsulates the Menu Inline section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun MenuInline(title:String,subtitle:String,onClick:()->Unit){val c=LocalMoneyMateTokens.current;Row(Modifier.fillMaxWidth().clickable(onClick=onClick).padding(vertical=12.dp)){Column(Modifier.weight(1f)){Text(title,color=c.primaryText,fontWeight=FontWeight.Bold);Text(subtitle,color=c.secondaryText,fontSize=11.sp)};Text("›",color=c.mutedText,fontSize=22.sp)}}


// -----------------------------------------------------------------------------
// Section: buildCsv
// Purpose: Encapsulates the build Csv section of this file.
// -----------------------------------------------------------------------------
@Composable
private fun <T> PickerFieldPublic(label:String,items:List<T>,selected:T?,itemLabel:(T)->String,onSelect:(T)->Unit){var expanded by remember{mutableStateOf(false)};Box(Modifier.fillMaxWidth()){OutlinedButton(onClick={expanded=true},modifier=Modifier.fillMaxWidth()){Text(if(selected==null)label else itemLabel(selected))};DropdownMenu(expanded=expanded,onDismissRequest={expanded=false}){items.forEach{item->DropdownMenuItem(text={Text(itemLabel(item))},onClick={onSelect(item);expanded=false})}}}}

private fun buildCsv(state:MoneyMateState):String{val out=StringBuilder("date,type,amount,merchant,paymentMethod,notes,accountName,categoryName\n");state.transactions.forEach{t->val a=state.accounts.firstOrNull{it.id==t.accountId}?.name.orEmpty();val c=state.categories.firstOrNull{it.id==t.categoryId}?.name.orEmpty();out.append(listOf(t.occurredAt,t.type,t.amount,t.merchant.orEmpty(),t.paymentMethod.orEmpty(),t.notes.orEmpty(),a,c).joinToString(","){csvEscape(it)}).append('\n')};return out.toString()}

// -----------------------------------------------------------------------------
// Section: csvEscape
// Purpose: Encapsulates the csv Escape section of this file.
// -----------------------------------------------------------------------------
private fun csvEscape(v:String):String="\""+v.replace("\"","\"\"")+"\""

// -----------------------------------------------------------------------------
// Section: parseCsvLine
// Purpose: Encapsulates the parse Csv Line section of this file.
// -----------------------------------------------------------------------------
private fun parseCsvLine(line:String):List<String>{val out=mutableListOf<String>();val cur=StringBuilder();var quoted=false;var i=0;while(i<line.length){val ch=line[i];when{ch=='\"'&&quoted&&i+1<line.length&&line[i+1]=='\"'->{cur.append('\"');i++};ch=='\"'->quoted=!quoted;ch==','&&!quoted->{out+=cur.toString();cur.clear()};else->cur.append(ch)};i++};out+=cur.toString();return out}

// -----------------------------------------------------------------------------
// Section: importCsv
// Purpose: Encapsulates the import Csv section of this file.
// -----------------------------------------------------------------------------
private suspend fun importCsv(
    state: MoneyMateState,
    text: String
): String {
    val lines = text
        .lineSequence()
        .filter { it.isNotBlank() }
        .toList()

    if (lines.size < 2) {
        return "No importable rows found."
    }

    var ok = 0
    var fail = 0

    for (line in lines.drop(1)) {
        val cols = parseCsvLine(line)

        if (cols.size < 8) {
            fail++
            continue
        }

        val account = state.accounts.firstOrNull {
            it.name.equals(cols[6], true)
        } ?: state.accounts.firstOrNull()

        val category = state.categories.firstOrNull {
            it.name.equals(cols[7], true)
        }

        val amount = cols[2].toDoubleOrNull()

        if (account == null || amount == null) {
            fail++
            continue
        }

        val occurred = cols[0]
            .takeIf { it.contains('T') }
            ?: runCatching {
                LocalDate
                    .parse(cols[0].take(10))
                    .atStartOfDay()
                    .toInstant(ZoneOffset.UTC)
                    .toString()
            }.getOrDefault(
                java.time.Instant.now().toString()
            )

        when (
            state.repository.createTransaction(
                TransactionRequest(
                    account.id,
                    category?.id,
                    cols[1].ifBlank { "expense" },
                    amount,
                    cols[3].ifBlank { null },
                    cols[4].ifBlank { null },
                    cols[5].ifBlank { null },
                    occurredAt = occurred
                )
            )
        ) {
            is RepoResult.Success -> ok++
            is RepoResult.Error -> fail++
        }
    }

    return "Imported $ok transaction(s)${
        if (fail > 0) " · $fail skipped" else ""
    }."
}

// -----------------------------------------------------------------------------
// Section: buildPdf
// Purpose: Encapsulates the build Pdf section of this file.
// -----------------------------------------------------------------------------
private fun buildPdf(state:MoneyMateState):ByteArray{val doc=PdfDocument();val paint=Paint().apply{color=android.graphics.Color.rgb(16,24,40);textSize=12f};val pageInfo=PdfDocument.PageInfo.Builder(595,842,1).create();var page=doc.startPage(pageInfo);val canvas=page.canvas;paint.textSize=20f;paint.isFakeBoldText=true;canvas.drawText("MoneyMate Financial Statement",36f,50f,paint);paint.textSize=11f;paint.isFakeBoldText=false;var y=82f;canvas.drawText("Generated ${LocalDate.now()} · Currency ${state.currency}",36f,y,paint);y+=26f;state.transactions.take(45).forEach{t->val line="${t.occurredAt.take(10)}  ${t.type.uppercase()}  ${t.merchant?:"Transaction"}  ${money(t.amount,state.currency)}";canvas.drawText(line.take(86),36f,y,paint);y+=16f};doc.finishPage(page);val stream=java.io.ByteArrayOutputStream();doc.writeTo(stream);doc.close();return stream.toByteArray()}


// -----------------------------------------------------------------------------
// Section: helpText
// Purpose: Encapsulates the help Text section of this file.
// -----------------------------------------------------------------------------
private val helpText="""MoneyMate helps you track income and expenses, manage accounts, build budgets, monitor savings goals, manage bills, review analytics and keep portable backups. Use the + action from the main navigation to add a transaction. Financial Tools in Profile contains the full feature set."""

// -----------------------------------------------------------------------------
// Section: faqText
// Purpose: Encapsulates the faq Text section of this file.
// -----------------------------------------------------------------------------
private val faqText="""Do I need an account?\nYes. A MoneyMate account securely connects your financial data to the configured backend.\n\nWhat is included in Premium?\nPremium includes advanced analytics, AI insights, backup/restore, export, security controls, Premium Themes, recurring transactions and advanced filters.\n\nCan I restore my backup?\nYes. Use Profile → Restore and validate the JSON file before confirming.\n\nCan I change language and currency?\nYes, from Profile → General."""

// -----------------------------------------------------------------------------
// Section: privacyText
// Purpose: Encapsulates the privacy Text section of this file.
// -----------------------------------------------------------------------------
private val privacyText="""MoneyMate is designed to keep financial data associated with your authenticated account. Authentication secrets are not included in backup exports. The Android app stores session and preference data in app-private storage. Production deployments should use HTTPS, managed database backups, secure email credentials and an appropriate privacy policy for the operator's jurisdiction."""

// -----------------------------------------------------------------------------
// Section: termsText
// Purpose: Encapsulates the terms Text section of this file.
// -----------------------------------------------------------------------------
private val termsText="""MoneyMate provides personal-finance tracking tools. Information and computed insights are informational only and are not financial, tax, legal or investment advice. Users are responsible for reviewing transaction accuracy and maintaining secure account credentials."""

// -----------------------------------------------------------------------------
// Section: licensesText
// Purpose: Encapsulates the licenses Text section of this file.
// -----------------------------------------------------------------------------
private val licensesText="""MoneyMate uses AndroidX / Jetpack Compose, Retrofit, OkHttp, Gson, Express, PostgreSQL tooling, Prisma, Zod, bcryptjs, jsonwebtoken, Nodemailer, node-cron and their transitive dependencies. Review the package lockfiles and dependency metadata for exact versions and upstream licenses before store release."""

// -----------------------------------------------------------------------------
// Section: aboutText
// Purpose: Encapsulates the about Text section of this file.
// -----------------------------------------------------------------------------
private val aboutText="""MoneyMate helps you track spending, manage budgets and reach savings goals with a calm, clear view of your money — all in one app. This native Android build uses Jetpack Compose and connects to the MoneyMate REST API for synchronized financial data."""
