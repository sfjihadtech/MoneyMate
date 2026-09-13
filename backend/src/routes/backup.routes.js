// =============================================================================
// File: backup.routes.js
// Purpose: Express route definitions for backup.routes API endpoints.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const express = require("express");

const {
    authenticateToken,
} = require(
    "../middleware/auth.middleware"
);

const {
    downloadBackup,
} = require(
    "../controllers/backup.controller"
);


// ============================================================
// Express Router
// ============================================================
const router = express.Router();


// ============================================================
// Download Backup
//
// GET /api/backup
// ============================================================
router.get(
    "/",
    authenticateToken,
    downloadBackup
);


// ============================================================
// Export Router
// ============================================================
module.exports = router;
