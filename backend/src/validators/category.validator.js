// =============================================================================
// File: category.validator.js
// Purpose: Validation schema and request validation rules for category.validator.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { z } = require("zod");

const createCategorySchema = z.object({
    name: z
        .string()
        .trim()
        .min(1, "Category name is required")
        .max(100, "Category name is too long"),

    type: z
        .string()
        .trim()
        .min(1, "Category type is required")
        .max(50, "Category type is too long"),

    icon: z
        .string()
        .trim()
        .max(100, "Icon name is too long")
        .optional()
        .nullable(),
});

module.exports = {
    createCategorySchema,
};
