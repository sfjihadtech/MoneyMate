// =============================================================================
// File: account.validator.js
// Purpose: Validation schema and request validation rules for account.validator.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { z } = require("zod");

const createAccountSchema = z.object({
    name: z
        .string()
        .trim()
        .min(1, "Account name is required")
        .max(100, "Account name is too long"),

    type: z
        .string()
        .trim()
        .min(1, "Account type is required")
        .max(50, "Account type is too long"),

    balance: z
        .number({
            message: "Balance must be a number",
        }),

    currency: z
        .string()
        .trim()
        .length(3, "Currency must be a 3-letter code")
        .toUpperCase(),
});

module.exports = {
    createAccountSchema,
};
