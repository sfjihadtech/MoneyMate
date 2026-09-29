// =============================================================================
// File: pushNotification.service.js
// Purpose: Send Firebase Cloud Messaging push notifications to user devices.
// Notes:
// - Push delivery is best-effort.
// - A push failure must not break the main notification flow.
// - Invalid/unregistered FCM tokens are removed from the database.
// =============================================================================

const { db } = require("../prisma/db.ts");
const {
    getFirebaseAdmin,
} = require("../config/firebaseAdmin");


// =============================================================================
// Helper: Remove FCM Device
// =============================================================================

async function removeFcmDevice(deviceId) {
    try {
        await db.orm.public.FcmDevice
            .where({
                id: deviceId,
            })
            .delete();
    } catch (error) {
        console.error(
            "[FCM] Failed to remove invalid token:",
            error.message
        );
    }
}


// =============================================================================
// Send Push Notification To User
// =============================================================================

async function sendPushNotificationToUser({
    userId,
    title,
    message,
    data = {},
}) {
    try {
        const firebaseAdmin =
            getFirebaseAdmin();

        if (!firebaseAdmin) {
            console.warn(
                "[FCM] Firebase Admin is not available"
            );

            return {
                success: false,
                sentCount: 0,
            };
        }


        // ---------------------------------------------------------------------
        // Find every registered device for this user.
        // ---------------------------------------------------------------------

        const devices =
            await db.orm.public.FcmDevice
                .where({
                    userId,
                })
                .all();

        if (devices.length === 0) {
            return {
                success: true,
                sentCount: 0,
            };
        }


        // ---------------------------------------------------------------------
        // Send separately so one bad token cannot block another device.
        // ---------------------------------------------------------------------

        let sentCount = 0;

        for (const device of devices) {
            try {
                await firebaseAdmin
                    .messaging()
                    .send({
                        token: device.token,

                        notification: {
                            title,
                            body: message,
                        },

                        data: Object.fromEntries(
                            Object.entries(data).map(
                                ([key, value]) => [
                                    key,
                                    String(value),
                                ]
                            )
                        ),

                        android: {
                            priority: "high",
                        },
                    });

                sentCount += 1;

            } catch (error) {
                const errorCode =
                    error?.code || "";

                console.error(
                    "[FCM] Push delivery failed:",
                    errorCode || error.message
                );


                // -------------------------------------------------------------
                // Firebase says this token can no longer receive messages.
                // Remove it so future sends do not keep failing.
                // -------------------------------------------------------------

                if (
                    errorCode ===
                        "messaging/registration-token-not-registered" ||
                    errorCode ===
                        "messaging/invalid-registration-token"
                ) {
                    await removeFcmDevice(
                        device.id
                    );
                }
            }
        }

        return {
            success: true,
            sentCount,
        };

    } catch (error) {
        console.error(
            "[FCM] Push notification error:",
            error.stack
        );

        return {
            success: false,
            sentCount: 0,
        };
    }
}


// =============================================================================
// Exports
// =============================================================================

module.exports = {
    sendPushNotificationToUser,
};
