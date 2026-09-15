package com.moneymate.app.data.remote

// =============================================================================
// File: MoneyMateApiService.kt
// Purpose: Retrofit endpoint contract for the MoneyMate REST API.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import com.moneymate.app.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*


// -----------------------------------------------------------------------------
// Section: MoneyMateApiService
// Purpose: Formatting/helper logic for Money Mate Api Service.
// -----------------------------------------------------------------------------
interface MoneyMateApiService {
    @GET("api/health") suspend fun health(): Response<ResponseBody>
    @POST("api/auth/register") suspend fun register(@Body body: RegisterRequest): Response<ApiResponse<AuthData>>
    @POST("api/auth/login") suspend fun login(@Body body: LoginRequest): Response<ApiResponse<AuthData>>
    @GET("api/auth/me") suspend fun me(@Header("Authorization") auth: String): Response<ApiResponse<UserData>>

    @POST("api/password-reset/forgot") suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<ApiResponse<ResetData>>
    @POST("api/password-reset/verify") suspend fun verifyReset(@Body body: VerifyResetRequest): Response<ApiResponse<ResetData>>
    @POST("api/password-reset/reset") suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<ApiResponse<ResetData>>

    @GET("api/profile") suspend fun profile(@Header("Authorization") auth: String): Response<ApiResponse<UserData>>
    @PUT("api/profile") suspend fun updateProfile(@Header("Authorization") auth: String, @Body body: ProfileRequest): Response<ApiResponse<UserData>>
    @PUT("api/profile/password") suspend fun changePassword(@Header("Authorization") auth: String, @Body body: ChangePasswordRequest): Response<ApiResponse<Any>>
    @Multipart @POST("api/profile/image") suspend fun uploadProfileImage(@Header("Authorization") auth: String, @Part image: MultipartBody.Part): Response<ApiResponse<UserData>>
    @DELETE("api/profile/image") suspend fun deleteProfileImage(@Header("Authorization") auth: String): Response<ApiResponse<UserData>>
    @DELETE("api/profile") suspend fun deleteUserAccount(@Header("Authorization") auth: String): Response<ApiResponse<Any>>

    @GET("api/accounts") suspend fun accounts(@Header("Authorization") auth: String): Response<ApiResponse<AccountsData>>
    @POST("api/accounts") suspend fun createAccount(@Header("Authorization") auth: String, @Body body: AccountRequest): Response<ApiResponse<AccountData>>
    @PUT("api/accounts/{id}") suspend fun updateAccount(@Header("Authorization") auth: String, @Path("id") id: Int, @Body body: AccountRequest): Response<ApiResponse<AccountData>>
    @DELETE("api/accounts/{id}") suspend fun deleteAccount(@Header("Authorization") auth: String, @Path("id") id: Int): Response<ApiResponse<Any>>

    @GET("api/categories") suspend fun categories(@Header("Authorization") auth: String): Response<ApiResponse<CategoriesData>>
    @POST("api/categories") suspend fun createCategory(@Header("Authorization") auth: String, @Body body: CategoryRequest): Response<ApiResponse<CategoryData>>

    @GET("api/transactions") suspend fun transactions(@Header("Authorization") auth: String): Response<ApiResponse<TransactionsData>>
    @GET("api/transactions/recent") suspend fun recentTransactions(@Header("Authorization") auth: String, @Query("limit") limit: Int = 10): Response<ApiResponse<TransactionsData>>
    @POST("api/transactions") suspend fun createTransaction(@Header("Authorization") auth: String, @Body body: TransactionRequest): Response<ApiResponse<TransactionData>>
    @PUT("api/transactions/{id}") suspend fun updateTransaction(@Header("Authorization") auth: String, @Path("id") id: Int, @Body body: TransactionRequest): Response<ApiResponse<TransactionData>>
    @DELETE("api/transactions/{id}") suspend fun deleteTransaction(@Header("Authorization") auth: String, @Path("id") id: Int): Response<ApiResponse<Any>>

    @POST("api/transfers") suspend fun transfer(@Header("Authorization") auth: String, @Body body: TransferRequest): Response<ApiResponse<TransferData>>

