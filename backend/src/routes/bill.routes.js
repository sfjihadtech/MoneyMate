// =============================================================================
// File: bill.routes.js
// Purpose: Express route definitions for bill.routes API endpoints.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

// ============================================================
// Bill Routes
// ============================================================

const express = require("express");

const {
    createBill,
    getBills,
    getBillById,
    updateBill,
    deleteBill,
} = require("../controllers/bill.controller");

const {
    authenticateToken,
} = require("../middleware/auth.middleware");

const router = express.Router();


// ============================================================
// POST /api/bills
// Create a new bill
// ============================================================
router.post("/", authenticateToken, createBill);


// ============================================================
// GET /api/bills
// Get all bills of authenticated user
// ============================================================
router.get("/", authenticateToken, getBills);


// ============================================================
// GET /api/bills/:id
// Get a single bill of authenticated user
// ============================================================
router.get("/:id", authenticateToken, getBillById);


// ============================================================
// PUT /api/bills/:id
// Update a single bill of authenticated user
// ============================================================
router.put("/:id", authenticateToken, updateBill);


// ============================================================
// DELETE /api/bills/:id
// Delete a single bill of authenticated user
// ============================================================
router.delete("/:id", authenticateToken, deleteBill);


module.exports = router;
