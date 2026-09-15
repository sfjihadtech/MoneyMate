// =============================================================================
// File: passwordReset.controller.js
// Purpose: HTTP controller handlers for password Reset.controller requests and responses.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const crypto = require("crypto");
const bcrypt = require("bcryptjs");

const { db } = require("../prisma/db.ts");

const {
    forgotPasswordSchema,
    resetPasswordSchema,
    verifyResetTokenSchema,
} = require("../validators/passwordReset.validator");

const {
    sendPasswordResetEmail,
} = require("../services/email.service");


// ============================================================
// Generate Secure Reset Token
// ============================================================

// -----------------------------------------------------------------------------
// Section: generateResetToken
// Purpose: Handles the generate Reset Token part of this backend module.
// -----------------------------------------------------------------------------
function generateResetToken() {
    return crypto
        .randomBytes(32)
        .toString("hex");
}


// ============================================================
// Hash Reset Token
// ============================================================

// -----------------------------------------------------------------------------
// Section: hashResetToken
// Purpose: Handles the hash Reset Token part of this backend module.
// -----------------------------------------------------------------------------
function hashResetToken(token) {
    return crypto
        .createHash("sha256")
        .update(token)
        .digest("hex");
}


// ============================================================
// Forgot Password
// ============================================================

// -----------------------------------------------------------------------------
// Section: forgotPassword
// Purpose: Handles the forgot Password part of this backend module.
// -----------------------------------------------------------------------------
async function forgotPassword(req, res) {
    try {
        // ====================================================
        // Validate Request
        // ====================================================
        const validation =
            forgotPasswordSchema.safeParse(req.body);

        if (!validation.success) {
            return res.status(400).json({
                success: false,
                code: "INVALID_REQUEST",
                message:
                    "Unable to send reset link. Please check your email address and try again.",
            });
        }

        const { email } = validation.data;


        // ====================================================
        // Find User
        // ====================================================
        const user = await db.orm.public.User.first({
            email,
        });


        // ====================================================
        // Security:
        // Never reveal whether the email is registered
        // ====================================================
        if (!user) {
            return res.status(200).json({
                success: true,
                code: "RESET_LINK_SENT",
                message:
                    "We've sent a password reset link to your email. Please check your Mail inbox and spam folder.",
                data: {
                    cooldownSeconds: 90,
                },
            });
        }


        // ====================================================
        // Load Previous Reset Requests
        // ====================================================
        const existingTokens =
            await db.orm.public.PasswordResetToken
                .where({
                    userId: user.id,
                })
                .all();


        const nowMs = Date.now();

        const twelveHoursAgoMs =
            nowMs - (12 * 60 * 60 * 1000);

        const ninetySecondsAgoMs =
            nowMs - (90 * 1000);


        // ====================================================
        // Requests Made During Last 12 Hours
        // Maximum = 3
        // ====================================================
        const requestsInLast12Hours =
            existingTokens.filter((token) => {
                const createdAtMs =
                    new Date(token.createdAt).getTime();

                return (
                    Number.isFinite(createdAtMs) &&
                    createdAtMs >= twelveHoursAgoMs
                );
            });


        if (requestsInLast12Hours.length >= 3) {
            return res.status(429).json({
                success: false,
                code: "RESET_LIMIT_REACHED",
                message:
                    "You've reached the maximum number of reset requests. Please try again later.",
            });
        }


        // ====================================================
        // 90-Second Cooldown
        // ====================================================
        const latestRequest =
            requestsInLast12Hours
                .filter((token) => {
                    const createdAtMs =
                        new Date(token.createdAt).getTime();

                    return (
                        Number.isFinite(createdAtMs) &&
                        createdAtMs >= ninetySecondsAgoMs
                    );
                })
                .sort(
                    (a, b) =>
                        new Date(b.createdAt).getTime() -
                        new Date(a.createdAt).getTime()
                )[0];


        if (latestRequest) {
            const createdAtMs =
                new Date(latestRequest.createdAt).getTime();

            const elapsedSeconds =
                Math.floor(
                    (nowMs - createdAtMs) / 1000
                );

            const retryAfterSeconds =
                Math.max(
                    1,
                    90 - elapsedSeconds
                );

            return res.status(429).json({
                success: false,
                code: "RESET_COOLDOWN",
                message:
                    "Please wait before requesting another reset link.",
                data: {
                    retryAfterSeconds,
                },
            });
        }


        // ====================================================
        // Generate Secure Reset Token
        // ====================================================
        const rawToken =
            generateResetToken();

        const tokenHash =
            hashResetToken(rawToken);


        // ====================================================
        // Token Expiry = 15 Minutes
        // ====================================================
        const expiresAt =
            new Date(
                nowMs + (15 * 60 * 1000)
            ).toISOString();


        // ====================================================
        // Save New Hashed Token
        // ====================================================
        const createdToken =
            await db.orm.public.PasswordResetToken.create({
                userId: user.id,
                tokenHash,
                expiresAt,
            });


        // ====================================================
        // Send Password Reset Email
        // ====================================================
        try {
            await sendPasswordResetEmail({
                to: user.email,
                name: user.name,
                resetToken: rawToken,
            });
        } catch (emailError) {
            console.error(
                "Password reset email error:",
                emailError
            );

            // Mark failed token as used
            await db.orm.public.PasswordResetToken
                .where({
                    id: createdToken.id,
                })
                .update({
                    usedAt:
                        new Date().toISOString(),
                });

            return res.status(500).json({
                success: false,
                code: "RESET_EMAIL_FAILED",
                message:
                    "We couldn't send the reset link right now. Please try again shortly.",
            });
        }


        // ====================================================
        // Invalidate Older Active Tokens
        // Only the newest successfully emailed link remains valid
        // ====================================================
        for (const token of existingTokens) {
            if (!token.usedAt) {
                await db.orm.public.PasswordResetToken
                    .where({
                        id: token.id,
                    })
                    .update({
                        usedAt:
                            new Date().toISOString(),
                    });
            }
        }


        // ====================================================
        // Successful Response
        // ====================================================
        const responseData = {
            expiresAt,
            cooldownSeconds: 90,
        };


        // Development only:
        // Return token for local testing.
        // Never expose reset token in production.
        if (process.env.NODE_ENV !== "production") {
            responseData.resetToken =
                rawToken;
        }


        return res.status(200).json({
            success: true,
            code: "RESET_LINK_SENT",
            message:
                "We've sent a password reset link to your email. Please check your Mail inbox and spam folder.",
            data: responseData,
        });

    } catch (error) {
        console.error(
            "Forgot password error:",
            error
        );

        return res.status(500).json({
            success: false,
            code: "RESET_REQUEST_FAILED",
            message:
                "We couldn't send the reset link right now. Please try again shortly.",
        });
    }
}


