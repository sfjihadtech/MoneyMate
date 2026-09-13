// =============================================================================
// File: restore.controller.js
// Purpose: HTTP controller handlers for restore.controller requests and responses.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const {
    restoreUserBackup,
} = require(
    "../services/restore.service"
);

const {
    validateBackup,
} = require(
    "../validators/backup.validator"
);


// ============================================================
// Development Error Details
//
// Production-এ database internals response-এ পাঠানো হবে না.
// ============================================================

// -----------------------------------------------------------------------------
// Section: getDevelopmentErrorDetails
// Purpose: Handles the get Development Error Details part of this backend module.
// -----------------------------------------------------------------------------
function getDevelopmentErrorDetails(
    error
) {
    const details = {
        name:
            error?.name || null,

        message:
            error?.message || null,

        code:
            error?.code || null,

        detail:
            error?.detail || null,

        constraint:
            error?.constraint || null,

        table:
            error?.table || null,

        schema:
            error?.schema || null,
    };


    // ========================================================
    // Nested / Wrapped Error
    // ========================================================
    if (error?.cause) {
        details.cause = {
            name:
                error.cause?.name ||
                null,

            message:
                error.cause?.message ||
                null,

            code:
                error.cause?.code ||
                null,

            detail:
                error.cause?.detail ||
                null,

            constraint:
                error.cause?.constraint ||
                null,

            table:
                error.cause?.table ||
                null,

            schema:
                error.cause?.schema ||
                null,
        };
    }


    return details;
}


// ============================================================
// Parse Uploaded Backup File
// ============================================================

// -----------------------------------------------------------------------------
// Section: parseUploadedBackup
// Purpose: Handles the parse Uploaded Backup part of this backend module.
// -----------------------------------------------------------------------------
function parseUploadedBackup(
    file
) {
    if (!file) {
        const error =
            new Error(
                "Backup JSON file is required."
            );

        error.code =
            "BACKUP_FILE_REQUIRED";

        throw error;
    }


    try {
        const fileText =
            file.buffer.toString(
                "utf8"
            );

        return JSON.parse(
            fileText
        );
    } catch (error) {
        const parseError =
            new Error(
                "Invalid JSON backup file."
            );

        parseError.code =
            "INVALID_JSON";

        throw parseError;
    }
}


// ============================================================
// Validate / Preview Backup
//
// This endpoint NEVER modifies the database.
// ============================================================

// -----------------------------------------------------------------------------
// Section: validateRestoreBackup
// Purpose: Handles the validate Restore Backup part of this backend module.
// -----------------------------------------------------------------------------
async function validateRestoreBackup(
    req,
    res
) {
    try {
        // ====================================================
        // Authenticated User
        // ====================================================
        const userId =
            req.userId;

        if (!userId) {
            return res.status(401).json({
                success: false,
                message:
                    "Unauthorized",
            });
        }


        // ====================================================
        // Parse Uploaded Backup
        // ====================================================
        const rawBackup =
            parseUploadedBackup(
                req.file
            );


        // ====================================================
        // Validate Backup
        // ====================================================
        const validation =
            validateBackup(
                rawBackup
            );


        if (!validation.success) {
            return res.status(400).json({
                success: false,
                message:
                    "Invalid MoneyMate backup.",

                errors:
                    validation.errors,
            });
        }


        const backup =
            validation.data;


        // ====================================================
        // Preview Information
        //
        // Nothing is deleted or restored here.
        // ====================================================
        return res.status(200).json({
            success: true,

            message:
                "Backup is valid and ready for restore.",

            data: {
                backup: {
                    app:
                        backup.metadata.app,

                    version:
                        backup.metadata
                            .backupVersion,

                    exportedAt:
                        backup.metadata
                            .exportedAt,
                },

                sourceProfile: {
                    name:
                        backup.user.name,

                    username:
                        backup.user
                            .username,

                    currency:
                        backup.user
                            .currency,

                    language:
                        backup.user
                            .language,
                },

                recordsToRestore:
                    validation.counts,

                warning:
                    "Actual restore will replace the current user's finance data. Authentication credentials will not be restored.",

                databaseModified:
                    false,
            },
        });

    } catch (error) {
        console.error(
            "Validate restore backup error:",
            error
        );


        // ====================================================
        // Backup File Missing
        // ====================================================
        if (
            error.code ===
            "BACKUP_FILE_REQUIRED"
        ) {
            return res.status(400).json({
                success: false,
                message:
                    error.message,
            });
        }


        // ====================================================
        // Invalid JSON
        // ====================================================
        if (
            error.code ===
            "INVALID_JSON"
        ) {
            return res.status(400).json({
                success: false,
                message:
                    error.message,
            });
        }


        // ====================================================
        // Server Error
        // ====================================================
        const response = {
            success: false,
            message:
                "Failed to validate backup.",
        };


        if (
            process.env.NODE_ENV !==
            "production"
        ) {
            response.debug =
                getDevelopmentErrorDetails(
                    error
                );
        }


        return res
            .status(500)
            .json(response);
    }
}


