// =============================================================================
// File: passwordReset.validator.js
// Purpose: Validation schema and request validation rules for password Reset.validator.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { z } = require("zod");


// ============================================================
// Forgot Password Validation
// ============================================================
const forgotPasswordSchema = z.object({
    email: z
        .string({
            message: "Email is required",
        })
        .trim()
        .email("Please enter a valid email address")
        .toLowerCase(),
});


// ============================================================
// Reset Password Validation
// ============================================================
const resetPasswordSchema = z.object({
    token: z
        .string({
            message: "Reset token is required",
        })
        .trim()
        .min(1, "Reset token is required"),

    newPassword: z
        .string({
            message: "New password is required",
        })
        .min(
            10,
            "New password must be at least 10 characters"
        )
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
// Verify Reset Token Validation
// ============================================================
const verifyResetTokenSchema = z.object({
    token: z
        .string({
            message: "Reset token is required",
        })
        .trim()
        .min(1, "Reset token is required"),
});


// ============================================================
// Export Password Reset Validation Schemas
// ============================================================
module.exports = {
    forgotPasswordSchema,
    resetPasswordSchema,
    verifyResetTokenSchema,
};
