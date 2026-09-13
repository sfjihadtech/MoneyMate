// =============================================================================
// File: auth.validator.js
// Purpose: Validation schema and request validation rules for auth.validator.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { z } = require("zod");

const registerSchema = z.object({
    name: z
        .string()
        .trim()
        .min(2, "Name must be at least 2 characters")
        .max(100, "Name is too long"),

    email: z
        .string()
        .trim()
        .email("Please enter a valid email address")
        .toLowerCase(),

    password: z
        .string()
        .min(10, "Password must be at least 10 characters")
        .regex(/[A-Z]/, "Password must contain an uppercase letter")
        .regex(/[a-z]/, "Password must contain a lowercase letter")
        .regex(/[0-9]/, "Password must contain a number")
        .regex(
            /[^A-Za-z0-9]/,
            "Password must contain a special character"
        ),
});

const loginSchema = z.object({
    email: z
        .string()
        .trim()
        .email("Please enter a valid email address")
        .toLowerCase(),

    password: z
        .string()
        .min(1, "Password is required"),
});

module.exports = {
    registerSchema,
    loginSchema,
};
