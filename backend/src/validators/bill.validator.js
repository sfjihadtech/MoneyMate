// =============================================================================
// File: bill.validator.js
// Purpose: Validation schemas for bill create and update requests.
// =============================================================================

const { z } = require("zod");


// ============================================================
// Shared Bill Fields
// ============================================================

const billNameSchema = z
    .string({ message: "Bill name is required" })
    .trim()
    .min(1, "Bill name is required")
    .max(100, "Bill name is too long");

const billAmountSchema = z
    .number({ message: "Bill amount must be a number" })
    .positive("Bill amount must be greater than 0");

const billDueDateSchema = z
    .string()
    .datetime({
        message: "Due date must be a valid ISO datetime",
    });

const billStatusSchema = z
    .string()
    .trim()
    .toLowerCase()
    .refine(
        (value) =>
            value === "upcoming" ||
            value === "paid" ||
            value === "overdue",
        "Bill status must be upcoming, paid, or overdue"
    );


// ============================================================
// Create Bill Validation
// ============================================================

const createBillSchema = z.object({
    name: billNameSchema,
    amount: billAmountSchema,
    dueDate: billDueDateSchema,
    status: billStatusSchema.optional(),
});


// ============================================================
// Update Bill Validation
// ============================================================

const updateBillSchema = z
    .object({
        name: billNameSchema.optional(),
        amount: billAmountSchema.optional(),
        dueDate: billDueDateSchema.optional(),
        status: billStatusSchema.optional(),
    })
    .refine(
        (data) => Object.keys(data).length > 0,
        {
            message: "At least one field is required",
        }
    );


// ============================================================
// Exports
// ============================================================

module.exports = {
    createBillSchema,
    updateBillSchema,
};