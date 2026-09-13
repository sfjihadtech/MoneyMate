// =============================================================================
// File: bill.validator.js
// Purpose: Validation schema and request validation rules for bill.validator.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { z } = require("zod");


// ============================================================
// Create Bill Validation
// ============================================================
const createBillSchema = z.object({
    name: z
        .string({ message: "Bill name is required" })
        .trim()
        .min(1, "Bill name is required")
        .max(100, "Bill name is too long"),

    amount: z
        .number({ message: "Bill amount must be a number" })
        .positive("Bill amount must be greater than 0"),

    dueDate: z
        .string()
        .datetime({
            message: "Due date must be a valid ISO datetime",
        }),

    status: z
        .string()
        .trim()
        .toLowerCase()
        .refine(
            (value) =>
                value === "upcoming" ||
                value === "paid" ||
                value === "overdue",
            "Bill status must be upcoming, paid, or overdue"
        )
        .optional(),
});


// ============================================================
// Update Bill Validation
// ============================================================
const updateBillSchema = z.object({
    name: z
        .string({ message: "Bill name is required" })
        .trim()
        .min(1, "Bill name is required")
        .max(100, "Bill name is too long"),

    amount: z
        .number({ message: "Bill amount must be a number" })
        .positive("Bill amount must be greater than 0"),

    dueDate: z
        .string()
        .datetime({
            message: "Due date must be a valid ISO datetime",
        }),

    status: z
        .string()
        .trim()
        .toLowerCase()
        .refine(
            (value) =>
                value === "upcoming" ||
                value === "paid" ||
                value === "overdue",
            "Bill status must be upcoming, paid, or overdue"
        ),
});


// ============================================================
// Export Bill Validation Schemas
// ============================================================
module.exports = {
    createBillSchema,
    updateBillSchema,
};
