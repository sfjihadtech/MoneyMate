// =============================================================================
// File: profile.validator.js
// Purpose: Validation schema and request validation rules for profile.validator.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { z } = require("zod");


// ============================================================
// Update Profile Validation
// ============================================================
const updateProfileSchema = z.object({
    name: z
        .string({
            message: "Name must be a string",
        })
        .trim()
        .min(2, "Name must be at least 2 characters")
        .max(100, "Name is too long"),

    username: z
        .string({
            message: "Username must be a string",
        })
        .trim()
        .min(3, "Username must be at least 3 characters")
        .max(30, "Username is too long")
        .regex(
            /^[a-zA-Z0-9_]+$/,
            "Username can only contain letters, numbers, and underscores"
        )
        .optional()
        .nullable(),

    currency: z
        .string({
            message: "Currency must be a string",
        })
        .trim()
        .length(3, "Currency must be a 3-letter code")
        .toUpperCase(),

    language: z
        .string({
            message: "Language must be a string",
        })
        .trim()
        .min(2, "Language code must be at least 2 characters")
        .max(10, "Language code is too long")
        .toLowerCase(),
});


// ============================================================
// Change Password Validation
// ============================================================
const changePasswordSchema = z.object({
    currentPassword: z
        .string({
            message: "Current password is required",
        })
        .min(1, "Current password is required"),

    newPassword: z
        .string({
            message: "New password is required",
        })
        .min(10, "New password must be at least 10 characters")
        .regex(
            /[A-Z]/,
            "New password must contain an uppercase letter"
        )
        .regex(
            /[a-z]/,
            "New password must contain a lowercase letter"
        )
        .regex(
            /[0-9]/,
            "New password must contain a number"
        )
        .regex(
            /[^A-Za-z0-9]/,
            "New password must contain a special character"
        ),
});


// ============================================================
// Export Profile Validation Schemas
// ============================================================
module.exports = {
    updateProfileSchema,
    changePasswordSchema,
};
