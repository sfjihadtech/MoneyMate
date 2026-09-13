// =============================================================================
// File: savingsGoal.validator.js
// Purpose: Validation schema and request validation rules for savings Goal.validator.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { z } = require("zod");


// ============================================================
// Create Savings Goal Validation
// ============================================================
const createSavingsGoalSchema = z.object({
    name: z
        .string({ message: "Savings goal name is required" })
        .trim()
        .min(1, "Savings goal name is required")
        .max(100, "Savings goal name is too long"),

    targetAmount: z
        .number({ message: "Target amount must be a number" })
        .positive("Target amount must be greater than 0"),

    targetDate: z
        .string()
        .datetime({
            message: "Target date must be a valid ISO datetime",
        })
        .optional()
        .nullable(),
});


// ============================================================
// Update Savings Goal Validation
// ============================================================
const updateSavingsGoalSchema = z.object({
    name: z
        .string({ message: "Savings goal name is required" })
        .trim()
        .min(1, "Savings goal name is required")
        .max(100, "Savings goal name is too long"),

    targetAmount: z
        .number({ message: "Target amount must be a number" })
        .positive("Target amount must be greater than 0"),

    targetDate: z
        .string()
        .datetime({
            message: "Target date must be a valid ISO datetime",
        })
        .optional()
        .nullable(),
});


// ============================================================
// Savings Goal Contribution Validation
// ============================================================
const savingsGoalContributionSchema = z.object({
    amount: z
        .number({
            message: "Contribution amount must be a number",
        })
        .positive("Contribution amount must be greater than 0"),
});


// ============================================================
// Export Savings Goal Validation Schemas
// ============================================================
module.exports = {
    createSavingsGoalSchema,
    updateSavingsGoalSchema,
    savingsGoalContributionSchema,
};