// ============================================================
// Verify Reset Token
// ============================================================

// -----------------------------------------------------------------------------
// Section: verifyResetToken
// Purpose: Handles the verify Reset Token part of this backend module.
// -----------------------------------------------------------------------------
async function verifyResetToken(req, res) {
    try {
        const validation =
            verifyResetTokenSchema.safeParse(
                req.body
            );

        if (!validation.success) {
            return res.status(400).json({
                success: false,
                message: "Validation failed",
                errors: validation.error.issues.map(
                    (issue) => issue.message
                ),
            });
        }

        const { token } =
            validation.data;

        const tokenHash =
            hashResetToken(token);


        // ====================================================
        // Find Token
        // ====================================================
        const resetToken =
            await db.orm.public.PasswordResetToken.first({
                tokenHash,
            });

        if (!resetToken) {
            return res.status(400).json({
                success: false,
                message:
                    "Invalid or expired reset token",
            });
        }


        // ====================================================
        // Already Used
        // ====================================================
        if (resetToken.usedAt) {
            return res.status(400).json({
                success: false,
                message:
                    "Invalid or expired reset token",
            });
        }


        // ====================================================
        // Check Expiration
        // ====================================================
        const expiresAt =
            new Date(
                resetToken.expiresAt
            );

        if (
            Number.isNaN(
                expiresAt.getTime()
            ) ||
            expiresAt.getTime() <
                Date.now()
        ) {
            return res.status(400).json({
                success: false,
                message:
                    "Invalid or expired reset token",
            });
        }


        return res.status(200).json({
            success: true,
            message:
                "Reset token is valid",
            data: {
                valid: true,
                expiresAt:
                    resetToken.expiresAt,
            },
        });
    } catch (error) {
        console.error(
            "Verify reset token error:",
            error
        );

        return res.status(500).json({
            success: false,
            message:
                "Failed to verify reset token",
        });
    }
}


