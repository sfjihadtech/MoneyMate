
// =============================================================================
// File: upload.middleware.js
// Purpose: Validate profile image uploads and keep them in memory temporarily.
// Notes: Cloudinary upload is handled by profile.controller.js.
// =============================================================================

const multer = require("multer");

// ============================================================
// Multer Memory Storage
// ============================================================
// Files are kept in memory until the controller uploads them.
// No profile image is written to Render's local disk.

const storage = multer.memoryStorage();

// ============================================================
// Allowed Profile Image Types
// ============================================================

function profileImageFileFilter(req, file, cb) {
    const allowedMimeTypes = [
        "image/jpeg",
        "image/png",
        "image/webp",
    ];

    if (!allowedMimeTypes.includes(file.mimetype)) {
        return cb(
            new Error(
                "Only JPG, PNG, and WEBP images are allowed"
            )
        );
    }

    cb(null, true);
}

// ============================================================
// Multer Upload Configuration
// ============================================================

const profileImageUpload = multer({
    storage,

    limits: {
        // Maximum image size = 5 MB
        fileSize: 5 * 1024 * 1024,
        files: 1,
    },

    fileFilter: profileImageFileFilter,
});

// ============================================================
// Single Profile Image Upload Middleware
// Android multipart field name: profileImage
// ============================================================

const uploadProfileImage =
    profileImageUpload.single("profileImage");

// ============================================================
// Export Upload Middleware
// ============================================================

module.exports = {
    uploadProfileImage,
};
