// =============================================================================
// File: transfer.validator.js
// Purpose: Validation schema and request validation rules for transfer.validator.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { z } = require("zod");

const transferSchema = z.object({
    fromAccountId: z.number().int().positive(),
    toAccountId: z.number().int().positive(),
    amount: z.number().positive(),
    notes: z.string().trim().max(500).optional().nullable(),
    occurredAt: z.string().datetime({
        message: "Occurred date must be a valid ISO datetime",
    }),
}).refine(
    (value) => value.fromAccountId !== value.toAccountId,
    {
        message: "Source and destination accounts must be different",
        path: ["toAccountId"],
    }
);

module.exports = { transferSchema };
