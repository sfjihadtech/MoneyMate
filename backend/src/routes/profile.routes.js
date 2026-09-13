// =============================================================================
// File: profile.routes.js
// Purpose: Express route definitions for profile.routes API endpoints.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const express = require("express");

const {
    getProfile,
    updateProfile,
    uploadProfileImage,
    deleteProfileImage,
    changePassword,
    deleteUserAccount,
} = require("../controllers/profile.controller");

const {
    authenticateToken,
} = require("../middleware/auth.middleware");

const {
    uploadProfileImage: uploadProfileImageMiddleware,
} = require("../middleware/upload.middleware");


const router = express.Router();


// ============================================================
// Get Profile
// ============================================================
router.get(
    "/",
    authenticateToken,
    getProfile
);


// ============================================================
// Update Profile
// ============================================================
router.put(
    "/",
    authenticateToken,
    updateProfile
);


// ============================================================
// Upload / Replace Profile Image
// ============================================================
router.post(
    "/image",
    authenticateToken,
    uploadProfileImageMiddleware,
    uploadProfileImage
);


// ============================================================
// Delete Profile Image
// ============================================================
router.delete(
    "/image",
    authenticateToken,
    deleteProfileImage
);


// ============================================================
// Change Password
// ============================================================
router.put(
    "/password",
    authenticateToken,
    changePassword
);




// ============================================================
// Delete Account Permanently
// ============================================================
router.delete(
    "/",
    authenticateToken,
    deleteUserAccount
);


// ============================================================
// Export Profile Routes
// ============================================================
module.exports = router;
