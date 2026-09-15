package com.moneymate.app.data.model

// =============================================================================
// File: ApiModels.kt
// Purpose: Network request/response and domain data models used by the Android client.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import com.google.gson.annotations.SerializedName

// Generic API envelope used by the MoneyMate Express backend.

// -----------------------------------------------------------------------------
// Section: ApiResponse
// Purpose: Encapsulates the Api Response section of this file.
// -----------------------------------------------------------------------------
data class ApiResponse<T>(
    val success: Boolean = false,
    val message: String = "",
    val code: String? = null,
    val data: T? = null,
    val errors: List<ApiError>? = null
)


// -----------------------------------------------------------------------------
// Section: ApiError
// Purpose: Encapsulates the Api Error section of this file.
// -----------------------------------------------------------------------------
data class ApiError(
    val field: String? = null,
    val message: String? = null
)


// -----------------------------------------------------------------------------
// Section: User
// Purpose: Encapsulates the User section of this file.
// -----------------------------------------------------------------------------
data class User(
    val id: Int = 0,
    val name: String? = null,
    val email: String = "",
    val username: String? = null,
    val profileImageUrl: String? = null,
    val currency: String = "USD",
    val language: String = "en",
    val createdAt: String? = null,
    val updatedAt: String? = null
)


// -----------------------------------------------------------------------------
// Section: AuthData
// Purpose: Encapsulates the Auth Data section of this file.
// -----------------------------------------------------------------------------
data class AuthData(val token: String? = null, val user: User? = null)

// -----------------------------------------------------------------------------
// Section: UserData
// Purpose: Encapsulates the User Data section of this file.
// -----------------------------------------------------------------------------
data class UserData(val user: User? = null)

// -----------------------------------------------------------------------------
// Section: ResetData
// Purpose: Encapsulates the Reset Data section of this file.
// -----------------------------------------------------------------------------
data class ResetData(
    val resetToken: String? = null,
    val expiresAt: String? = null,
    val valid: Boolean? = null,
    val cooldownSeconds: Int? = null,
    val retryAfterSeconds: Int? = null
)


