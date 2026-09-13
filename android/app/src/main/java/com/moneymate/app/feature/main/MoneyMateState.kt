package com.moneymate.app.feature.main

// =============================================================================
// File: MoneyMateState.kt
// Purpose: Main application state holder that coordinates repository data, preferences, loading, errors, and feature actions.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.moneymate.app.data.local.AppPreferences
import com.moneymate.app.data.local.RecurringTemplate
import com.moneymate.app.core.common.CategoryCatalog
import com.moneymate.app.data.model.*
import com.moneymate.app.data.repository.MoneyMateRepository
import com.moneymate.app.data.repository.RepoResult
import java.time.LocalDate
import java.time.ZoneOffset


// -----------------------------------------------------------------------------
// Section: MoneyMateState
// Purpose: Formatting/helper logic for Money Mate State.
// -----------------------------------------------------------------------------
class MoneyMateState(context: Context, val guestMode: Boolean = false) {
    private val appContext = context.applicationContext
    val repository = MoneyMateRepository(appContext)
    val prefs = AppPreferences(appContext)

    var user by mutableStateOf<User?>(null)
    var accounts by mutableStateOf<List<Account>>(emptyList())
    var categories by mutableStateOf<List<Category>>(emptyList())
    var transactions by mutableStateOf<List<Transaction>>(emptyList())
    var budgets by mutableStateOf<List<Budget>>(emptyList())
    var savingsGoals by mutableStateOf<List<SavingsGoal>>(emptyList())
    var bills by mutableStateOf<List<Bill>>(emptyList())
    var notifications by mutableStateOf<List<NotificationItem>>(emptyList())
    var dashboard by mutableStateOf(DashboardData())
    var monthly by mutableStateOf(MonthlyData())
    var categoryBreakdown by mutableStateOf(CategoryBreakdownData())
    var budgetProgress by mutableStateOf(BudgetProgressData())

    var loading by mutableStateOf(false)
    var refreshing by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var message by mutableStateOf<String?>(null)
    var lockRequested by mutableStateOf(false)

    var currentCurrency by mutableStateOf(prefs.currency)
        private set
    var currentLanguage by mutableStateOf(prefs.language)
        private set
    var selectedPremiumTheme by mutableStateOf(prefs.selectedPremiumTheme)
        private set
    var selectedAppIcon by mutableStateOf(prefs.selectedAppIcon)
        private set

    val currency: String get() = currentCurrency
    val language: String get() = currentLanguage
    val unreadCount: Int get() = notifications.count { !it.isRead }

    suspend fun loadAll(showLoader: Boolean = true) {
        if (guestMode) {
            seedGuestData()
            loading = false
            refreshing = false
            return
        }
        if (showLoader) loading = true else refreshing = true
        error = null
        try {
            processRecurringTransactions()
            user = (repository.profile() as? RepoResult.Success)?.data?.user ?: user
            accounts = (repository.accounts() as? RepoResult.Success)?.data?.accounts ?: accounts
            categories = (repository.categories() as? RepoResult.Success)?.data?.categories ?: categories
            transactions = (repository.transactions() as? RepoResult.Success)?.data?.transactions ?: transactions
            budgets = (repository.budgets() as? RepoResult.Success)?.data?.budgets ?: budgets
            savingsGoals = (repository.savingsGoals() as? RepoResult.Success)?.data?.savingsGoals ?: savingsGoals
            bills = (repository.bills() as? RepoResult.Success)?.data?.bills ?: bills
            notifications = (repository.notifications() as? RepoResult.Success)?.data?.notifications ?: notifications
            dashboard = (repository.dashboard() as? RepoResult.Success)?.data ?: dashboard
            val now = LocalDate.now()
            monthly = (repository.monthly(now.monthValue, now.year) as? RepoResult.Success)?.data ?: monthly
            categoryBreakdown = (repository.categoryBreakdown(now.monthValue, now.year) as? RepoResult.Success)?.data ?: categoryBreakdown
            budgetProgress = (repository.budgetProgress(now.monthValue, now.year) as? RepoResult.Success)?.data ?: budgetProgress
            user?.let {
                currentCurrency = it.currency
                currentLanguage = it.language
                prefs.currency = it.currency
                prefs.language = it.language
            }
        } catch (e: Exception) {
            error = e.message ?: "Unable to refresh MoneyMate"
        } finally {
            loading = false
            refreshing = false
        }
    }

    suspend fun refreshCore() = loadAll(false)

    suspend fun setLanguage(code: String): Boolean {
        if (code !in setOf("en", "bn")) return false
        if (!guestMode) {
            val u = user ?: return false
            val result = repository.updateProfile(ProfileRequest(u.name ?: "MoneyMate User", u.username, u.currency, code))
            if (result is RepoResult.Error) { error = result.message; return false }
            user = u.copy(language = code)
        } else {
            user = user?.copy(language = code)
        }
        currentLanguage = code
        prefs.language = code
        return true
    }

