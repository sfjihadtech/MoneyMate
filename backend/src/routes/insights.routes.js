// =============================================================================
// File: insights.routes.js
// Purpose: Express route definitions for insights.routes API endpoints.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const express = require("express");

const {
    getMonthlySummary,
    getCategoryExpenseBreakdown,
    getBudgetProgress,
} = require("../controllers/insights.controller");

const { authenticateToken } = require("../middleware/auth.middleware");

const router = express.Router();

// ============================================================
// Monthly Summary
// ============================================================
router.get("/monthly", authenticateToken, getMonthlySummary);

// ============================================================
// Category Expense Breakdown
// ============================================================
router.get("/categories", authenticateToken, getCategoryExpenseBreakdown);

// ============================================================
// Budget Progress
// ============================================================
router.get("/budgets", authenticateToken, getBudgetProgress);

module.exports = router;
