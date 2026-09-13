// =============================================================================
// File: backup.controller.js
// Purpose: HTTP controller handlers for backup.controller requests and responses.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const {
    createUserBackup,
} = require(
    "../services/backup.service"
);


// ============================================================
// Download User Backup
// ============================================================

// -----------------------------------------------------------------------------
// Section: downloadBackup
// Purpose: Handles the download Backup part of this backend module.
// -----------------------------------------------------------------------------
async function downloadBackup(req, res) {
    try {
        // ====================================================
        // Authenticated User ID
        //
        // auth.middleware.js stores the authenticated
        // user ID directly on req.userId
        // ====================================================
        const userId =
            req.userId;


        // ====================================================
        // Unauthorized
        // ====================================================
        if (!userId) {
            return res.status(401).json({
                success: false,
                message: "Unauthorized",
            });
        }


        // ====================================================
        // Generate Backup
        // ====================================================
        const backup =
            await createUserBackup(
                userId
            );


        // ====================================================
        // Generate File Name
        //
        // Example:
        // moneymate-backup-2026-09-12.json
        // ====================================================
        const date =
            new Date()
                .toISOString()
                .split("T")[0];

        const fileName =
            `moneymate-backup-${date}.json`;


        // ====================================================
        // Download Headers
        // ====================================================
        res.setHeader(
            "Content-Type",
            "application/json; charset=utf-8"
        );

        res.setHeader(
            "Content-Disposition",
            `attachment; filename="${fileName}"`
        );

        res.setHeader(
            "Cache-Control",
            "no-store"
        );


        // ====================================================
        // Send Backup File
        // ====================================================
        return res
            .status(200)
            .send(
                JSON.stringify(
                    backup,
                    null,
                    2
                )
            );

    } catch (error) {
        console.error(
            "Download backup error:",
            error
        );


        // ====================================================
        // User Not Found
        // ====================================================
        if (
            error.message ===
            "User not found"
        ) {
            return res.status(404).json({
                success: false,
                message: "User not found",
            });
        }


        // ====================================================
        // Server Error
        // ====================================================
        return res.status(500).json({
            success: false,
            message:
                "Failed to create backup",
        });
    }
}


// ============================================================
// Export Controller
// ============================================================
module.exports = {
    downloadBackup,
};
