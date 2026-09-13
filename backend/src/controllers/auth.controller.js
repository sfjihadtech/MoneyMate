// =============================================================================
// File: auth.controller.js
// Purpose: HTTP controller handlers for auth.controller requests and responses.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");

const { db } = require("../prisma/db.ts");
const { ensureStarterData } = require("../services/starterData.service");
const {
    registerSchema,
    loginSchema,
} = require("../validators/auth.validator");


// -----------------------------------------------------------------------------
// Section: register
// Purpose: Handles the register part of this backend module.
// -----------------------------------------------------------------------------
async function register(req, res) {
    try {
        // Validate request data
        const result = registerSchema.safeParse(req.body);

        if (!result.success) {
            return res.status(400).json({
                success: false,
                message: "Validation failed",
                errors: result.error.issues.map((issue) => ({
                    field: issue.path.join("."),
                    message: issue.message,
                })),
            });
        }

        const { name, email, password } = result.data;

        // Check if email already exists
        const existingUser = await db.orm.public.User.first({
            email,
        });

        if (existingUser) {
            return res.status(409).json({
                success: false,
                message: "An account with this email already exists",
            });
        }

        // Hash password
        const passwordHash = await bcrypt.hash(password, 12);

        // Create user
        const user = await db.orm.public.User.create({
            name,
            email,
            passwordHash,
        });

        await ensureStarterData(user);

        // Create JWT token
        const token = jwt.sign(
            {
                userId: user.id,
            },
            process.env.JWT_SECRET,
            {
                expiresIn: "7d",
            }
        );

        return res.status(201).json({
            success: true,
            message: "Account created successfully",
            data: {
                token,
                user: {
                    id: user.id,
                    name: user.name,
                    email: user.email,
                    username: user.username,
                    currency: user.currency,
                    language: user.language,
                    createdAt: user.createdAt,
                    updatedAt: user.updatedAt,
                },
            },
        });
    } catch (error) {
        console.error("Register error:", error);

        return res.status(500).json({
            success: false,
            message: "Something went wrong while creating your account",
        });
    }
}


// -----------------------------------------------------------------------------
// Section: login
// Purpose: Handles the login part of this backend module.
// -----------------------------------------------------------------------------
async function login(req, res) {
    try {
        // Validate request data
        const result = loginSchema.safeParse(req.body);

        if (!result.success) {
            return res.status(400).json({
                success: false,
                message: "Validation failed",
                errors: result.error.issues.map((issue) => ({
                    field: issue.path.join("."),
                    message: issue.message,
                })),
            });
        }

        const { email, password } = result.data;

        // Find user by email
        const user = await db.orm.public.User.first({
            email,
        });

        // Do not reveal whether the email exists
        if (!user) {
            return res.status(401).json({
                success: false,
                message: "Invalid email or password",
            });
        }

        // Verify password
        const passwordMatches = await bcrypt.compare(
            password,
            user.passwordHash
        );

        if (!passwordMatches) {
            return res.status(401).json({
                success: false,
                message: "Invalid email or password",
            });
        }

        await ensureStarterData(user);

        // Create JWT token
        const token = jwt.sign(
            {
                userId: user.id,
            },
            process.env.JWT_SECRET,
            {
                expiresIn: "7d",
            }
        );

        return res.status(200).json({
            success: true,
            message: "Login successful",
            data: {
                token,
                user: {
                    id: user.id,
                    name: user.name,
                    email: user.email,
                    username: user.username,
                    currency: user.currency,
                    language: user.language,
                    createdAt: user.createdAt,
                    updatedAt: user.updatedAt,
                },
            },
        });
    } catch (error) {
        console.error("Login error:", error);

        return res.status(500).json({
            success: false,
            message: "Something went wrong while signing in",
        });
    }
}


// -----------------------------------------------------------------------------
// Section: getMe
// Purpose: Handles the get Me part of this backend module.
// -----------------------------------------------------------------------------
async function getMe(req, res) {
    try {
        // Find authenticated user
        const user = await db.orm.public.User.first({
            id: req.userId,
        });

        if (!user) {
            return res.status(404).json({
                success: false,
                message: "User not found",
            });
        }

        return res.status(200).json({
            success: true,
            message: "User profile retrieved successfully",
            data: {
                user: {
                    id: user.id,
                    name: user.name,
                    email: user.email,
                    username: user.username,
                    currency: user.currency,
                    language: user.language,
                    createdAt: user.createdAt,
                    updatedAt: user.updatedAt,
                },
            },
        });
    } catch (error) {
        console.error("Get me error:", error);

        return res.status(500).json({
            success: false,
            message: "Something went wrong while retrieving your profile",
        });
    }
}

module.exports = {
    register,
    login,
    getMe,
};
