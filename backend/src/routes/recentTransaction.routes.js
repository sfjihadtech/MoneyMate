// =============================================================================
// File: recentTransaction.routes.js
// Purpose: Express route definitions for recent Transaction.routes API endpoints.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const express = require("express");

const {
    getRecentTransactions,
} = require("../controllers/recentTransaction.controller");

const { authenticateToken } = require("../middleware/auth.middleware");

const router = express.Router();

// ============================================================
// Recent Transactions
// ============================================================
router.get("/", authenticateToken, getRecentTransactions);

module.exports = router;
