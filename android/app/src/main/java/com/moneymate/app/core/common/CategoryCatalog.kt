package com.moneymate.app.core.common

// =============================================================================
// File: CategoryCatalog.kt
// Purpose: Single source of truth for built-in income and expense category definitions and display ordering.
// Notes: Major sections below are commented so the code is easier to read,
//        maintain, and safely extend without changing existing behavior.
// =============================================================================

import com.moneymate.app.data.model.Category
import com.moneymate.app.data.model.CategoryRequest


// -----------------------------------------------------------------------------
// Section: MoneyMateCategoryTemplate
// Purpose: Formatting/helper logic for Money Mate Category Template.
// -----------------------------------------------------------------------------
data class MoneyMateCategoryTemplate(
    val name: String,
    val type: String,
    val icon: String
)


// -----------------------------------------------------------------------------
// Section: CategoryCatalog
// Purpose: Encapsulates the Category Catalog section of this file.
// -----------------------------------------------------------------------------
object CategoryCatalog {
    val income = listOf(
        MoneyMateCategoryTemplate("Salary", "income", "payments"),
        MoneyMateCategoryTemplate("Freelance", "income", "work"),
        MoneyMateCategoryTemplate("Business", "income", "business_center"),
        MoneyMateCategoryTemplate("Investment", "income", "trending_up"),
        MoneyMateCategoryTemplate("Rental Income", "income", "apartment"),
        MoneyMateCategoryTemplate("Gift", "income", "redeem"),
        MoneyMateCategoryTemplate("Refund", "income", "undo"),
        MoneyMateCategoryTemplate("Other", "income", "category")
    )

    val expense = listOf(
        MoneyMateCategoryTemplate("Food & Dining", "expense", "restaurant"),
        MoneyMateCategoryTemplate("Groceries", "expense", "shopping_cart"),
        MoneyMateCategoryTemplate("Shopping", "expense", "shopping_bag"),
        MoneyMateCategoryTemplate("Transport", "expense", "directions_car"),
        MoneyMateCategoryTemplate("Bills & Utilities", "expense", "receipt_long"),
        MoneyMateCategoryTemplate("Rent & Housing", "expense", "home"),
        MoneyMateCategoryTemplate("Entertainment", "expense", "movie"),
        MoneyMateCategoryTemplate("Subscriptions", "expense", "subscriptions"),
        MoneyMateCategoryTemplate("Health", "expense", "health_and_safety"),
        MoneyMateCategoryTemplate("Insurance", "expense", "shield"),
        MoneyMateCategoryTemplate("Travel", "expense", "flight"),
        MoneyMateCategoryTemplate("Education", "expense", "school"),
        MoneyMateCategoryTemplate("Personal Care", "expense", "spa"),
        MoneyMateCategoryTemplate("Kids & Family", "expense", "family_restroom"),
        MoneyMateCategoryTemplate("Pets", "expense", "pets"),
        MoneyMateCategoryTemplate("Gifts & Donations", "expense", "volunteer_activism"),
        MoneyMateCategoryTemplate("Taxes", "expense", "account_balance"),
        MoneyMateCategoryTemplate("Other", "expense", "category")
    )

    val all = income + expense

    fun requests(): List<CategoryRequest> = all.map {
        CategoryRequest(it.name, it.type, it.icon)
    }

    fun guestCategories(): List<Category> = all.mapIndexed { index, item ->
        Category(index + 1, item.name, item.type, item.icon)
    }

    /**
     * Returns categories in the same canonical order used by Add Income / Expense.
     * Custom user categories are preserved and appended after the built-in taxonomy.
     */
    fun ordered(categories: List<Category>, type: String? = null): List<Category> {
        val filtered = categories.filter { type == null || it.type.equals(type, true) }
        val rank = all.mapIndexed { index, item ->
            "${item.type.lowercase()}::${item.name.lowercase()}" to index
        }.toMap()
        return filtered.sortedWith(
            compareBy<Category> { rank["${it.type.lowercase()}::${it.name.lowercase()}"] ?: Int.MAX_VALUE }
                .thenBy { it.name.lowercase() }
        )
    }

    fun incomeCategories(categories: List<Category>): List<Category> = ordered(categories, "income")

    fun expenseCategories(categories: List<Category>): List<Category> = ordered(categories, "expense")
}

