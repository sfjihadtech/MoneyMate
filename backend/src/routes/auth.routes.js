
// =============================================================================
// File: auth.routes.js
// Purpose: Express route definitions for authentication API endpoints.
// =============================================================================

const express = require("express");

const {
    register,
    login,
    refresh,
    logout,
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

// POST /api/auth/refresh
// Uses a refresh token to issue a new access token and refresh token.
router.post("/refresh", refresh);

// POST /api/auth/logout
// Revokes the supplied refresh token.
router.post("/logout", logout);

// GET /api/auth/me
// Protected route — requires a valid JWT access token.
router.get("/me", authenticateToken, getMe);

module.exports = router;