// ============================================================
// Restore Backup
// ============================================================

// -----------------------------------------------------------------------------
// Section: restoreBackup
// Purpose: Handles the restore Backup part of this backend module.
// -----------------------------------------------------------------------------
async function restoreBackup(
    req,
    res
) {
    try {
        // ====================================================
        // Authenticated User
        // ====================================================
        const userId =
            req.userId;

        if (!userId) {
            return res.status(401).json({
                success: false,
                message:
                    "Unauthorized",
            });
        }


        // ====================================================
        // Explicit Confirmation
        // ====================================================
        const confirmation =
            req.body?.confirmation;

        if (
            confirmation !==
            "RESTORE"
        ) {
            return res.status(400).json({
                success: false,
                message:
                    'Restore confirmation required. Send confirmation="RESTORE".',
            });
        }


        // ====================================================
        // Parse Uploaded Backup
        // ====================================================
        const rawBackup =
            parseUploadedBackup(
                req.file
            );


        // ====================================================
        // Restore User Backup
        // ====================================================
        const result =
            await restoreUserBackup(
                userId,
                rawBackup
            );


        // ====================================================
        // Success Response
        // ====================================================
        return res.status(200).json({
            success: true,

            message:
                result.message,

            data: {
                restored:
                    result.restored,

                backup:
                    result.backup,
            },
        });

    } catch (error) {
        console.error(
            "Restore backup error:",
            error
        );


        // ====================================================
        // Backup File Missing
        // ====================================================
        if (
            error.code ===
            "BACKUP_FILE_REQUIRED"
        ) {
            return res.status(400).json({
                success: false,
                message:
                    error.message,
            });
        }


        // ====================================================
        // Invalid JSON
        // ====================================================
        if (
            error.code ===
            "INVALID_JSON"
        ) {
            return res.status(400).json({
                success: false,
                message:
                    error.message,
            });
        }


        // ====================================================
        // Invalid Backup
        // ====================================================
        if (
            error.code ===
            "INVALID_BACKUP"
        ) {
            return res.status(400).json({
                success: false,
                message:
                    "Invalid MoneyMate backup.",

                errors:
                    error.validationErrors ||
                    [],
            });
        }


        // ====================================================
        // User Not Found
        // ====================================================
        if (
            error.code ===
            "USER_NOT_FOUND"
        ) {
            return res.status(404).json({
                success: false,
                message:
                    "User not found",
            });
        }


        // ====================================================
        // Server Error
        //
        // Development only:
        // expose useful database error details.
        // ====================================================
        const response = {
            success: false,
            message:
                "Failed to restore backup.",
        };


        if (
            process.env.NODE_ENV !==
            "production"
        ) {
            response.debug =
                getDevelopmentErrorDetails(
                    error
                );
        }


        return res
            .status(500)
            .json(response);
    }
}


// ============================================================
// Exports
// ============================================================
module.exports = {
    validateRestoreBackup,
    restoreBackup,
};
