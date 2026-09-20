// =============================================================================
// File: transaction.routes.js
// Purpose: Transaction API routes and permanent receipt uploads.
// =============================================================================

const express = require("express");
const multer = require("multer");
const { v2: cloudinary } = require("cloudinary");

const {
    createTransaction,
    getTransactions,
    getTransactionById,
    updateTransaction,
    deleteTransaction,
} = require("../controllers/transaction.controller");

const {
    authenticateToken,
} = require("../middleware/auth.middleware");

const router = express.Router();

// Cloudinary reads CLOUDINARY_URL from the environment
cloudinary.config({
    secure: true,
});

// Keep receipt in memory before uploading to Cloudinary
const receiptUpload = multer({
    storage: multer.memoryStorage(),

    limits: {
        fileSize: 5 * 1024 * 1024,
    },

    fileFilter: (req, file, cb) => {
        const allowedTypes = [
            "image/jpeg",
            "image/png",
            "image/webp",
        ];

        if (!allowedTypes.includes(file.mimetype)) {
            return cb(
                new Error(
                    "Only JPG, PNG, and WEBP receipt images are allowed"
                )
            );
        }

        cb(null, true);
    },
});

// Upload receipt buffer to Cloudinary
function uploadReceiptToCloudinary(file, userId) {
    return new Promise((resolve, reject) => {
        const uploadStream =
            cloudinary.uploader.upload_stream(
                {
                    folder: "moneymate/receipts",
                    resource_type: "image",
                    public_id:
                        `receipt-${userId}-${Date.now()}`,
                    overwrite: false,
                },
                (error, result) => {
                    if (error) {
                        reject(error);
                        return;
                    }

                    resolve(result);
                }
            );

        uploadStream.end(file.buffer);
    });
}

// GET /api/transactions
router.get(
    "/",
    authenticateToken,
    getTransactions
);

// POST /api/transactions
router.post(
    "/",
    authenticateToken,
    createTransaction
);

// POST /api/transactions/receipt
router.post(
    "/receipt",
    authenticateToken,
    receiptUpload.single("receipt"),
    async (req, res) => {
        try {
            if (!req.file) {
                return res.status(400).json({
                    success: false,
                    message: "Receipt image is required",
                });
            }

            const result =
                await uploadReceiptToCloudinary(
                    req.file,
                    req.userId
                );

            if (!result?.secure_url) {
                return res.status(500).json({
                    success: false,
                    message: "Receipt upload failed",
                });
            }

            return res.status(201).json({
                success: true,
                message: "Receipt uploaded successfully",
                data: {
                    receiptUrl: result.secure_url,
                },
            });
        } catch (error) {
            console.error(
                "Receipt upload failed:",
                error
            );

            return res.status(500).json({
                success: false,
                message: "Receipt upload failed",
            });
        }
    }
);

// GET /api/transactions/:id
router.get(
    "/:id",
    authenticateToken,
    getTransactionById
);

// PUT /api/transactions/:id
router.put(
    "/:id",
    authenticateToken,
    updateTransaction
);

// DELETE /api/transactions/:id
router.delete(
    "/:id",
    authenticateToken,
    deleteTransaction
);

module.exports = router;