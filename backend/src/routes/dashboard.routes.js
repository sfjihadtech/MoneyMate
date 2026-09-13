// =============================================================================
// File: dashboard.routes.js
// Purpose: Express route definitions for dashboard.routes API endpoints.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const express = require("express");

const { getDashboardSummary } = require("../controllers/dashboard.controller");
const { authenticateToken } = require("../middleware/auth.middleware");

const router = express.Router();

// ============================================================
// Dashboard Summary
// ============================================================
router.get("/summary", authenticateToken, getDashboardSummary);

module.exports = router;