// -----------------------------------------------------------------------------
// Section: Account
// Purpose: Encapsulates the Account section of this file.
// -----------------------------------------------------------------------------
data class Account(
    val id: Int = 0,
    val name: String = "",
    val type: String = "",
    val balance: String = "0",
    val currency: String = "USD",
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// -----------------------------------------------------------------------------
// Section: AccountsData
// Purpose: Encapsulates the Accounts Data section of this file.
// -----------------------------------------------------------------------------
data class AccountsData(val accounts: List<Account> = emptyList())

// -----------------------------------------------------------------------------
// Section: AccountData
// Purpose: Encapsulates the Account Data section of this file.
// -----------------------------------------------------------------------------
data class AccountData(val account: Account? = null)


// -----------------------------------------------------------------------------
// Section: Category
// Purpose: Encapsulates the Category section of this file.
// -----------------------------------------------------------------------------
data class Category(
    val id: Int = 0,
    val name: String = "",
    val type: String = "",
    val icon: String? = null,
    val createdAt: String? = null
)

// -----------------------------------------------------------------------------
// Section: CategoriesData
// Purpose: Encapsulates the Categories Data section of this file.
// -----------------------------------------------------------------------------
data class CategoriesData(val categories: List<Category> = emptyList())

// -----------------------------------------------------------------------------
// Section: CategoryData
// Purpose: Encapsulates the Category Data section of this file.
// -----------------------------------------------------------------------------
data class CategoryData(val category: Category? = null)


// -----------------------------------------------------------------------------
// Section: Transaction
// Purpose: Encapsulates the Transaction section of this file.
// -----------------------------------------------------------------------------
data class Transaction(
    val id: Int = 0,
    val accountId: Int = 0,
    val categoryId: Int? = null,
    val type: String = "expense",
    val amount: String = "0",
    val merchant: String? = null,
    val paymentMethod: String? = null,
    val notes: String? = null,
    val receiptUrl: String? = null,
    val occurredAt: String = "",
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// -----------------------------------------------------------------------------
// Section: TransactionsData
// Purpose: Encapsulates the Transactions Data section of this file.
// -----------------------------------------------------------------------------
data class TransactionsData(val transactions: List<Transaction> = emptyList(), val count: Int? = null)

// -----------------------------------------------------------------------------
// Section: TransactionData
// Purpose: Encapsulates the Transaction Data section of this file.
// -----------------------------------------------------------------------------
data class TransactionData(val transaction: Transaction? = null)


// -----------------------------------------------------------------------------
// Section: Budget
// Purpose: Encapsulates the Budget section of this file.
// -----------------------------------------------------------------------------
data class Budget(
    val id: Int = 0,
    val categoryId: Int = 0,
    val amount: String = "0",
    val month: Int = 1,
    val year: Int = 2026,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// -----------------------------------------------------------------------------
// Section: BudgetsData
// Purpose: Encapsulates the Budgets Data section of this file.
// -----------------------------------------------------------------------------
data class BudgetsData(val budgets: List<Budget> = emptyList())

// -----------------------------------------------------------------------------
// Section: BudgetData
// Purpose: Encapsulates the Budget Data section of this file.
// -----------------------------------------------------------------------------
data class BudgetData(val budget: Budget? = null)


// -----------------------------------------------------------------------------
// Section: SavingsGoal
// Purpose: Encapsulates the Savings Goal section of this file.
// -----------------------------------------------------------------------------
data class SavingsGoal(
    val id: Int = 0,
    val name: String = "",
    val targetAmount: String = "0",
    val savedAmount: String = "0",
    val targetDate: String? = null,
    val progressPercentage: Double? = null,
    val remainingAmount: Double? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// -----------------------------------------------------------------------------
// Section: SavingsGoalsData
// Purpose: Encapsulates the Savings Goals Data section of this file.
// -----------------------------------------------------------------------------
data class SavingsGoalsData(val savingsGoals: List<SavingsGoal> = emptyList())

// -----------------------------------------------------------------------------
// Section: SavingsGoalData
// Purpose: Encapsulates the Savings Goal Data section of this file.
// -----------------------------------------------------------------------------
data class SavingsGoalData(val savingsGoal: SavingsGoal? = null, val goal: SavingsGoal? = null)


// -----------------------------------------------------------------------------
// Section: Bill
// Purpose: Encapsulates the Bill section of this file.
// -----------------------------------------------------------------------------
data class Bill(
    val id: Int = 0,
    val name: String = "",
    val amount: String = "0",
    val dueDate: String = "",
    val status: String = "upcoming",
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// -----------------------------------------------------------------------------
// Section: BillsData
// Purpose: Encapsulates the Bills Data section of this file.
// -----------------------------------------------------------------------------
data class BillsData(val bills: List<Bill> = emptyList(), val count: Int? = null)

// -----------------------------------------------------------------------------
// Section: BillData
// Purpose: Encapsulates the Bill Data section of this file.
// -----------------------------------------------------------------------------
data class BillData(val bill: Bill? = null)


// -----------------------------------------------------------------------------
// Section: NotificationItem
// Purpose: Encapsulates the Notification Item section of this file.
// -----------------------------------------------------------------------------
data class NotificationItem(
    val id: Int = 0,
    val type: String = "general",
    val title: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    val relatedEntityType: String? = null,
    val relatedEntityId: Int? = null,
    val readAt: String? = null,
    val createdAt: String? = null
)

// -----------------------------------------------------------------------------
// Section: NotificationsData
// Purpose: Encapsulates the Notifications Data section of this file.
// -----------------------------------------------------------------------------
data class NotificationsData(
    val notifications: List<NotificationItem> = emptyList(),
    val count: Int = 0,
    val unreadCount: Int = 0,
    val updatedCount: Int? = null,
    val notification: NotificationItem? = null
)


// -----------------------------------------------------------------------------
// Section: DashboardSummary
// Purpose: Encapsulates the Dashboard Summary section of this file.
// -----------------------------------------------------------------------------
data class DashboardSummary(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalSavings: Double = 0.0,
    val upcomingBillsTotal: Double = 0.0
)

// -----------------------------------------------------------------------------
// Section: DashboardCounts
// Purpose: Encapsulates the Dashboard Counts section of this file.
// -----------------------------------------------------------------------------
data class DashboardCounts(
    val accounts: Int = 0,
    val transactions: Int = 0,
    val savingsGoals: Int = 0,
    val upcomingBills: Int = 0
)

// -----------------------------------------------------------------------------
// Section: DashboardData
// Purpose: Encapsulates the Dashboard Data section of this file.
// -----------------------------------------------------------------------------
data class DashboardData(val summary: DashboardSummary = DashboardSummary(), val counts: DashboardCounts = DashboardCounts())


// -----------------------------------------------------------------------------
// Section: MonthlySummary
// Purpose: Encapsulates the Monthly Summary section of this file.
// -----------------------------------------------------------------------------
data class MonthlySummary(val totalIncome: Double = 0.0, val totalExpense: Double = 0.0, val netAmount: Double = 0.0)

// -----------------------------------------------------------------------------
// Section: MonthlyData
// Purpose: Encapsulates the Monthly Data section of this file.
// -----------------------------------------------------------------------------
data class MonthlyData(val month: Int = 1, val year: Int = 2026, val summary: MonthlySummary = MonthlySummary(), val transactionCount: Int = 0)


// -----------------------------------------------------------------------------
// Section: CategoryBreakdown
// Purpose: Encapsulates the Category Breakdown section of this file.
// -----------------------------------------------------------------------------
data class CategoryBreakdown(
    val categoryId: Int? = null,
    val name: String = "Uncategorized",
    val icon: String? = null,
    val amount: Double = 0.0,
    val transactionCount: Int = 0,
    val percentage: Double = 0.0
)

// -----------------------------------------------------------------------------
// Section: CategoryBreakdownData
// Purpose: Encapsulates the Category Breakdown Data section of this file.
// -----------------------------------------------------------------------------
data class CategoryBreakdownData(val month: Int = 1, val year: Int = 2026, val totalExpense: Double = 0.0, val categories: List<CategoryBreakdown> = emptyList())


// -----------------------------------------------------------------------------
// Section: BudgetProgress
// Purpose: Encapsulates the Budget Progress section of this file.
// -----------------------------------------------------------------------------
data class BudgetProgress(
    val budgetId: Int = 0,
    val categoryId: Int = 0,
    val categoryName: String = "",
    val categoryIcon: String? = null,
    val budgetAmount: Double = 0.0,
    val spent: Double = 0.0,
    val remaining: Double = 0.0,
    val percentage: Double = 0.0,
    val status: String = "safe"
)

// -----------------------------------------------------------------------------
// Section: BudgetProgressData
// Purpose: Encapsulates the Budget Progress Data section of this file.
// -----------------------------------------------------------------------------
data class BudgetProgressData(
    val month: Int = 1,
    val year: Int = 2026,
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val totalRemaining: Double = 0.0,
    val budgets: List<BudgetProgress> = emptyList()
)


// -----------------------------------------------------------------------------
// Section: TransferResult
// Purpose: Encapsulates the Transfer Result section of this file.
// -----------------------------------------------------------------------------
data class TransferResult(
    val fromAccount: TransferAccount? = null,
    val toAccount: TransferAccount? = null,
    val outgoingTransactionId: Int? = null,
    val incomingTransactionId: Int? = null
)

// -----------------------------------------------------------------------------
// Section: TransferAccount
// Purpose: Encapsulates the Transfer Account section of this file.
// -----------------------------------------------------------------------------
data class TransferAccount(val id: Int = 0, val name: String = "", val balance: Double = 0.0)

// -----------------------------------------------------------------------------
// Section: TransferData
// Purpose: Encapsulates the Transfer Data section of this file.
// -----------------------------------------------------------------------------
data class TransferData(val transfer: TransferResult? = null)


// -----------------------------------------------------------------------------
// Section: RestoreCounts
// Purpose: Encapsulates the Restore Counts section of this file.
// -----------------------------------------------------------------------------
data class RestoreCounts(
    val accounts: Int = 0,
    val categories: Int = 0,
    val transactions: Int = 0,
    val budgets: Int = 0,
    val savingsGoals: Int = 0,
    val bills: Int = 0,
    val notifications: Int = 0
)

// -----------------------------------------------------------------------------
// Section: RestoreBackupInfo
// Purpose: Encapsulates the Restore Backup Info section of this file.
// -----------------------------------------------------------------------------
data class RestoreBackupInfo(val version: Int = 1, val exportedAt: String? = null)

// -----------------------------------------------------------------------------
// Section: RestoreData
// Purpose: Encapsulates the Restore Data section of this file.
// -----------------------------------------------------------------------------
data class RestoreData(val restored: RestoreCounts? = null, val backup: RestoreBackupInfo? = null)


// -----------------------------------------------------------------------------
// Section: RegisterRequest
// Purpose: Encapsulates the Register Request section of this file.
// -----------------------------------------------------------------------------
data class RegisterRequest(val name: String, val email: String, val password: String)

// -----------------------------------------------------------------------------
// Section: LoginRequest
// Purpose: Encapsulates the Login Request section of this file.
// -----------------------------------------------------------------------------
data class LoginRequest(val email: String, val password: String)

// -----------------------------------------------------------------------------
// Section: ForgotPasswordRequest
// Purpose: Encapsulates the Forgot Password Request section of this file.
// -----------------------------------------------------------------------------
data class ForgotPasswordRequest(val email: String)

// -----------------------------------------------------------------------------
// Section: VerifyResetRequest
// Purpose: Encapsulates the Verify Reset Request section of this file.
// -----------------------------------------------------------------------------
data class VerifyResetRequest(val token: String)

// -----------------------------------------------------------------------------
// Section: ResetPasswordRequest
// Purpose: Encapsulates the Reset Password Request section of this file.
// -----------------------------------------------------------------------------
data class ResetPasswordRequest(val token: String, val newPassword: String)


// -----------------------------------------------------------------------------
// Section: AccountRequest
// Purpose: Encapsulates the Account Request section of this file.
// -----------------------------------------------------------------------------
data class AccountRequest(val name: String, val type: String, val balance: Double, val currency: String)

// -----------------------------------------------------------------------------
// Section: CategoryRequest
// Purpose: Encapsulates the Category Request section of this file.
// -----------------------------------------------------------------------------
data class CategoryRequest(val name: String, val type: String, val icon: String? = null)

// -----------------------------------------------------------------------------
// Section: TransactionRequest
// Purpose: Encapsulates the Transaction Request section of this file.
// -----------------------------------------------------------------------------
data class TransactionRequest(
    val accountId: Int,
    val categoryId: Int? = null,
    val type: String,
    val amount: Double,
    val merchant: String? = null,
    val paymentMethod: String? = null,
    val notes: String? = null,
    val receiptUrl: String? = null,
    val occurredAt: String
)

// -----------------------------------------------------------------------------
// Section: TransferRequest
// Purpose: Encapsulates the Transfer Request section of this file.
// -----------------------------------------------------------------------------
data class TransferRequest(val fromAccountId: Int, val toAccountId: Int, val amount: Double, val notes: String? = null, val occurredAt: String)

// -----------------------------------------------------------------------------
// Section: BudgetRequest
// Purpose: Encapsulates the Budget Request section of this file.
// -----------------------------------------------------------------------------
data class BudgetRequest(val categoryId: Int, val amount: Double, val month: Int, val year: Int)

// -----------------------------------------------------------------------------
// Section: SavingsGoalRequest
// Purpose: Encapsulates the Savings Goal Request section of this file.
// -----------------------------------------------------------------------------
data class SavingsGoalRequest(val name: String, val targetAmount: Double, val targetDate: String? = null)

// -----------------------------------------------------------------------------
// Section: SavingsContributionRequest
// Purpose: Encapsulates the Savings Contribution Request section of this file.
// -----------------------------------------------------------------------------
data class SavingsContributionRequest(val amount: Double)

// -----------------------------------------------------------------------------
// Section: BillRequest
// Purpose: Encapsulates the Bill Request section of this file.
// -----------------------------------------------------------------------------
data class BillRequest(val name: String, val amount: Double, val dueDate: String, val status: String = "upcoming")

// -----------------------------------------------------------------------------
// Section: ProfileRequest
// Purpose: Encapsulates the Profile Request section of this file.
// -----------------------------------------------------------------------------
data class ProfileRequest(val name: String, val username: String?, val currency: String, val language: String)

// -----------------------------------------------------------------------------
// Section: ChangePasswordRequest
// Purpose: Encapsulates the Change Password Request section of this file.
// -----------------------------------------------------------------------------
data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)