    @GET("api/budgets") suspend fun budgets(@Header("Authorization") auth: String): Response<ApiResponse<BudgetsData>>
    @POST("api/budgets") suspend fun createBudget(@Header("Authorization") auth: String, @Body body: BudgetRequest): Response<ApiResponse<BudgetData>>
    @PUT("api/budgets/{id}") suspend fun updateBudget(@Header("Authorization") auth: String, @Path("id") id: Int, @Body body: BudgetRequest): Response<ApiResponse<BudgetData>>
    @DELETE("api/budgets/{id}") suspend fun deleteBudget(@Header("Authorization") auth: String, @Path("id") id: Int): Response<ApiResponse<Any>>

    @GET("api/savings-goals") suspend fun savingsGoals(@Header("Authorization") auth: String): Response<ApiResponse<SavingsGoalsData>>
    @POST("api/savings-goals") suspend fun createSavingsGoal(@Header("Authorization") auth: String, @Body body: SavingsGoalRequest): Response<ApiResponse<SavingsGoalData>>
    @PUT("api/savings-goals/{id}") suspend fun updateSavingsGoal(@Header("Authorization") auth: String, @Path("id") id: Int, @Body body: SavingsGoalRequest): Response<ApiResponse<SavingsGoalData>>
    @POST("api/savings-goals/{id}/contributions") suspend fun contributeSavings(@Header("Authorization") auth: String, @Path("id") id: Int, @Body body: SavingsContributionRequest): Response<ApiResponse<SavingsGoalData>>
    @DELETE("api/savings-goals/{id}") suspend fun deleteSavingsGoal(@Header("Authorization") auth: String, @Path("id") id: Int): Response<ApiResponse<Any>>

    @GET("api/bills") suspend fun bills(@Header("Authorization") auth: String): Response<ApiResponse<BillsData>>
    @GET("api/bills/upcoming") suspend fun upcomingBills(@Header("Authorization") auth: String, @Query("limit") limit: Int = 5): Response<ApiResponse<BillsData>>
    @POST("api/bills") suspend fun createBill(@Header("Authorization") auth: String, @Body body: BillRequest): Response<ApiResponse<BillData>>
    @PUT("api/bills/{id}") suspend fun updateBill(@Header("Authorization") auth: String, @Path("id") id: Int, @Body body: BillRequest): Response<ApiResponse<BillData>>
    @DELETE("api/bills/{id}") suspend fun deleteBill(@Header("Authorization") auth: String, @Path("id") id: Int): Response<ApiResponse<Any>>

    @GET("api/dashboard/summary") suspend fun dashboard(@Header("Authorization") auth: String): Response<ApiResponse<DashboardData>>
    @GET("api/insights/monthly") suspend fun monthly(@Header("Authorization") auth: String, @Query("month") month: Int, @Query("year") year: Int): Response<ApiResponse<MonthlyData>>
    @GET("api/insights/categories") suspend fun categoryBreakdown(@Header("Authorization") auth: String, @Query("month") month: Int, @Query("year") year: Int): Response<ApiResponse<CategoryBreakdownData>>
    @GET("api/insights/budgets") suspend fun budgetProgress(@Header("Authorization") auth: String, @Query("month") month: Int, @Query("year") year: Int): Response<ApiResponse<BudgetProgressData>>

    @GET("api/notifications") suspend fun notifications(@Header("Authorization") auth: String, @Query("limit") limit: Int = 50, @Query("unreadOnly") unreadOnly: Boolean = false): Response<ApiResponse<NotificationsData>>
    @PUT("api/notifications/{id}/read") suspend fun markNotificationRead(@Header("Authorization") auth: String, @Path("id") id: Int): Response<ApiResponse<NotificationsData>>
    @PUT("api/notifications/read-all") suspend fun markAllRead(@Header("Authorization") auth: String): Response<ApiResponse<NotificationsData>>
    @DELETE("api/notifications/{id}") suspend fun deleteNotification(@Header("Authorization") auth: String, @Path("id") id: Int): Response<ApiResponse<Any>>

    @Streaming @GET("api/backup") suspend fun downloadBackup(@Header("Authorization") auth: String): Response<ResponseBody>
    @Multipart @POST("api/restore/validate") suspend fun validateRestore(@Header("Authorization") auth: String, @Part backupFile: MultipartBody.Part): Response<ApiResponse<Any>>
    @Multipart @POST("api/restore") suspend fun restore(@Header("Authorization") auth: String, @Part backupFile: MultipartBody.Part, @Part("confirmation") confirmation: RequestBody): Response<ApiResponse<RestoreData>>
}
