// =============================================================================
// File: upcomingBill.validator.js
// Purpose: Validation schema and request validation rules for upcoming Bill.validator.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { z } = require("zod");


// ============================================================
// Upcoming Bills Validation
// ============================================================
const upcomingBillsSchema = z.object({
    limit: z
        .number({
            message: "Limit must be a number",
        })
        .int("Limit must be an integer")
        .min(1, "Limit must be at least 1")
        .max(50, "Limit cannot exceed 50"),
});


// ============================================================
// Export Upcoming Bills Validation Schema
// ============================================================
module.exports = {
    upcomingBillsSchema,
};
