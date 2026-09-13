// =============================================================================
// File: upload.middleware.js
// Purpose: Express middleware for upload.middleware behavior.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const multer = require("multer");
const path = require("path");
const fs = require("fs");


// ============================================================
// Profile Image Upload Directory
// ============================================================
const uploadDirectory = path.join(
    process.cwd(),
    "uploads",
    "profile-images"
);


// Create directory automatically if it does not exist
if (!fs.existsSync(uploadDirectory)) {
    fs.mkdirSync(uploadDirectory, {
        recursive: true,
    });
}


// ============================================================
// Multer Storage Configuration
// ============================================================
const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, uploadDirectory);
    },

    filename: function (req, file, cb) {
        const extension = path.extname(
            file.originalname
        ).toLowerCase();

        const userId = req.userId || "user";

        const uniqueName =
            `profile-${userId}-${Date.now()}-${Math.round(
                Math.random() * 1e9
            )}${extension}`;

        cb(null, uniqueName);
    },
});


// ============================================================
// Allowed Profile Image Types
// ============================================================

// -----------------------------------------------------------------------------
// Section: profileImageFileFilter
// Purpose: Handles the profile Image File Filter part of this backend module.
// -----------------------------------------------------------------------------
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
//
// Android multipart field name:
// profileImage
// ============================================================
const uploadProfileImage =
    profileImageUpload.single("profileImage");


// ============================================================
// Export Upload Middleware
// ============================================================
module.exports = {
    uploadProfileImage,
};
