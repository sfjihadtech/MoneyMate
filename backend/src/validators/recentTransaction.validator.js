// =============================================================================
// File: recentTransaction.validator.js
// Purpose: Validation schema and request validation rules for recent Transaction.validator.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { z } = require("zod");

// ============================================================
// Recent Transactions Validation
// ============================================================
const recentTransactionsSchema = z.object({
    limit: z.number({
        message: "Limit must be a number",
    })
        .int("Limit must be an integer")
        .min(1, "Limit must be at least 1")
        .max(50, "Limit cannot exceed 50"),
});

module.exports = {
    recentTransactionsSchema,
};
