// =============================================================================
// File: auth.routes.js
// Purpose: Express route definitions for auth.routes API endpoints.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const express = require("express");

const {
    register,
    login,
    getMe,
} = require("../controllers/auth.controller");

const {
    authenticateToken,
} = require("../middleware/auth.middleware");

const router = express.Router();

// POST /api/auth/register
router.post("/register", register);

// POST /api/auth/login
router.post("/login", login);

// GET /api/auth/me
// Protected route — requires a valid JWT token
router.get("/me", authenticateToken, getMe);

module.exports = router;
