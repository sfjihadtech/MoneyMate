// =============================================================================
// File: account.controller.js
// Purpose: HTTP controller handlers for account.controller requests and responses.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { db } = require("../prisma/db.ts");
const { createAccountSchema } = require("../validators/account.validator");

// POST /api/accounts

// -----------------------------------------------------------------------------
// Section: createAccount
// Purpose: Handles the create Account part of this backend module.
// -----------------------------------------------------------------------------
async function createAccount(req, res) {
    try {
        // Validate request data
        const result = createAccountSchema.safeParse(req.body);

        if (!result.success) {
            return res.status(400).json({
                success: false,
                message: "Validation failed",
                errors: result.error.issues.map((issue) => ({
                    field: issue.path.join("."),
                    message: issue.message,
                })),
            });
        }

        const { name, type, balance, currency } = result.data;

        // Create account for authenticated user
        const account = await db.orm.public.Account.create({
            userId: req.userId,
            name,
            type,
            balance,
            currency,
        });

        return res.status(201).json({
            success: true,
            message: "Account created successfully",
            data: {
                account: {
                    id: account.id,
                    name: account.name,
                    type: account.type,
                    balance: account.balance,
                    currency: account.currency,
                    createdAt: account.createdAt,
                    updatedAt: account.updatedAt,
                },
            },
        });
    } catch (error) {
        console.error("Create account error:", error);

        return res.status(500).json({
            success: false,
            message: "Something went wrong while creating the account",
        });
    }
}


// GET /api/accounts

// -----------------------------------------------------------------------------
// Section: getAccounts
// Purpose: Handles the get Accounts part of this backend module.
// -----------------------------------------------------------------------------
async function getAccounts(req, res) {
    try {
        // Get only accounts belonging to authenticated user
        const accounts = await db.orm.public.Account
            .where({
                userId: req.userId,
            })
            .all();

        return res.status(200).json({
            success: true,
            message: "Accounts retrieved successfully",
            data: {
                accounts: accounts.map((account) => ({
                    id: account.id,
                    name: account.name,
                    type: account.type,
                    balance: account.balance,
                    currency: account.currency,
                    createdAt: account.createdAt,
                    updatedAt: account.updatedAt,
                })),
            },
        });
    } catch (error) {
        console.error("Get accounts error:", error);

        return res.status(500).json({
            success: false,
            message: "Something went wrong while retrieving accounts",
        });
    }
}


// GET /api/accounts/:id

// -----------------------------------------------------------------------------
// Section: getAccountById
// Purpose: Handles the get Account By Id part of this backend module.
// -----------------------------------------------------------------------------
async function getAccountById(req, res) {
    try {
        const accountId = Number(req.params.id);

        // Validate account ID
        if (!Number.isInteger(accountId) || accountId <= 0) {
            return res.status(400).json({
                success: false,
                message: "Invalid account ID",
            });
        }

        // Find account belonging to authenticated user
        const account = await db.orm.public.Account.first({
            id: accountId,
            userId: req.userId,
        });

        if (!account) {
            return res.status(404).json({
                success: false,
                message: "Account not found",
            });
        }

        return res.status(200).json({
            success: true,
            message: "Account retrieved successfully",
            data: {
                account: {
                    id: account.id,
                    name: account.name,
                    type: account.type,
                    balance: account.balance,
                    currency: account.currency,
                    createdAt: account.createdAt,
                    updatedAt: account.updatedAt,
                },
            },
        });
    } catch (error) {
        console.error("Get account error:", error);

        return res.status(500).json({
            success: false,
            message: "Something went wrong while retrieving the account",
        });
    }
}


// PUT /api/accounts/:id

// -----------------------------------------------------------------------------
// Section: updateAccount
// Purpose: Handles the update Account part of this backend module.
// -----------------------------------------------------------------------------
async function updateAccount(req, res) {
    try {
        const accountId = Number(req.params.id);

        // Validate account ID
        if (!Number.isInteger(accountId) || accountId <= 0) {
            return res.status(400).json({
                success: false,
                message: "Invalid account ID",
            });
        }

        // Check that the account belongs to authenticated user
        const account = await db.orm.public.Account.first({
            id: accountId,
            userId: req.userId,
        });

        if (!account) {
            return res.status(404).json({
                success: false,
                message: "Account not found",
            });
        }

        // Validate update data
        const result = createAccountSchema.safeParse(req.body);

        if (!result.success) {
            return res.status(400).json({
                success: false,
                message: "Validation failed",
                errors: result.error.issues.map((issue) => ({
                    field: issue.path.join("."),
                    message: issue.message,
                })),
            });
        }

        const { name, type, balance, currency } = result.data;

        // Update account
        const updatedAccount = await db.orm.public.Account
            .where({
                id: accountId,
                userId: req.userId,
            })
            .update({
                name,
                type,
                balance,
                currency,
            });

        return res.status(200).json({
            success: true,
            message: "Account updated successfully",
            data: {
                account: {
                    id: updatedAccount.id,
                    name: updatedAccount.name,
                    type: updatedAccount.type,
                    balance: updatedAccount.balance,
                    currency: updatedAccount.currency,
                    createdAt: updatedAccount.createdAt,
                    updatedAt: updatedAccount.updatedAt,
                },
            },
        });
    } catch (error) {
        console.error("Update account error:", error);

        return res.status(500).json({
            success: false,
            message: "Something went wrong while updating the account",
        });
    }
}


// DELETE /api/accounts/:id

// -----------------------------------------------------------------------------
// Section: deleteAccount
// Purpose: Handles the delete Account part of this backend module.
// -----------------------------------------------------------------------------
async function deleteAccount(req, res) {
    try {
        const accountId = Number(req.params.id);

        // Validate account ID
        if (!Number.isInteger(accountId) || accountId <= 0) {
            return res.status(400).json({
                success: false,
                message: "Invalid account ID",
            });
        }

        // Check that the account belongs to authenticated user
        const account = await db.orm.public.Account.first({
            id: accountId,
            userId: req.userId,
        });

        if (!account) {
            return res.status(404).json({
                success: false,
                message: "Account not found",
            });
        }

        // Delete account
        await db.orm.public.Account
            .where({
                id: accountId,
                userId: req.userId,
            })
            .delete();

        return res.status(200).json({
            success: true,
            message: "Account deleted successfully",
        });
    } catch (error) {
        console.error("Delete account error:", error);

        return res.status(500).json({
            success: false,
            message: "Something went wrong while deleting the account",
        });
    }
}


module.exports = {
    createAccount,
    getAccounts,
    getAccountById,
    updateAccount,
    deleteAccount,
};
