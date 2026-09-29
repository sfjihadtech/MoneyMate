
// =============================================================================
// File: firebaseAdmin.js
// Purpose: Initialize Firebase Admin SDK for server-side push notifications.
// =============================================================================

const {
    initializeApp,
    getApps,
    getApp,
    cert,
} = require("firebase-admin/app");

const {
    getMessaging,
} = require("firebase-admin/messaging");


// =============================================================================
// Firebase Admin Initialization
// =============================================================================

function initializeFirebaseAdmin() {
    if (getApps().length > 0) {
        return getApp();
    }

    const rawServiceAccount =
        process.env.FIREBASE_SERVICE_ACCOUNT_JSON;

    if (!rawServiceAccount) {
        console.warn(
            "[Firebase Admin] FIREBASE_SERVICE_ACCOUNT_JSON is not configured"
        );
        return null;
    }

    try {
        const serviceAccount = JSON.parse(rawServiceAccount);

        const app = initializeApp({
            credential: cert(serviceAccount),
        });

        console.log(
            "[Firebase Admin] Initialized successfully"
        );

        return app;
    } catch (error) {
        console.error(
            "[Firebase Admin] Initialization failed:",
            error.message
        );

        return null;
    }
}


// =============================================================================
// Get Firebase Admin Instance
// Preserve the existing .messaging().send() interface.
// =============================================================================

function getFirebaseAdmin() {
    const app = initializeFirebaseAdmin();

    if (!app) {
        return null;
    }

    return {
        messaging: () => getMessaging(app),
    };
}


// =============================================================================
// Exports
// =============================================================================

module.exports = {
    getFirebaseAdmin,
};
