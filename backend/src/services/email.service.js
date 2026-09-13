// =============================================================================
// File: email.service.js
// Purpose: Business/service layer for email.service functionality.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const nodemailer = require("nodemailer");


// ============================================================
// Create SMTP Transporter
// ============================================================

// -----------------------------------------------------------------------------
// Section: createTransporter
// Purpose: Handles the create Transporter part of this backend module.
// -----------------------------------------------------------------------------
function createTransporter() {
    const smtpPort = Number(
        process.env.SMTP_PORT || 587
    );

    return nodemailer.createTransport({
        host: process.env.SMTP_HOST,
        port: smtpPort,

        secure:
            smtpPort === 465,

        auth: {
            user: process.env.SMTP_USER,
            pass: process.env.SMTP_PASS,
        },
    });
}


// ============================================================
// Send Password Reset Email
// ============================================================

// -----------------------------------------------------------------------------
// Section: sendPasswordResetEmail
// Purpose: Handles the send Password Reset Email part of this backend module.
// -----------------------------------------------------------------------------
async function sendPasswordResetEmail({
    to,
    name,
    resetToken,
}) {
    const transporter =
        createTransporter();

    const resetBaseUrl =
        process.env.PASSWORD_RESET_URL ||
        "http://localhost:3000/reset-password";

    const resetUrl =
        `${resetBaseUrl}?token=${encodeURIComponent(
            resetToken
        )}`;

    const fromEmail =
        process.env.EMAIL_FROM ||
        process.env.SMTP_USER;

    const displayName =
        name || "MoneyMate User";


    // ========================================================
    // Plain Text Email
    // ========================================================
    const text = `
Hi ${displayName},

We received a request to reset your MoneyMate password.

Reset your password using this link:

${resetUrl}

This link will expire in 15 minutes.

If you did not request a password reset, you can ignore this email.

MoneyMate
`.trim();


    // ========================================================
    // HTML Email
    // ========================================================
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


    // ========================================================
    // Send Email
    // ========================================================
    const info =
        await transporter.sendMail({
            from: `"MoneyMate" <${fromEmail}>`,
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


// ============================================================
// Verify SMTP Connection
// ============================================================

// -----------------------------------------------------------------------------
// Section: verifyEmailConnection
// Purpose: Handles the verify Email Connection part of this backend module.
// -----------------------------------------------------------------------------
async function verifyEmailConnection() {
    const transporter =
        createTransporter();

    await transporter.verify();

    return true;
}


// ============================================================
// Export Email Service
// ============================================================
module.exports = {
    sendPasswordResetEmail,
    verifyEmailConnection,
};
