// =============================================================================
// File: fcm.controller.js
// Purpose: Register and unregister Firebase Cloud Messaging device tokens.
// Notes:
// - FCM tokens are stored only for authenticated users.
// - One user may have multiple devices.
// - A token can belong to only one user at a time.
// =============================================================================

const { db } = require("../prisma/db.ts");


// =============================================================================
// POST /api/notifications/fcm-token
// Register or refresh an FCM token for the authenticated user.
// =============================================================================

async function registerFcmToken(req, res) {
    try {
        const userId = req.userId;

        const token =
            typeof req.body?.token === "string"
                ? req.body.token.trim()
                : "";

        if (!token) {
            return res.status(400).json({
                success: false,
                message: "FCM token is required",
            });
        }

        if (token.length > 4096) {
            return res.status(400).json({
                success: false,
                message: "Invalid FCM token",
            });
        }


        // ---------------------------------------------------------------------
        // Check whether this token already exists.
        // ---------------------------------------------------------------------

        const existingDevices =
            await db.orm.public.FcmDevice
                .where({
                    token,
                })
                .all();

        const existingDevice =
            existingDevices[0] ?? null;


        // ---------------------------------------------------------------------
        // Token already belongs to this authenticated user.
        // Nothing else needs to be created.
        // ---------------------------------------------------------------------

        if (
            existingDevice &&
            existingDevice.userId === userId
        ) {
            return res.status(200).json({
                success: true,
                message: "FCM token already registered",
            });
        }


        // ---------------------------------------------------------------------
        // The same physical app installation may later be used by another
        // account. Since token is unique, move it to the current user.
        // ---------------------------------------------------------------------

        if (existingDevice) {
            await db.orm.public.FcmDevice
                .where({
                    id: existingDevice.id,
                })
                .update({
                    userId,
                });

            return res.status(200).json({
                success: true,
                message: "FCM token updated successfully",
            });
        }


        // ---------------------------------------------------------------------
        // New token.
        // ---------------------------------------------------------------------

        await db.orm.public.FcmDevice.create({
            userId,
            token,
        });

        return res.status(201).json({
            success: true,
            message: "FCM token registered successfully",
        });

    } catch (error) {
        console.error(
            "Register FCM token error:",
            error
        );

        return res.status(500).json({
            success: false,
            message: "Failed to register FCM token",
        });
    }
}


// =============================================================================
// DELETE /api/notifications/fcm-token
// Unregister an FCM token for the authenticated user.
// Used when push notifications are disabled or the device logs out.
// =============================================================================

async function unregisterFcmToken(req, res) {
    try {
        const userId = req.userId;

        const token =
            typeof req.body?.token === "string"
                ? req.body.token.trim()
                : "";

        if (!token) {
            return res.status(400).json({
                success: false,
                message: "FCM token is required",
            });
        }


        // ---------------------------------------------------------------------
        // Only remove a token that belongs to this authenticated user.
        // ---------------------------------------------------------------------

        const devices =
            await db.orm.public.FcmDevice
                .where({
                    userId,
                    token,
                })
                .all();

        const device =
            devices[0] ?? null;

        if (!device) {
            return res.status(200).json({
                success: true,
                message: "FCM token is not registered",
            });
        }

        await db.orm.public.FcmDevice
            .where({
                id: device.id,
                userId,
            })
            .delete();

        return res.status(200).json({
            success: true,
            message: "FCM token unregistered successfully",
        });

    } catch (error) {
        console.error(
            "Unregister FCM token error:",
            error
        );

        return res.status(500).json({
            success: false,
            message: "Failed to unregister FCM token",
        });
    }
}


// =============================================================================
// Exports
// =============================================================================

module.exports = {
    registerFcmToken,
    unregisterFcmToken,
};