// =============================================================================
// File: firebaseAdmin.js
// Purpose: Initialize Firebase Admin SDK for server-side push notifications.
// Notes:
// - Service-account credentials come only from environment variables.
// - No private key file is stored in the repository.
// - Firebase Admin is initialized only once.
// =============================================================================

const admin = require("firebase-admin");


// =============================================================================
// Firebase Admin Initialization
// =============================================================================

function initializeFirebaseAdmin() {
    if (admin.apps.length > 0) {
        return admin;
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
        const serviceAccount =
            JSON.parse(rawServiceAccount);

        admin.initializeApp({
            credential:
                admin.credential.cert(serviceAccount),
        });

        console.log(
            "[Firebase Admin] Initialized successfully"
        );

        return admin;
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
// =============================================================================

function getFirebaseAdmin() {
    if (admin.apps.length > 0) {
        return admin;
    }

    return initializeFirebaseAdmin();
}


// =============================================================================
// Exports
// =============================================================================

module.exports = {
    getFirebaseAdmin,
};
