// =============================================================================
// File: passwordReset.routes.js
// Purpose: Express route definitions for password Reset.routes API endpoints.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const express = require("express");

const {
    forgotPassword,
    verifyResetToken,
    resetPassword,
} = require("../controllers/passwordReset.controller");


const router = express.Router();


// ============================================================
// Forgot Password
// ============================================================
router.post(
    "/forgot",
    forgotPassword
);


// ============================================================
// Verify Reset Token
// ============================================================
router.post(
    "/verify",
    verifyResetToken
);


// ============================================================
// Reset Password
// ============================================================
router.post(
    "/reset",
    resetPassword
);


// ============================================================
// Export Password Reset Routes
// ============================================================
module.exports = router;
