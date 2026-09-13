// =============================================================================
// File: backupUpload.middleware.js
// Purpose: Express middleware for backup Upload.middleware behavior.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const multer = require("multer");


// ============================================================
// Memory Storage
//
// Backup file disk-এ permanently save হবে না.
// Restore request-এর সময় memory-তে থাকবে.
// ============================================================
const storage =
    multer.memoryStorage();


// ============================================================
// Backup File Filter
// ============================================================

// -----------------------------------------------------------------------------
// Section: backupFileFilter
// Purpose: Handles the backup File Filter part of this backend module.
// -----------------------------------------------------------------------------
function backupFileFilter(
    req,
    file,
    callback
) {
    // ========================================================
    // Accepted MIME Types
    //
    // Different systems/browser JSON-এর জন্য different
    // MIME type পাঠাতে পারে.
    // ========================================================
    const allowedMimeTypes = [
        "application/json",
        "text/json",
        "text/plain",
        "application/octet-stream",
    ];


    // ========================================================
    // File Extension Check
    // ========================================================
    const fileName =
        file.originalname
            .toLowerCase();

    const hasJsonExtension =
        fileName.endsWith(
            ".json"
        );


    // ========================================================
    // Reject Invalid File
    // ========================================================
    if (
        !hasJsonExtension ||
        !allowedMimeTypes.includes(
            file.mimetype
        )
    ) {
        const error =
            new Error(
                "Only JSON backup files are allowed."
            );

        error.code =
            "INVALID_BACKUP_FILE_TYPE";

        return callback(
            error,
            false
        );
    }


    // ========================================================
    // Accept File
    // ========================================================
    callback(
        null,
        true
    );
}


// ============================================================
// Multer Upload Configuration
// ============================================================
const uploadBackup =
    multer({
        storage,

        fileFilter:
            backupFileFilter,

        limits: {
            // Maximum backup size: 10 MB
            fileSize:
                10 *
                1024 *
                1024,

            // Only one uploaded file
            files: 1,
        },
    });


// ============================================================
// Single Backup File Middleware
//
// multipart/form-data field:
//
// backupFile
// ============================================================
const uploadBackupFile =
    uploadBackup.single(
        "backupFile"
    );


// ============================================================
// Restore Upload Middleware
//
// Handles Multer errors cleanly.
// ============================================================

// -----------------------------------------------------------------------------
// Section: handleBackupUpload
// Purpose: Handles the handle Backup Upload part of this backend module.
// -----------------------------------------------------------------------------
function handleBackupUpload(
    req,
    res,
    next
) {
    uploadBackupFile(
        req,
        res,
        (error) => {
            if (!error) {
                return next();
            }


            // ================================================
            // File Too Large
            // ================================================
            if (
                error instanceof
                multer.MulterError
            ) {
                if (
                    error.code ===
                    "LIMIT_FILE_SIZE"
                ) {
                    return res
                        .status(400)
                        .json({
                            success: false,
                            message:
                                "Backup file is too large. Maximum size is 10 MB.",
                        });
                }


                if (
                    error.code ===
                    "LIMIT_FILE_COUNT"
                ) {
                    return res
                        .status(400)
                        .json({
                            success: false,
                            message:
                                "Only one backup file can be uploaded.",
                        });
                }


                return res
                    .status(400)
                    .json({
                        success: false,
                        message:
                            "Backup upload failed.",
                        error:
                            error.code,
                    });
            }


            // ================================================
            // Invalid File Type
            // ================================================
            if (
                error.code ===
                "INVALID_BACKUP_FILE_TYPE"
            ) {
                return res
                    .status(400)
                    .json({
                        success: false,
                        message:
                            "Only .json MoneyMate backup files are allowed.",
                    });
            }


            // ================================================
            // Unknown Upload Error
            // ================================================
            console.error(
                "Backup upload error:",
                error
            );

            return res
                .status(500)
                .json({
                    success: false,
                    message:
                        "Failed to upload backup file.",
                });
        }
    );
}


// ============================================================
// Exports
// ============================================================
module.exports = {
    handleBackupUpload,
};
