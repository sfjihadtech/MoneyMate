// =============================================================================
// File: transfer.routes.js
// Purpose: Express route definitions for transfer.routes API endpoints.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const express = require("express");
const { createTransfer } = require("../controllers/transfer.controller");
const { authenticateToken } = require("../middleware/auth.middleware");

const router = express.Router();
router.post("/", authenticateToken, createTransfer);

module.exports = router;
