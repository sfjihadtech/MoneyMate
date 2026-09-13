// =============================================================================
// File: budget.routes.js
// Purpose: Express route definitions for budget.routes API endpoints.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const express = require("express");

const {
    createBudget,
    getBudgets,
    getBudgetById,
    updateBudget,
    deleteBudget,
} = require("../controllers/budget.controller");

const { authenticateToken } = require("../middleware/auth.middleware");

const router = express.Router();


// ============================================================
// POST /api/budgets
// Create a new budget
// ============================================================
router.post("/", authenticateToken, createBudget);


// ============================================================
// GET /api/budgets
// Get all budgets of authenticated user
// ============================================================
router.get("/", authenticateToken, getBudgets);


// ============================================================
// GET /api/budgets/:id
// Get a single budget of authenticated user
// ============================================================
router.get("/:id", authenticateToken, getBudgetById);


// ============================================================
// PUT /api/budgets/:id
// Update a single budget of authenticated user
// ============================================================
router.put("/:id", authenticateToken, updateBudget);


// ============================================================
// DELETE /api/budgets/:id
// Delete a single budget of authenticated user
// ============================================================
router.delete("/:id", authenticateToken, deleteBudget);


module.exports = router;
