// =============================================================================
// File: upcomingBill.routes.js
// Purpose: Express route definitions for upcoming Bill.routes API endpoints.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const express = require("express");

const {
    getUpcomingBills,
} = require("../controllers/upcomingBill.controller");

const {
    authenticateToken,
} = require("../middleware/auth.middleware");


const router = express.Router();


// ============================================================
// Get Upcoming Bills
// ============================================================
router.get("/", authenticateToken, getUpcomingBills);


// ============================================================
// Export Upcoming Bills Routes
// ============================================================
module.exports = router;
