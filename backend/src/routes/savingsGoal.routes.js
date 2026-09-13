// =============================================================================
// File: savingsGoal.routes.js
// Purpose: Express route definitions for savings Goal.routes API endpoints.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

// ============================================================
// Savings Goal Routes
// ============================================================

const express = require("express");

const {
    createSavingsGoal,
    getSavingsGoals,
    getSavingsGoalById,
    updateSavingsGoal,
    addSavingsGoalContribution,
    deleteSavingsGoal,
} = require("../controllers/savingsGoal.controller");

const {
    authenticateToken,
} = require("../middleware/auth.middleware");

const router = express.Router();


// ============================================================
// POST /api/savings-goals
// Create a new savings goal
// ============================================================
router.post("/", authenticateToken, createSavingsGoal);

// ============================================================
// GET /api/savings-goals
// Get all savings goals of authenticated user
// ============================================================
router.get("/", authenticateToken, getSavingsGoals);

// ============================================================
// GET /api/savings-goals/:id
// Get a single savings goal of authenticated user
// ============================================================
router.get("/:id", authenticateToken, getSavingsGoalById);

// ============================================================
// PUT /api/savings-goals/:id
// Update a single savings goal of authenticated user
// ============================================================
router.put("/:id", authenticateToken, updateSavingsGoal);

// ============================================================
// Add Contribution to Savings Goal
// ============================================================
router.post("/:id/contributions", authenticateToken, addSavingsGoalContribution);

// ============================================================
// DELETE /api/savings-goals/:id
// Delete a single savings goal of authenticated user
// ============================================================
router.delete("/:id", authenticateToken, deleteSavingsGoal);


module.exports = router;
