// =============================================================================
// File: insights.validator.js
// Purpose: Validation schema and request validation rules for insights.validator.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { z } = require("zod");

// ============================================================
// Monthly Summary Validation
// ============================================================
const monthlySummarySchema = z.object({
    month: z.number({
        message: "Month must be a number",
    })
        .int("Month must be an integer")
        .min(1, "Month must be between 1 and 12")
        .max(12, "Month must be between 1 and 12"),

    year: z.number({
        message: "Year must be a number",
    })
        .int("Year must be an integer")
        .min(2000, "Year must be valid")
        .max(2100, "Year must be valid"),
});

module.exports = {
    monthlySummarySchema,
};
