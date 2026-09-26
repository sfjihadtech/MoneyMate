// =============================================================================
// File: trial.controller.js
// Purpose: Server-side 7-day free trial claim and status handling.
// Notes:
// - Trial start time always comes from the server.
// - One user account can claim the free trial only once.
// - One device can claim the free trial only once.
// - The client never decides when the trial started or whether it expired.
// =============================================================================

const crypto = require("crypto");
const { db } = require("../prisma/db.ts");


// =============================================================================
// Constants
// =============================================================================

const TRIAL_DURATION_DAYS = 7;
const TRIAL_DURATION_MS =
    TRIAL_DURATION_DAYS * 24 * 60 * 60 * 1000;


// =============================================================================
// Helper: Hash Device Identifier
// =============================================================================

function hashDeviceIdentifier(deviceId) {
    return crypto
        .createHash("sha256")
        .update(deviceId)
        .digest("hex");
}


// =============================================================================
// Helper: Build Trial Status
// =============================================================================

function buildTrialStatus(trial) {
    if (!trial) {
        return {
            eligible: false,
            active: false,
            expired: false,
            daysLeft: 0,
            trialStartedAt: null,
            trialEndsAt: null,
        };
    }

    const startedAt = new Date(trial.trialStartedAt);
    const endsAt = new Date(
        startedAt.getTime() + TRIAL_DURATION_MS
    );

    const now = new Date();
    const remainingMs = endsAt.getTime() - now.getTime();

    const active = remainingMs > 0;

    const daysLeft = active
        ? Math.ceil(
              remainingMs / (24 * 60 * 60 * 1000)
          )
        : 0;

    return {
        eligible: true,
        active,
        expired: !active,
        daysLeft,
        trialStartedAt: startedAt.toISOString(),
        trialEndsAt: endsAt.toISOString(),
    };
}


// =============================================================================
// POST /api/trial/claim
// Claim the 7-day free trial.
//
// Rules:
// 1. An account that already claimed a trial keeps its original trial.
// 2. A device already used by another account cannot claim another trial.
// 3. A new account + unused device receives a new 7-day trial.
// =============================================================================

async function claimTrial(req, res) {
    try {
        const userId = req.userId;

        const rawDeviceId =
            typeof req.body?.deviceId === "string"
                ? req.body.deviceId.trim()
                : "";

        if (!rawDeviceId) {
            return res.status(400).json({
                success: false,
                message: "Device identifier is required",
            });
        }

        if (rawDeviceId.length > 500) {
            return res.status(400).json({
                success: false,
                message: "Invalid device identifier",
            });
        }

        const deviceHash =
            hashDeviceIdentifier(rawDeviceId);


        // ---------------------------------------------------------------------
        // 1. Check whether this account already claimed a trial.
        // ---------------------------------------------------------------------

        const userTrials =
            await db.orm.public.TrialDevice
                .where({
                    userId,
                })
                .all();

        const existingUserTrial =
            userTrials[0] ?? null;

        if (existingUserTrial) {
            return res.status(200).json({
                success: true,
                message: "Trial status retrieved successfully",
                data: {
                    trial: buildTrialStatus(
                        existingUserTrial
                    ),
                },
            });
        }


        // ---------------------------------------------------------------------
        // 2. Check whether this device was already used for another account.
        // ---------------------------------------------------------------------

        const deviceTrials =
            await db.orm.public.TrialDevice
                .where({
                    deviceHash,
                })
                .all();

        const existingDeviceTrial =
            deviceTrials[0] ?? null;

        if (existingDeviceTrial) {
            return res.status(200).json({
                success: true,
                message:
                    "This device has already used its free trial",
                data: {
                    trial: {
                        eligible: false,
                        active: false,
                        expired: false,
                        daysLeft: 0,
                        trialStartedAt: null,
                        trialEndsAt: null,
                    },
                },
            });
        }


        // ---------------------------------------------------------------------
        // 3. Account and device are both eligible.
        //    Database/server time creates trialStartedAt automatically.
        // ---------------------------------------------------------------------

        const createdTrial =
            await db.orm.public.TrialDevice.create({
                userId,
                deviceHash,
            });

        return res.status(201).json({
            success: true,
            message: "7-day free trial started successfully",
            data: {
                trial: buildTrialStatus(
                    createdTrial
                ),
            },
        });

    } catch (error) {
        console.error("Claim trial error:", error);

        // Unique constraints on userId/deviceHash provide the final
        // database-level protection against simultaneous duplicate claims.
        return res.status(500).json({
            success: false,
            message:
                "Something went wrong while starting the free trial",
        });
    }
}


// =============================================================================
// GET /api/trial/status
// Return the authenticated account's server-side trial status.
// =============================================================================

async function getTrialStatus(req, res) {
    try {
        const userId = req.userId;

        const trials =
            await db.orm.public.TrialDevice
                .where({
                    userId,
                })
                .all();

        const trial =
            trials[0] ?? null;

        if (!trial) {
            return res.status(200).json({
                success: true,
                message:
                    "No free trial has been claimed for this account",
                data: {
                    trial: {
                        eligible: true,
                        active: false,
                        expired: false,
                        daysLeft: 0,
                        trialStartedAt: null,
                        trialEndsAt: null,
                    },
                },
            });
        }

        return res.status(200).json({
            success: true,
            message: "Trial status retrieved successfully",
            data: {
                trial: buildTrialStatus(trial),
            },
        });

    } catch (error) {
        console.error("Get trial status error:", error);

        return res.status(500).json({
            success: false,
            message:
                "Something went wrong while retrieving trial status",
        });
    }
}


// =============================================================================
// Exports
// =============================================================================

module.exports = {
    claimTrial,
    getTrialStatus,
};
