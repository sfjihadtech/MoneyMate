// =============================================================================
// File: restore.routes.js
// Purpose: Express route definitions for restore.routes API endpoints.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const express = require("express");

const {
    authenticateToken,
} = require(
    "../middleware/auth.middleware"
);

const {
    handleBackupUpload,
} = require(
    "../middleware/backupUpload.middleware"
);

const {
    validateRestoreBackup,
    restoreBackup,
} = require(
    "../controllers/restore.controller"
);


// ============================================================
// Express Router
// ============================================================
const router =
    express.Router();


// ============================================================
// Validate / Preview MoneyMate Backup
//
// POST /api/restore/validate
//
// Content-Type:
// multipart/form-data
//
// Field:
//
// backupFile = MoneyMate .json backup
//
// IMPORTANT:
// This endpoint NEVER modifies the database.
// ============================================================
router.post(
    "/validate",
    authenticateToken,
    handleBackupUpload,
    validateRestoreBackup
);


// ============================================================
// Restore MoneyMate Backup
//
// POST /api/restore
//
// Content-Type:
// multipart/form-data
//
// Fields:
//
// backupFile   = MoneyMate .json backup
// confirmation = RESTORE
//
// WARNING:
// This endpoint replaces current finance data.
// ============================================================
router.post(
    "/",
    authenticateToken,
    handleBackupUpload,
    restoreBackup
);


// ============================================================
// Export Router
// ============================================================
module.exports = router;
