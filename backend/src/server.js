// =============================================================================
// File: server.js
// Purpose: Backend application entry point. Configures Express middleware, API routes, health checks, and server startup.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const express = require("express");
const cors = require("cors");
const helmet = require("helmet");
const morgan = require("morgan");
const path = require("path");

require("dotenv").config();
require("tsx/cjs");


// ============================================================
// Route Imports
// ============================================================
const authRoutes = require(
    "./routes/auth.routes"
);

const passwordResetRoutes = require(
    "./routes/passwordReset.routes"
);

const profileRoutes = require(
    "./routes/profile.routes"
);

const accountRoutes = require(
    "./routes/account.routes"
);

const categoryRoutes = require(
    "./routes/category.routes"
);

const recentTransactionRoutes = require(
    "./routes/recentTransaction.routes"
);

const transactionRoutes = require(
    "./routes/transaction.routes"
);

const transferRoutes = require(
    "./routes/transfer.routes"
);

const budgetRoutes = require(
    "./routes/budget.routes"
);

const savingsGoalRoutes = require(
    "./routes/savingsGoal.routes"
);

const upcomingBillRoutes = require(
    "./routes/upcomingBill.routes"
);

const billRoutes = require(
    "./routes/bill.routes"
);

const dashboardRoutes = require(
    "./routes/dashboard.routes"
);

const insightsRoutes = require(
    "./routes/insights.routes"
);

const notificationRoutes = require(
    "./routes/notification.routes"
);

const backupRoutes = require(
    "./routes/backup.routes"
);

const restoreRoutes = require(
    "./routes/restore.routes"
);

const trialRoutes = require(
    "./routes/trial.routes"
);


// ============================================================
// Service Imports
// ============================================================
const {
    startBillReminderScheduler,
} = require(
    "./services/billReminder.service"
);


// ============================================================
// Express App
// ============================================================
const app = express();


// ============================================================
// Global Middleware
// ============================================================
app.use(
    helmet()
);


// -----------------------------------------------------------------------------
// Section: allowedOrigins
// Purpose: Handles the allowed Origins part of this backend module.
// -----------------------------------------------------------------------------
const allowedOrigins = (process.env.CORS_ORIGINS || "")
    .split(",")
    .map((origin) => origin.trim())
    .filter(Boolean);

app.use(
    cors({
        origin(origin, callback) {
            // Native Android requests generally do not send a browser Origin header.
            if (!origin || allowedOrigins.length === 0 || allowedOrigins.includes(origin)) {
                return callback(null, true);
            }

            return callback(
                new Error("Origin not allowed by CORS")
            );
        },
        credentials: true,
    })
);

app.use(
    express.json()
);

app.use(
    express.urlencoded({
        extended: true,
    })
);

app.use(
    morgan(
        process.env.NODE_ENV === "production"
            ? "combined"
            : "dev"
    )
);


// ============================================================
// Public Profile Images
// ============================================================
app.use(
    "/uploads/profile-images",
    express.static(
        path.join(
            process.cwd(),
            "uploads",
            "profile-images"
        )
    )
);


// ============================================================
// Public Receipt Images
// ============================================================
app.use(
    "/uploads/receipts",
    express.static(
        path.join(
            process.cwd(),
            "uploads",
            "receipts"
        )
    )
);


// ============================================================
// Authentication Routes
// ============================================================
app.use(
    "/api/auth",
    authRoutes
);


// ============================================================
// Password Reset Routes
// ============================================================
app.use(
    "/api/password-reset",
    passwordResetRoutes
);


// ============================================================
// Profile Routes
// ============================================================
app.use(
    "/api/profile",
    profileRoutes
);


// ============================================================
// Account Routes
// ============================================================
app.use(
    "/api/accounts",
    accountRoutes
);


// ============================================================
// Category Routes
// ============================================================
app.use(
    "/api/categories",
    categoryRoutes
);


