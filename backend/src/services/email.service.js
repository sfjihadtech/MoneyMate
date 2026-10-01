
"use strict";

// =============================================================================
// File: email.service.js
// Purpose: Handles MoneyMate transactional email delivery.
// Notes:
// - Uses Resend HTTPS API instead of Gmail SMTP.
// - Reuses one Resend client.
// - Preserves the existing password-reset email design and content.
// - Reads credentials only from environment variables.
// =============================================================================

const { Resend } = require("resend");

// =============================================================================
// Resend Client
// =============================================================================

let resendInstance = null;

function createEmailClient() {
    if (resendInstance) {
        return resendInstance;
    }

    const apiKey = String(
        process.env.RESEND_API_KEY || ""
    ).trim();

    if (!apiKey) {
        throw new Error(
            "RESEND_API_KEY is not configured"
        );
    }

    resendInstance = new Resend(apiKey);

    return resendInstance;
}

// Escape dynamic values inserted into HTML.
function escapeHtml(value) {
    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

// =============================================================================
// Send Password Reset Email
// =============================================================================

async function sendPasswordResetEmail({
    to,
    name,
    resetToken,
}) {
    const resend = createEmailClient();

    // -------------------------------------------------------------------------
    // Reset URL — original behavior preserved
    // -------------------------------------------------------------------------

    const resetBaseUrl =
        process.env.PASSWORD_RESET_URL ||
        "http://localhost:3000/reset-password";

    const resetUrl =
        `${resetBaseUrl}?token=${encodeURIComponent(
            resetToken
        )}`;

    // -------------------------------------------------------------------------
    // Sender / Recipient Display Information
    // -------------------------------------------------------------------------

    const fromEmail = String(
        process.env.RESEND_FROM_EMAIL || ""
    ).trim();

    if (!fromEmail) {
        throw new Error(
            "RESEND_FROM_EMAIL is not configured"
        );
    }

    const displayName =
        name || "MoneyMate User";

    const safeName =
        escapeHtml(displayName);

    const safeResetUrl =
        escapeHtml(resetUrl);

    // =========================================================================
    // Plain Text Email — original content preserved
    // =========================================================================

    const text = `
Hi ${displayName},

We received a request to reset your MoneyMate password.

Reset your password using this link:

${resetUrl}

This link will expire in 15 minutes.

If you did not request a password reset, you can ignore this email.

MoneyMate
`.trim();

    // =========================================================================
    // HTML Email — original design preserved
    // =========================================================================

    const html = `
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <meta
        name="viewport"
        content="width=device-width, initial-scale=1.0"
    />
</head>

<body
    style="
        margin: 0;
        padding: 0;
        background-color: #f4f7f5;
        font-family: Arial, sans-serif;
        color: #111827;
    "
>
    <div
        style="
            max-width: 600px;
            margin: 0 auto;
            padding: 40px 20px;
        "
    >
        <div
            style="
                background-color: #ffffff;
                border-radius: 16px;
                padding: 32px;
            "
        >
            <h1
                style="
                    margin-top: 0;
                    color: #0B3B29;
                    font-size: 28px;
                "
            >
                Reset your MoneyMate password
            </h1>

            <p>
                Hi ${safeName},
            </p>

            <p>
                We received a request to reset
                your MoneyMate password.
            </p>

            <p
                style="
                    margin: 32px 0;
                "
            >
                <a
                    href="${safeResetUrl}"
                    style="
                        display: inline-block;
                        background-color: #0B3B29;
                        color: #ffffff;
                        text-decoration: none;
                        padding: 14px 24px;
                        border-radius: 10px;
                        font-weight: bold;
                    "
                >
                    Reset Password
                </a>
            </p>

            <p>
                This reset link will expire
                in 15 minutes.
            </p>

            <p>
                If you did not request this,
                you can safely ignore this email.
            </p>

            <hr
                style="
                    border: none;
                    border-top: 1px solid #e5e7eb;
                    margin: 28px 0;
                "
            />

            <p
                style="
                    color: #6b7280;
                    font-size: 13px;
                "
            >
                If the button does not work,
                copy and paste this link into
                your browser:
            </p>

            <p
                style="
                    color: #6b7280;
                    font-size: 12px;
                    word-break: break-all;
                "
            >
                ${safeResetUrl}
            </p>
        </div>
    </div>
</body>
</html>
`;

    // =========================================================================
    // Send Email Through Resend HTTPS API
    // =========================================================================

    const { data, error } =
        await resend.emails.send({
            from: `MoneyMate <${fromEmail}>`,
            to: [to],
            subject:
                "Reset your MoneyMate password",
            text,
            html,
        });

    if (error) {
        // Do not log the reset token or email contents.
        throw new Error(
            `Resend email delivery failed: ${
                error.name || "API_ERROR"
            }`
        );
    }

    if (!data || !data.id) {
        throw new Error(
            "Resend did not return an email ID"
        );
    }

    return {
        messageId: data.id,
    };
}

// =============================================================================
// Verify Email Configuration
// =============================================================================

// Validates configuration without sending an email.
async function verifyEmailConnection() {
    createEmailClient();

    if (!String(
        process.env.RESEND_FROM_EMAIL || ""
    ).trim()) {
        throw new Error(
            "RESEND_FROM_EMAIL is not configured"
        );
    }

    return true;
}

// =============================================================================
// Export Email Service
// =============================================================================

module.exports = {
    sendPasswordResetEmail,
    verifyEmailConnection,
};
