// =============================================================================
// File: trial.routes.js
// Purpose: Routes for server-side 7-day free trial management.
// =============================================================================

const express = require("express");

const {
    claimTrial,
    getTrialStatus,
} = require("../controllers/trial.controller");

const {
    authenticateToken,
} = require("../middleware/auth.middleware");


const router = express.Router();


// =============================================================================
// Trial Routes
// =============================================================================

// Start/claim the free trial for the authenticated account + device.
router.post(
    "/claim",
    authenticateToken,
    claimTrial
);

// Get the authenticated account's current trial status.
router.get(
    "/status",
    authenticateToken,
    getTrialStatus
);


module.exports = router;
