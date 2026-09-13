// =============================================================================
// File: account.routes.js
// Purpose: Express route definitions for account.routes API endpoints.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const express = require("express");

const {
    createAccount,
    getAccounts,
    getAccountById,
    updateAccount,
    deleteAccount,
} = require("../controllers/account.controller");

const {
    authenticateToken,
} = require("../middleware/auth.middleware");

const router = express.Router();

// GET /api/accounts
router.get("/", authenticateToken, getAccounts);

// POST /api/accounts
router.post("/", authenticateToken, createAccount);

// GET /api/accounts/:id
router.get("/:id", authenticateToken, getAccountById);

// PUT /api/accounts/:id
router.put("/:id", authenticateToken, updateAccount);

// DELETE /api/accounts/:id
router.delete("/:id", authenticateToken, deleteAccount);

module.exports = router;