// ============================================================
// Recent Transaction Routes
//
// IMPORTANT:
// Must come before /api/transactions
// ============================================================
app.use(
    "/api/transactions/recent",
    recentTransactionRoutes
);


// ============================================================
// Transaction Routes
// ============================================================
app.use(
    "/api/transactions",
    transactionRoutes
);


// ============================================================
// Transfer Routes
// ============================================================
app.use(
    "/api/transfers",
    transferRoutes
);


// ============================================================
// Budget Routes
// ============================================================
app.use(
    "/api/budgets",
    budgetRoutes
);


// ============================================================
// Savings Goal Routes
// ============================================================
app.use(
    "/api/savings-goals",
    savingsGoalRoutes
);


// ============================================================
// Upcoming Bill Routes
//
// IMPORTANT:
// Must come before /api/bills
// ============================================================
app.use(
    "/api/bills/upcoming",
    upcomingBillRoutes
);


// ============================================================
// Bill Routes
// ============================================================
app.use(
    "/api/bills",
    billRoutes
);


// ============================================================
// Dashboard Routes
// ============================================================
app.use(
    "/api/dashboard",
    dashboardRoutes
);


// ============================================================
// Insights Routes
// ============================================================
app.use(
    "/api/insights",
    insightsRoutes
);


// ============================================================
// Notification Routes
// ============================================================
app.use(
    "/api/notifications",
    notificationRoutes
);


// ============================================================
// Backup Routes
// ============================================================
app.use(
    "/api/backup",
    backupRoutes
);


// ============================================================
// Restore Routes
// ============================================================
app.use(
    "/api/restore",
    restoreRoutes
);


// ============================================================
// Trial Routes
// ============================================================
app.use(
    "/api/trial",
    trialRoutes
);


// ============================================================
// Password Reset HTTPS Bridge
// ============================================================
// Gmail and other email clients may block custom URI schemes such as
// moneymate:// directly inside email buttons.
//
// The email therefore opens this normal HTTPS endpoint first.
// This endpoint then redirects Android to the MoneyMate deep link.
// ============================================================

app.get(
    "/reset-password",
    (req, res) => {
        const token =
            String(req.query.token || "").trim();

        // ----------------------------------------------------
        // Missing Token
        // ----------------------------------------------------
        if (!token) {
            return res
                .status(400)
                .send(
                    "Invalid or missing password reset token."
                );
        }

        // ----------------------------------------------------
        // Open MoneyMate App
        // ----------------------------------------------------
        const appDeepLink =
            `moneymate://reset-password?token=${encodeURIComponent(
                token
            )}`;

        return res.redirect(
            302,
            appDeepLink
        );
    }
);


// ============================================================
// Health Check
// ============================================================
app.get(
    "/api/health",
    (req, res) => {
        res.status(200).json({
            success: true,
            message:
                "MoneyMate API is running",
            timestamp:
                new Date().toISOString(),
        });
    }
);


// ============================================================
// Root Route
// ============================================================
app.get(
    "/",
    (req, res) => {
        res.json({
            name: "MoneyMate API",
            status: "running",
        });
    }
);


// ============================================================
// 404 + Error Handling
// ============================================================
app.use((req, res) => {
    res.status(404).json({
        success: false,
        message: "Route not found",
    });
});

app.use((error, req, res, next) => {
    console.error(
        "Unhandled server error:",
        error
    );

    res.status(500).json({
        success: false,
        message: "Internal server error",
        ...(process.env.NODE_ENV !== "production" && {
            debug: error.message,
        }),
    });
});


// ============================================================
// Start Server
// ============================================================
const PORT =
    process.env.PORT || 5000;

app.listen(
    PORT,
    () => {
        console.log(
            `MoneyMate API running on port ${PORT}`
        );

        // ====================================================
        // Start Bill Reminder Scheduler
        // ====================================================
        startBillReminderScheduler();
    }
);