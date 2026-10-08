
// =============================================================================
// File: auth.controller.js
// Purpose: Registration, login, profile and rotating refresh-token handling.
// =============================================================================

const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const crypto = require("crypto");

const { db } = require("../prisma/db.ts");
const { pool } = require("../prisma/pg");
const { ensureStarterData } = require("../services/starterData.service");
const {
    registerSchema,
    loginSchema,
} = require("../validators/auth.validator");

// =============================================================================
// Constants
// =============================================================================

const ACCESS_TOKEN_LIFETIME = "7d";
const REFRESH_TOKEN_DAYS = 90;
const REFRESH_TOKEN_MS =
    REFRESH_TOKEN_DAYS * 24 * 60 * 60 * 1000;

// =============================================================================
// Helpers
// =============================================================================

function createAccessToken(userId) {
    return jwt.sign(
        { userId },
        process.env.JWT_SECRET,
        { expiresIn: ACCESS_TOKEN_LIFETIME }
    );
}

function hashRefreshToken(token) {
    return crypto
        .createHash("sha256")
        .update(token)
        .digest("hex");
}

function generateRefreshToken() {
    return crypto.randomBytes(48).toString("hex");
}

function refreshTokenExpiry() {
    return new Date(
        Date.now() + REFRESH_TOKEN_MS
    ).toISOString();
}

function publicUser(user) {
    return {
        id: user.id,
        name: user.name,
        email: user.email,
        username: user.username,
        currency: user.currency,
        language: user.language,
        createdAt: user.createdAt,
        updatedAt: user.updatedAt,
    };
}

async function createRefreshToken(userId, orm = db.orm) {
    const refreshToken = generateRefreshToken();

    await orm.public.RefreshToken.create({
        userId,
        tokenHash: hashRefreshToken(refreshToken),
        expiresAt: refreshTokenExpiry(),
    });

    return refreshToken;
}

function readRefreshToken(req) {
    const token = req.body?.refreshToken;

    if (
        typeof token !== "string" ||
        !/^[a-f0-9]{96}$/.test(token)
    ) {
        return null;
    }

    return token;
}

function invalidRefreshToken(res) {
    return res.status(401).json({
        success: false,
        code: "INVALID_REFRESH_TOKEN",
        message: "Session expired. Please sign in again.",
    });
}

// =============================================================================
// POST /api/auth/register
// =============================================================================

async function register(req, res) {
    try {
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

        const existingUser =
            await db.orm.public.User.first({ email });

        if (existingUser) {
            return res.status(409).json({
                success: false,
                message:
                    "An account with this email already exists",
            });
        }

        const passwordHash = await bcrypt.hash(password, 12);

        const user = await db.orm.public.User.create({
            name,
            email,
            passwordHash,
        });

        await ensureStarterData(user);

        const token = createAccessToken(user.id);
        const refreshToken =
            await createRefreshToken(user.id);

        return res.status(201).json({
            success: true,
            message: "Account created successfully",
            data: {
                token,
                refreshToken,
                user: publicUser(user),
            },
        });
    } catch (error) {
        console.error("Register error:", error);

        return res.status(500).json({
            success: false,
            message:
                "Something went wrong while creating your account",
        });
    }
}

// =============================================================================
// POST /api/auth/login
// =============================================================================

async function login(req, res) {
    try {
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

        const user =
            await db.orm.public.User.first({ email });

        if (!user) {
            return res.status(401).json({
                success: false,
                message: "We couldn't sign you in. Please check your email and password.",
            });
        }

        const passwordMatches = await bcrypt.compare(
            password,
            user.passwordHash
        );

        if (!passwordMatches) {
            return res.status(401).json({
                success: false,
                message: "We couldn't sign you in. Please check your email and password.",
            });
        }

        await ensureStarterData(user);

        const token = createAccessToken(user.id);
        const refreshToken =
            await createRefreshToken(user.id);

        return res.status(200).json({
            success: true,
            message: "Login successful",
            data: {
                token,
                refreshToken,
                user: publicUser(user),
            },
        });
    } catch (error) {
        console.error("Login error:", error);

        return res.status(500).json({
            success: false,
            message:
                "Something went wrong while signing in",
        });
    }
}


// =============================================================================
// POST /api/auth/refresh
// Atomically revoke the old refresh token and issue a new one.
// =============================================================================

async function refresh(req, res) {
    const suppliedToken = readRefreshToken(req);

    if (!suppliedToken) {
        return invalidRefreshToken(res);
    }

    const client = await pool.connect();

    try {
        await client.query("BEGIN");

        const tokenHash = hashRefreshToken(suppliedToken);

        // PostgreSQL atomically updates only an active, unexpired token.
        // Concurrent requests for the same token cannot both succeed.
        const revokedResult = await client.query(
            `
            UPDATE "refreshToken"
            SET "revokedAt" = NOW()
            WHERE "tokenHash" = $1
              AND "revokedAt" IS NULL
              AND "expiresAt" > NOW()
            RETURNING "userId"
            `,
            [tokenHash]
        );

        if (revokedResult.rowCount !== 1) {
            await client.query("ROLLBACK");
            return invalidRefreshToken(res);
        }

        const userId = revokedResult.rows[0].userId;

        // Ensure the account still exists.
        const userResult = await client.query(
            'SELECT "id" FROM "user" WHERE "id" = $1',
            [userId]
        );

        if (userResult.rowCount !== 1) {
            await client.query("ROLLBACK");
            return invalidRefreshToken(res);
        }

        const nextRefreshToken = generateRefreshToken();

        await client.query(
            `
            INSERT INTO "refreshToken"
                ("userId", "tokenHash", "expiresAt")
            VALUES ($1, $2, $3)
            `,
            [
                userId,
                hashRefreshToken(nextRefreshToken),
                refreshTokenExpiry(),
            ]
        );

        await client.query("COMMIT");

        return res.status(200).json({
            success: true,
            message: "Session refreshed successfully",
            data: {
                token: createAccessToken(userId),
                refreshToken: nextRefreshToken,
            },
        });
    } catch (error) {
        await client.query("ROLLBACK").catch(() => {});

        console.error("Refresh session error:", error);

        return res.status(500).json({
            success: false,
            message:
                "Something went wrong while refreshing your session",
        });
    } finally {
        client.release();
    }
}


// =============================================================================
// POST /api/auth/logout
// Revoke the supplied refresh token.
// =============================================================================

async function logout(req, res) {
    const suppliedToken = readRefreshToken(req);

    if (suppliedToken) {
        try {
            await db.orm.public.RefreshToken
                .where({
                    tokenHash:
                        hashRefreshToken(suppliedToken),
                    revokedAt: null,
                })
                .update({
                    revokedAt: new Date().toISOString(),
                });
        } catch (error) {
            console.error("Logout error:", error);

            return res.status(500).json({
                success: false,
                message: "Unable to end session",
            });
        }
    }

    return res.status(200).json({
        success: true,
        message: "Logged out successfully",
    });
}

// =============================================================================
// GET /api/auth/me
// =============================================================================

async function getMe(req, res) {
    try {
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
                user: publicUser(user),
            },
        });
    } catch (error) {
        console.error("Get me error:", error);

        return res.status(500).json({
            success: false,
            message:
                "Something went wrong while retrieving your profile",
        });
    }
}

module.exports = {
    register,
    login,
    refresh,
    logout,
    getMe,
};