    suspend fun setCurrency(code: String): Boolean {
        if (code !in setOf("USD", "BDT", "MYR", "EUR", "GBP", "SGD", "INR")) return false
        if (!guestMode) {
            val u = user ?: return false
            val result = repository.updateProfile(ProfileRequest(u.name ?: "MoneyMate User", u.username, code, u.language))
            if (result is RepoResult.Error) { error = result.message; return false }
            user = u.copy(currency = code)
        } else {
            user = user?.copy(currency = code)
            accounts = accounts.map { it.copy(currency = code) }
        }
        currentCurrency = code
        prefs.currency = code
        return true
    }

    fun setPremiumTheme(name: String) {
        if (com.moneymate.app.ui.theme.PremiumThemes.none { it.name == name }) return
        selectedPremiumTheme = name
        prefs.selectedPremiumTheme = name
        com.moneymate.app.ui.theme.MoneyMateThemeRuntime.premiumThemeName = name
    }

    fun setAppIcon(id: String): Boolean {
        return runCatching {
            com.moneymate.app.core.common.LauncherIconManager.apply(appContext, id)
            selectedAppIcon = id
            prefs.selectedAppIcon = id
            true
        }.getOrElse {
            error = it.message ?: "Unable to change app icon"
            false
        }
    }

    suspend fun createTransaction(body: TransactionRequest): Boolean = mutate(repository.createTransaction(body))
    suspend fun updateTransaction(id: Int, body: TransactionRequest): Boolean = mutate(repository.updateTransaction(id, body))
    suspend fun deleteTransaction(id: Int): Boolean = mutate(repository.deleteTransaction(id))
    suspend fun transfer(body: TransferRequest): Boolean = mutate(repository.transfer(body))

    suspend fun createAccount(body: AccountRequest): Boolean = mutate(repository.createAccount(body))
    suspend fun updateAccount(id: Int, body: AccountRequest): Boolean = mutate(repository.updateAccount(id, body))
    suspend fun deleteAccount(id: Int): Boolean = mutate(repository.deleteAccount(id))

    suspend fun createBudget(body: BudgetRequest): Boolean = mutate(repository.createBudget(body))
    suspend fun updateBudget(id: Int, body: BudgetRequest): Boolean = mutate(repository.updateBudget(id, body))
    suspend fun deleteBudget(id: Int): Boolean = mutate(repository.deleteBudget(id))

    suspend fun createGoal(body: SavingsGoalRequest): Boolean = mutate(repository.createSavingsGoal(body))
    suspend fun updateGoal(id: Int, body: SavingsGoalRequest): Boolean = mutate(repository.updateSavingsGoal(id, body))
    suspend fun contributeGoal(id: Int, amount: Double): Boolean = mutate(repository.contributeSavings(id, amount))
    suspend fun deleteGoal(id: Int): Boolean = mutate(repository.deleteSavingsGoal(id))

    suspend fun createBill(body: BillRequest): Boolean = mutate(repository.createBill(body))
    suspend fun updateBill(id: Int, body: BillRequest): Boolean = mutate(repository.updateBill(id, body))
    suspend fun deleteBill(id: Int): Boolean = mutate(repository.deleteBill(id))

    suspend fun updateProfile(body: ProfileRequest): Boolean = mutate(repository.updateProfile(body))
    suspend fun changePassword(current: String, next: String): Boolean = mutate(repository.changePassword(current, next), refresh = false)
    suspend fun deleteUserAccount(): Boolean {
        if (guestMode) {
            error = "Guest Mode has no account to delete."
            return false
        }
        return when (val result = repository.deleteUserAccount()) {
            is RepoResult.Success -> true
            is RepoResult.Error -> { error = result.message; false }
        }
    }

    suspend fun markNotificationRead(id: Int): Boolean = mutate(repository.markNotificationRead(id))
    suspend fun markAllRead(): Boolean = mutate(repository.markAllRead())
    suspend fun deleteNotification(id: Int): Boolean = mutate(repository.deleteNotification(id))

    fun recurring(): List<RecurringTemplate> = prefs.recurring()
    fun saveRecurring(items: List<RecurringTemplate>) = prefs.saveRecurring(items)

    private suspend fun mutate(result: RepoResult<*>, refresh: Boolean = true): Boolean {
        if (guestMode) {
            error = "This action requires a signed-in MoneyMate account."
            return false
        }
        return when (result) {
            is RepoResult.Success -> {
                message = result.message.ifBlank { "Saved" }
                if (refresh) loadAll(false)
                true
            }
            is RepoResult.Error -> {
                error = result.message
                false
            }
        }
    }

