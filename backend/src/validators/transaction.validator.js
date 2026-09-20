// =============================================================================
// File: transaction.validator.js
// Purpose: Validation schema and request validation rules for transaction.validator.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { z } = require("zod");

const createTransactionSchema = z.object({
    accountId: z
        .number({
            message: "Account ID must be a number",
        })
        .int("Account ID must be an integer")
        .positive("Account ID must be positive"),

    categoryId: z
        .number({
            message: "Category ID must be a number",
        })
        .int("Category ID must be an integer")
        .positive("Category ID must be positive")
        .nullable()
        .optional(),

    type: z
        .string()
        .trim()
        .toLowerCase()
        .refine(
            (value) => value === "income" || value === "expense",
            "Transaction type must be income or expense"
        ),

    amount: z
        .number({
            message: "Amount must be a number",
        })
        .positive("Amount must be greater than 0"),

    merchant: z
        .string()
        .trim()
        .max(150, "Merchant name is too long")
        .optional()
        .nullable(),

    paymentMethod: z
        .string()
        .trim()
        .max(50, "Payment method is too long")
        .optional()
        .nullable(),

    notes: z
        .string()
        .trim()
        .max(500, "Notes are too long")
        .optional()
        .nullable(),

    receiptUrl: z
        .string()
        .trim()
        .max(1000, "Receipt URL is too long")
        .optional()
        .nullable(),

    occurredAt: z
        .string()
        .datetime({
            message: "Occurred date must be a valid ISO datetime",
        }),
});

module.exports = {
    createTransactionSchema,
};
