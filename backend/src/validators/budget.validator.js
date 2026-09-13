// =============================================================================
// File: budget.validator.js
// Purpose: Validation schema and request validation rules for budget.validator.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { z } = require("zod");


// ============================================================
// Create Budget Validation
// ============================================================
const createBudgetSchema = z.object({
    categoryId: z
        .number({ message: "Category ID must be a number" })
        .int("Category ID must be an integer")
        .positive("Category ID must be positive"),

    amount: z
        .number({ message: "Budget amount must be a number" })
        .positive("Budget amount must be greater than 0"),

    month: z
        .number({ message: "Month must be a number" })
        .int("Month must be an integer")
        .min(1, "Month must be between 1 and 12")
        .max(12, "Month must be between 1 and 12"),

    year: z
        .number({ message: "Year must be a number" })
        .int("Year must be an integer")
        .min(2000, "Year must be valid")
        .max(2100, "Year must be valid"),
});


// ============================================================
// Update Budget Validation
// ============================================================
const updateBudgetSchema = z.object({
    categoryId: z
        .number({ message: "Category ID must be a number" })
        .int("Category ID must be an integer")
        .positive("Category ID must be positive"),

    amount: z
        .number({ message: "Budget amount must be a number" })
        .positive("Budget amount must be greater than 0"),

    month: z
        .number({ message: "Month must be a number" })
        .int("Month must be an integer")
        .min(1, "Month must be between 1 and 12")
        .max(12, "Month must be between 1 and 12"),

    year: z
        .number({ message: "Year must be a number" })
        .int("Year must be an integer")
        .min(2000, "Year must be valid")
        .max(2100, "Year must be valid"),
});


// ============================================================
// Export Budget Validation Schemas
// ============================================================
module.exports = {
    createBudgetSchema,
    updateBudgetSchema,
};
