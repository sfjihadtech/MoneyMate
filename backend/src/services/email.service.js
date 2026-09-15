// =============================================================================
// File: email.service.js
// Purpose: Handles MoneyMate transactional email delivery.
// Notes:
// - Uses one reusable pooled SMTP transporter.
// - Reuses Gmail SMTP connections when possible.
// - Keeps password-reset email design and content unchanged.
// - SMTP credentials are read only from environment variables.
// =============================================================================

const nodemailer = require("nodemailer");


// =============================================================================
// SMTP Transporter
// =============================================================================

// Keep one transporter instance for the lifetime of the backend process.
// This avoids creating a new Gmail SMTP/TLS connection for every email.
let transporterInstance = null;


// -----------------------------------------------------------------------------
// Create / Reuse SMTP Transporter
// -----------------------------------------------------------------------------
function createTransporter() {

    // Return the existing transporter when already initialized.
    if (transporterInstance) {
        return transporterInstance;
    }


    const smtpPort =
        Number(
            process.env.SMTP_PORT || 587
        );


    const smtpUser =
        String(
            process.env.SMTP_USER || ""
        ).trim();


    // Google may display App Passwords with spaces.
    // Nodemailer requires the raw password without spaces.
    const smtpPass =
        String(
            process.env.SMTP_PASS || ""
        ).replace(/\s+/g, "");


    // -------------------------------------------------------------------------
    // Validate SMTP Configuration
    // -------------------------------------------------------------------------

    if (
        !process.env.SMTP_HOST ||
        !smtpUser ||
        !smtpPass
    ) {
        throw new Error(
            "SMTP is not configured: SMTP_HOST, SMTP_USER and SMTP_PASS are required"
        );
    }


    // -------------------------------------------------------------------------
    // Create Reusable SMTP Pool
    // -------------------------------------------------------------------------

    transporterInstance =
        nodemailer.createTransport({

            host: process.env.SMTP_HOST,

            port: smtpPort,

            secure:
                smtpPort === 465,

            auth: {
                user: smtpUser,
                pass: smtpPass,
            },


            // -------------------------------------------------------------
            // Connection Pool
            // -------------------------------------------------------------
            // Reuse an authenticated SMTP connection instead of opening
            // a completely new Gmail connection for every reset email.

            pool: true,

            maxConnections: 2,

            maxMessages: 50,


            // -------------------------------------------------------------
            // Timeouts
            // -------------------------------------------------------------

            connectionTimeout: 10000,

            greetingTimeout: 10000,

            socketTimeout: 15000,
        });


    return transporterInstance;
}


// =============================================================================
// Send Password Reset Email
// =============================================================================

async function sendPasswordResetEmail({
    to,
    name,
    resetToken,
}) {

    const transporter =
        createTransporter();


    // -------------------------------------------------------------------------
    // Reset URL
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

    const fromEmail =
        process.env.EMAIL_FROM ||
        process.env.SMTP_USER;


    const displayName =
        name || "MoneyMate User";


    // =========================================================================
    // Plain Text Email
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
    // HTML Email
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
                Hi ${displayName},
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
                    href="${resetUrl}"
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
                ${resetUrl}
            </p>
        </div>
    </div>
</body>
</html>
`;


    // =========================================================================
    // Send Email
    // =========================================================================

    const info =
        await transporter.sendMail({
            from:
                `"MoneyMate" <${fromEmail}>`,

            to,

            subject:
                "Reset your MoneyMate password",

            text,

            html,
        });


    return {
        messageId: info.messageId,
    };
}


// =============================================================================
// Verify SMTP Connection
// =============================================================================

async function verifyEmailConnection() {

    // Use the same reusable transporter.
    const transporter =
        createTransporter();


    await transporter.verify();


    return true;
}


// =============================================================================
// Export Email Service
// =============================================================================

module.exports = {
    sendPasswordResetEmail,
    verifyEmailConnection,
};