    private fun seedGuestData() {
        val now = LocalDate.now()
        user = User(id = -1, name = "Alex Morgan", email = "local@moneymate.invalid", currency = prefs.currency, language = prefs.language)
        accounts = listOf(
            Account(1, "Cash Wallet", "cash", "2427.85", currency),
            Account(2, "Main Checking", "bank", "11200", currency),
            Account(3, "High-Yield Savings", "savings", "10500", currency)
        )
        categories = CategoryCatalog.guestCategories()
        transactions = listOf(
            Transaction(1,2,1,"income","6240","Salary","Bank Transfer",occurredAt=now.withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC).toString()),
            Transaction(2,2,9,"expense","68.40","Bistro","Card",occurredAt=now.minusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toString()),
            Transaction(3,2,12,"expense","42.00","Transit Pass","Card",occurredAt=now.minusDays(2).atStartOfDay().toInstant(ZoneOffset.UTC).toString()),
            Transaction(4,2,11,"expense","129.99","Home Store","Card",occurredAt=now.minusDays(4).atStartOfDay().toInstant(ZoneOffset.UTC).toString())
        )
        budgets = listOf(Budget(1,9,"600",now.monthValue,now.year), Budget(2,12,"300",now.monthValue,now.year))
        savingsGoals = listOf(SavingsGoal(1,"Emergency Fund","15000","10500",now.plusMonths(8).atStartOfDay().toInstant(ZoneOffset.UTC).toString()))
        bills = listOf(Bill(1,"Internet Bill","80",now.plusDays(5).atStartOfDay().toInstant(ZoneOffset.UTC).toString(),"upcoming"))
        notifications = listOf(NotificationItem(1,"general","Local session","Sign in to securely sync your MoneyMate data across devices.",false,createdAt=java.time.Instant.now().toString()))
        dashboard = DashboardData(DashboardSummary(24127.85,6240.0,2981.40,10500.0,80.0),DashboardCounts(3,4,1,1))
        monthly = MonthlyData(now.monthValue,now.year,MonthlySummary(6240.0,2981.40,3258.60),4)
        categoryBreakdown = CategoryBreakdownData(now.monthValue,now.year,240.39,listOf(CategoryBreakdown(9,"Food & Dining","restaurant",68.40,1,28.45),CategoryBreakdown(11,"Shopping","shopping_bag",129.99,1,54.07),CategoryBreakdown(12,"Transport","directions_car",42.0,1,17.47)))
        budgetProgress = BudgetProgressData(now.monthValue,now.year,900.0,110.4,789.6,listOf(BudgetProgress(1,9,"Food & Dining","restaurant",600.0,68.4,531.6,11.4,"safe"),BudgetProgress(2,12,"Transport","directions_car",300.0,42.0,258.0,14.0,"safe")))
    }

    private suspend fun processRecurringTransactions() {
        val current = prefs.recurring().toMutableList()
        if (current.isEmpty()) return
        var changed = false
        val today = LocalDate.now()
        for (index in current.indices) {
            val item = current[index]
            if (!item.enabled) continue
            val next = runCatching { LocalDate.parse(item.nextDate) }.getOrNull() ?: continue
            if (next.isAfter(today)) continue
            val occurredAt = next.atStartOfDay().toInstant(ZoneOffset.UTC).toString()
            val result = repository.createTransaction(
                TransactionRequest(
                    accountId = item.accountId,
                    categoryId = item.categoryId,
                    type = item.type,
                    amount = item.amount,
                    merchant = item.title,
                    paymentMethod = "Recurring",
                    notes = "Recurring ${item.frequency} transaction",
                    occurredAt = occurredAt
                )
            )
            if (result is RepoResult.Success) {
                val advanced = when (item.frequency) {
                    "weekly" -> next.plusWeeks(1)
                    "yearly" -> next.plusYears(1)
                    else -> next.plusMonths(1)
                }
                current[index] = item.copy(nextDate = advanced.toString())
                changed = true
            }
        }
        if (changed) prefs.saveRecurring(current)
    }
}


// -----------------------------------------------------------------------------
// Section: MainTab
// Purpose: Encapsulates the Main Tab section of this file.
// -----------------------------------------------------------------------------
enum class MainTab { HOME, ACTIVITY, ANALYTICS, PROFILE }


// -----------------------------------------------------------------------------
// Section: ToolPage
// Purpose: Encapsulates the Tool Page section of this file.
// -----------------------------------------------------------------------------
enum class ToolPage {
    NONE, ACCOUNTS, BUDGETS, GOALS, BILLS, CALENDAR, NOTIFICATIONS,
    ADVANCED_ANALYTICS, AI_INSIGHTS, RECURRING, ADVANCED_FILTERS,
    APPEARANCE, LANGUAGE, CURRENCY, ACCESSIBILITY, PREMIUM_THEMES,
    PREMIUM_ICONS, SECURITY, BACKUP, RESTORE, EXPORT, IMPORT,
    HELP, FAQ, CONTACT, PRIVACY, TERMS, LICENSES, ABOUT, EDIT_PROFILE,
    PLANS, QUICK_PROFILE, ANALYTICS_FULL
}
