package com.moneymate.app.data.repository

// =============================================================================
// File: MoneyMateRepository.kt
// Purpose: Repository layer that coordinates API calls and exposes data operations to app state/UI.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import android.content.Context
import com.google.gson.Gson
import com.moneymate.app.core.common.CategoryCatalog
import com.moneymate.app.data.local.TokenManager
import com.moneymate.app.data.model.*
import com.moneymate.app.data.remote.ApiClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

sealed class RepoResult<out T> {
    data class Success<T>(
        val data: T,
        val message: String = ""
    ) : RepoResult<T>()

    data class Error(
        val message: String,
        val code: Int? = null,
        val errorCode: String? = null
    ) : RepoResult<Nothing>()
}


// -----------------------------------------------------------------------------
// Section: MoneyMateRepository
// Purpose: Formatting/helper logic for Money Mate Repository.
// -----------------------------------------------------------------------------
class MoneyMateRepository(context: Context) {
    private val api = ApiClient.moneyMate
    private val tokenManager = TokenManager(context)
    private val gson = Gson()

    fun hasSession() = tokenManager.hasToken()
    fun logout() = tokenManager.clearToken()
    suspend fun warmUpServer() { runCatching { api.health() } }
    private fun auth(): String? = tokenManager.getAuthorizationHeader()



    private suspend fun <T> call(
        block: suspend () -> Response<ApiResponse<T>>
    ): RepoResult<T> {
        return try {
            val response = block()

            if (response.isSuccessful) {
                val body = response.body()

                if (body?.success == true && body.data != null) {
                    RepoResult.Success(
                        body.data,
                        body.message
                    )
                } else if (body?.success == true) {
                    @Suppress("UNCHECKED_CAST")
                    RepoResult.Success(
                        Unit as T,
                        body.message
                    )
                } else {
                    RepoResult.Error(
                        message = body?.message ?: "Request failed",
                        code = response.code(),
                        errorCode = body?.code
                    )
                }
            } else {
                val raw =
                    response.errorBody()?.string()

                val parsed =
                    runCatching {
                        gson.fromJson(
                            raw,
                            ApiResponse::class.java
                        )
                    }.getOrNull()

                if (
                    response.code() == 401 ||
                    response.code() == 403
                ) {
                    tokenManager.clearToken()
                }

                RepoResult.Error(
                    message =
                        parsed?.message
                            ?: "Request failed (${response.code()})",
                    code = response.code(),
                    errorCode = parsed?.code
                )
            }
        } catch (e: Exception) {
            RepoResult.Error(
                message =
                    e.message
                        ?: "Unable to connect to the server"
            )
        }
    }



    suspend fun login(email: String, password: String, rememberMe: Boolean): RepoResult<User> {
        return when (val result = call { api.login(LoginRequest(email.trim(), password)) }) {
            is RepoResult.Success -> {
                val token = result.data.token
                val user = result.data.user
                if (token.isNullOrBlank() || user == null) RepoResult.Error("Invalid login response")
                else {
                    tokenManager.saveToken(token, rememberMe)
                    RepoResult.Success(user, result.message)
                }
            }
            is RepoResult.Error -> result
        }
    }

    suspend fun register(name: String, email: String, password: String): RepoResult<User> {
        return when (val result = call { api.register(RegisterRequest(name.trim(), email.trim(), password)) }) {
            is RepoResult.Success -> {
                val token = result.data.token
                val user = result.data.user
                if (token.isNullOrBlank() || user == null) RepoResult.Error("Invalid registration response")
                else {
                    tokenManager.saveToken(token, true)
                    RepoResult.Success(user, result.message)
                }
            }
            is RepoResult.Error -> result
        }
    }

    suspend fun forgotPassword(email: String) = call { api.forgotPassword(ForgotPasswordRequest(email.trim())) }
    suspend fun verifyReset(token: String) = call { api.verifyReset(VerifyResetRequest(token)) }
    suspend fun resetPassword(token: String, password: String) = call { api.resetPassword(ResetPasswordRequest(token, password)) }

    suspend fun profile() = withAuth { api.profile(it) }
    suspend fun updateProfile(body: ProfileRequest) = withAuth { api.updateProfile(it, body) }
    suspend fun changePassword(current: String, next: String) = withAuth { api.changePassword(it, ChangePasswordRequest(current, next)) }

