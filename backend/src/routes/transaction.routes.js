// =============================================================================
// File: transaction.routes.js
// Purpose: Express route definitions for transaction.routes API endpoints.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const express = require("express");
const path = require("path");
const fs = require("fs");
const multer = require("multer");


// Receipt upload directory
const receiptUploadDir = path.join(
    __dirname,
    "../../uploads/receipts"
);

fs.mkdirSync(receiptUploadDir, {
    recursive: true,
});

// Receipt image storage
const receiptStorage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, receiptUploadDir);
    },

    filename: (req, file, cb) => {
        const extension = path.extname(file.originalname).toLowerCase();

        const uniqueName =
            `receipt-${req.userId}-${Date.now()}-${Math.round(
                Math.random() * 1e9
            )}${extension}`;

        cb(null, uniqueName);
    },
});

// Accept receipt images only
const receiptUpload = multer({
    storage: receiptStorage,

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

// GET /api/transactions
router.get("/", authenticateToken, getTransactions);

// POST /api/transactions
router.post("/", authenticateToken, createTransaction);

// POST /api/transactions/receipt
router.post(
    "/receipt",
    authenticateToken,
    receiptUpload.single("receipt"),
    (req, res) => {
        if (!req.file) {
            return res.status(400).json({
                success: false,
                message: "Receipt image is required",
            });
        }

        const receiptUrl =
            `/uploads/receipts/${req.file.filename}`;

        return res.status(201).json({
            success: true,
            message: "Receipt uploaded successfully",
            data: {
                receiptUrl,
            },
        });
    }
);



// GET /api/transactions/:id
router.get("/:id", authenticateToken, getTransactionById);

// PUT /api/transactions/:id
router.put("/:id", authenticateToken, updateTransaction);

// DELETE /api/transactions/:id
router.delete("/:id", authenticateToken, deleteTransaction);

module.exports = router;
