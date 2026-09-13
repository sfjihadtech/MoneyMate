// =============================================================================
// File: category.routes.js
// Purpose: Express route definitions for category.routes API endpoints.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const express = require("express");

const {
    createCategory,
    getCategories,
    getCategoryById,
    updateCategory,
    deleteCategory,
} = require("../controllers/category.controller");

const {
    authenticateToken,
} = require("../middleware/auth.middleware");

const router = express.Router();

// GET /api/categories
router.get("/", authenticateToken, getCategories);

// POST /api/categories
router.post("/", authenticateToken, createCategory);

// GET /api/categories/:id
router.get("/:id", authenticateToken, getCategoryById);

// PUT /api/categories/:id
router.put("/:id", authenticateToken, updateCategory);

// DELETE /api/categories/:id
router.delete("/:id", authenticateToken, deleteCategory);

module.exports = router;