    suspend fun uploadProfileImage(file: File): RepoResult<UserData> {
        val header = auth() ?: return RepoResult.Error("You are not logged in", 401)
        val mime = when (file.extension.lowercase()) {
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "image/jpeg"
        }
        val body = file.asRequestBody(mime.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("profileImage", file.name, body)
        return call { api.uploadProfileImage(header, part) }
    }

    suspend fun deleteProfileImage() = withAuth { api.deleteProfileImage(it) }
    suspend fun deleteUserAccount(): RepoResult<Any> {
        val result = withAuth { api.deleteUserAccount(it) }
        if (result is RepoResult.Success) tokenManager.clearToken()
        return result
    }

    suspend fun accounts() = withAuth { api.accounts(it) }
    suspend fun createAccount(body: AccountRequest) = withAuth { api.createAccount(it, body) }
    suspend fun updateAccount(id: Int, body: AccountRequest) = withAuth { api.updateAccount(it, id, body) }
    suspend fun deleteAccount(id: Int) = withAuth { api.deleteAccount(it, id) }

    suspend fun categories() = withAuth { api.categories(it) }
    suspend fun createCategory(body: CategoryRequest) = withAuth { api.createCategory(it, body) }

    suspend fun transactions() = withAuth { api.transactions(it) }
    suspend fun recentTransactions(limit: Int = 10) = withAuth { api.recentTransactions(it, limit) }
    suspend fun createTransaction(body: TransactionRequest) = withAuth { api.createTransaction(it, body) }
    suspend fun updateTransaction(id: Int, body: TransactionRequest) = withAuth { api.updateTransaction(it, id, body) }
    suspend fun deleteTransaction(id: Int) = withAuth { api.deleteTransaction(it, id) }
    suspend fun transfer(body: TransferRequest) = withAuth { api.transfer(it, body) }

    suspend fun budgets() = withAuth { api.budgets(it) }
    suspend fun createBudget(body: BudgetRequest) = withAuth { api.createBudget(it, body) }
    suspend fun updateBudget(id: Int, body: BudgetRequest) = withAuth { api.updateBudget(it, id, body) }
    suspend fun deleteBudget(id: Int) = withAuth { api.deleteBudget(it, id) }

    suspend fun savingsGoals() = withAuth { api.savingsGoals(it) }
    suspend fun createSavingsGoal(body: SavingsGoalRequest) = withAuth { api.createSavingsGoal(it, body) }
    suspend fun updateSavingsGoal(id: Int, body: SavingsGoalRequest) = withAuth { api.updateSavingsGoal(it, id, body) }
    suspend fun contributeSavings(id: Int, amount: Double) = withAuth { api.contributeSavings(it, id, SavingsContributionRequest(amount)) }
    suspend fun deleteSavingsGoal(id: Int) = withAuth { api.deleteSavingsGoal(it, id) }

    suspend fun bills() = withAuth { api.bills(it) }
    suspend fun upcomingBills(limit: Int = 5) = withAuth { api.upcomingBills(it, limit) }
    suspend fun createBill(body: BillRequest) = withAuth { api.createBill(it, body) }
    suspend fun updateBill(id: Int, body: BillRequest) = withAuth { api.updateBill(it, id, body) }
    suspend fun deleteBill(id: Int) = withAuth { api.deleteBill(it, id) }

    suspend fun dashboard() = withAuth { api.dashboard(it) }
    suspend fun monthly(month: Int, year: Int) = withAuth { api.monthly(it, month, year) }
    suspend fun categoryBreakdown(month: Int, year: Int) = withAuth { api.categoryBreakdown(it, month, year) }
    suspend fun budgetProgress(month: Int, year: Int) = withAuth { api.budgetProgress(it, month, year) }

    suspend fun notifications() = withAuth { api.notifications(it) }
    suspend fun markNotificationRead(id: Int) = withAuth { api.markNotificationRead(it, id) }
    suspend fun markAllRead() = withAuth { api.markAllRead(it) }
    suspend fun deleteNotification(id: Int) = withAuth { api.deleteNotification(it, id) }

    suspend fun downloadBackup(): RepoResult<ByteArray> {
        val auth = auth() ?: return RepoResult.Error("You are not logged in", 401)
        return try {
            val response = api.downloadBackup(auth)
            if (response.isSuccessful) RepoResult.Success(response.body()?.bytes() ?: ByteArray(0), "Backup downloaded")
            else RepoResult.Error("Backup failed (${response.code()})", response.code())
        } catch (e: Exception) { RepoResult.Error(e.message ?: "Backup failed") }
    }

    suspend fun validateRestore(file: File): RepoResult<Any> {
        val auth = auth() ?: return RepoResult.Error("You are not logged in", 401)
        val body = file.asRequestBody("application/json".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("backupFile", file.name, body)
        return call { api.validateRestore(auth, part) }
    }

    suspend fun restore(file: File): RepoResult<RestoreData> {
        val auth = auth() ?: return RepoResult.Error("You are not logged in", 401)
        val body = file.asRequestBody("application/json".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("backupFile", file.name, body)
        val confirmation = "RESTORE".toRequestBody("text/plain".toMediaTypeOrNull())
        return call { api.restore(auth, part, confirmation) }
    }

    private suspend fun <T> withAuth(block: suspend (String) -> Response<ApiResponse<T>>): RepoResult<T> {
        val header = auth() ?: return RepoResult.Error("You are not logged in", 401)
        return call { block(header) }
    }

    private suspend fun ensureStarterData(currency: String) {
        val header = auth() ?: return
        runCatching {
            val accountResponse = api.accounts(header)
            if (accountResponse.isSuccessful && accountResponse.body()?.data?.accounts.isNullOrEmpty()) {
                listOf(
                    AccountRequest("Cash Wallet", "cash", 0.0, currency),
                    AccountRequest("Main Bank", "bank", 0.0, currency),
                    AccountRequest("Savings", "savings", 0.0, currency),
                    AccountRequest("Credit Card", "credit_card", 0.0, currency)
                ).forEach { api.createAccount(header, it) }
            }

            val defaultCategories = CategoryCatalog.requests()
            val categoryResponse = api.categories(header)
            if (categoryResponse.isSuccessful) {
                val existing = categoryResponse.body()?.data?.categories.orEmpty()
                    .map { "${it.type.lowercase()}::${it.name.lowercase()}" }
                    .toSet()
                defaultCategories
                    .filter { "${it.type.lowercase()}::${it.name.lowercase()}" !in existing }
                    .forEach { api.createCategory(header, it) }
            }
        }
    }
}
