// =============================================================================
// File: transaction.routes.js
// Purpose: Express route definitions for transaction.routes API endpoints.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const express = require("express");

const {
    createTransaction,
    getTransactions,
    getTransactionById,
    updateTransaction,
    deleteTransaction,
} = require("../controllers/transaction.controller");

const {
    authenticateToken,
} = require("../middleware/auth.middleware");

const router = express.Router();

// GET /api/transactions
router.get("/", authenticateToken, getTransactions);

// POST /api/transactions
router.post("/", authenticateToken, createTransaction);

// GET /api/transactions/:id
router.get("/:id", authenticateToken, getTransactionById);

// PUT /api/transactions/:id
router.put("/:id", authenticateToken, updateTransaction);

// DELETE /api/transactions/:id
router.delete("/:id", authenticateToken, deleteTransaction);

module.exports = router;