// ============================================================
// Reset Password
// ============================================================

// -----------------------------------------------------------------------------
// Section: resetPassword
// Purpose: Handles the reset Password part of this backend module.
// -----------------------------------------------------------------------------
async function resetPassword(req, res) {
    try {
        const validation =
            resetPasswordSchema.safeParse(
                req.body
            );

        if (!validation.success) {
            return res.status(400).json({
                success: false,
                message: "Validation failed",
                errors: validation.error.issues.map(
                    (issue) => issue.message
                ),
            });
        }

        const {
            token,
            newPassword,
        } = validation.data;

        const tokenHash =
            hashResetToken(token);


        // ====================================================
        // Find Reset Token
        // ====================================================
        const resetToken =
            await db.orm.public.PasswordResetToken.first({
                tokenHash,
            });

        if (!resetToken) {
            return res.status(400).json({
                success: false,
                message:
                    "Invalid or expired reset token",
            });
        }


        // ====================================================
        // Token Already Used
        // ====================================================
        if (resetToken.usedAt) {
            return res.status(400).json({
                success: false,
                message:
                    "Invalid or expired reset token",
            });
        }


        // ====================================================
        // Check Expiration
        // ====================================================
        const expiresAt =
            new Date(
                resetToken.expiresAt
            );

        if (
            Number.isNaN(
                expiresAt.getTime()
            ) ||
            expiresAt.getTime() <
                Date.now()
        ) {
            return res.status(400).json({
                success: false,
                message:
                    "Invalid or expired reset token",
            });
        }


        // ====================================================
        // Find User
        // ====================================================
        const user =
            await db.orm.public.User.first({
                id: resetToken.userId,
            });

        if (!user) {
            return res.status(400).json({
                success: false,
                message:
                    "Invalid or expired reset token",
            });
        }


        // ====================================================
        // Prevent Reusing Current Password
        // ====================================================
        const isSamePassword =
            await bcrypt.compare(
                newPassword,
                user.passwordHash
            );

        if (isSamePassword) {
            return res.status(400).json({
                success: false,
                message:
                    "New password must be different from current password",
            });
        }


        // ====================================================
        // Hash New Password
        // ====================================================
        const newPasswordHash =
            await bcrypt.hash(
                newPassword,
                12
            );


        // ====================================================
        // Update Password
        // ====================================================
        await db.orm.public.User
            .where({
                id: user.id,
            })
            .update({
                passwordHash:
                    newPasswordHash,
            });


        // ====================================================
        // Invalidate All Reset Tokens
        // ====================================================
        const userTokens =
            await db.orm.public.PasswordResetToken
                .where({
                    userId: user.id,
                })
                .all();

        const usedAt =
            new Date().toISOString();

        for (const userToken of userTokens) {
            if (!userToken.usedAt) {
                await db.orm.public.PasswordResetToken
                    .where({
                        id: userToken.id,
                    })
                    .update({
                        usedAt,
                    });
            }
        }


        return res.status(200).json({
            success: true,
            message:
                "Password reset successfully",
        });
    } catch (error) {
        console.error(
            "Reset password error:",
            error
        );

        return res.status(500).json({
            success: false,
            message:
                "Failed to reset password",
        });
    }
}


// ============================================================
// Export Password Reset Controller
// ============================================================
module.exports = {
    forgotPassword,
    verifyResetToken,
    resetPassword,
};
