// =============================================================================
// File: auth.middleware.js
// Purpose: Express middleware for auth.middleware behavior.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const jwt = require("jsonwebtoken");


// -----------------------------------------------------------------------------
// Section: authenticateToken
// Purpose: Handles the authenticate Token part of this backend module.
// -----------------------------------------------------------------------------
function authenticateToken(req, res, next) {
    try {
        // Get Authorization header
        const authHeader = req.headers.authorization;

        if (!authHeader) {
            return res.status(401).json({
                success: false,
                message: "Authentication token is required",
            });
        }

        // Expected format: Bearer <token>
        const [scheme, token] = authHeader.split(" ");

        if (scheme !== "Bearer" || !token) {
            return res.status(401).json({
                success: false,
                message: "Invalid authentication format",
            });
        }

        // Verify JWT token
        const decoded = jwt.verify(
            token,
            process.env.JWT_SECRET
        );

        // Attach authenticated user ID to request
        req.userId = decoded.userId;

        next();
    } catch (error) {
        console.error("Authentication error:", error.message);

        return res.status(401).json({
            success: false,
            message: "Invalid or expired authentication token",
        });
    }
}

module.exports = {
    authenticateToken,
};
