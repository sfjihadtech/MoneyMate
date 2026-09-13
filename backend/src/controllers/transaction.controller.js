// =============================================================================
// File: transaction.controller.js
// Purpose: HTTP controller handlers for transaction.controller requests and responses.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { db } = require("../prisma/db.ts");
const { createTransactionSchema } = require("../validators/transaction.validator");
const { evaluateBudgetsForTransaction } = require("../services/budgetNotification.service");


// -----------------------------------------------------------------------------
// Section: getBalanceEffect
// Purpose: Handles the get Balance Effect part of this backend module.
// -----------------------------------------------------------------------------
function getBalanceEffect(type, amount) {
    return type === "income" ? Number(amount) : -Number(amount);
}


// -----------------------------------------------------------------------------
// Section: getReverseBalanceEffect
// Purpose: Handles the get Reverse Balance Effect part of this backend module.
// -----------------------------------------------------------------------------
function getReverseBalanceEffect(type, amount) {
    return type === "income" ? -Number(amount) : Number(amount);
}


// -----------------------------------------------------------------------------
// Section: safelyEvaluateBudget
// Purpose: Handles the safely Evaluate Budget part of this backend module.
// -----------------------------------------------------------------------------
async function safelyEvaluateBudget({ userId, categoryId, occurredAt }) {
    try {
        if (!categoryId || !occurredAt) return;
        await evaluateBudgetsForTransaction({ userId, categoryId, occurredAt });
    } catch (error) {
        console.error("Budget notification evaluation error:", error);
    }
}


// -----------------------------------------------------------------------------
// Section: serializeTransaction
// Purpose: Handles the serialize Transaction part of this backend module.
// -----------------------------------------------------------------------------
function serializeTransaction(transaction) {
    return {
        id: transaction.id,
        accountId: transaction.accountId,
        categoryId: transaction.categoryId,
        type: transaction.type,
        amount: transaction.amount,
        merchant: transaction.merchant,
        paymentMethod: transaction.paymentMethod,
        notes: transaction.notes,
        receiptUrl: transaction.receiptUrl,
        occurredAt: transaction.occurredAt,
        createdAt: transaction.createdAt,
        updatedAt: transaction.updatedAt,
    };
}


// -----------------------------------------------------------------------------
// Section: createTransaction
// Purpose: Handles the create Transaction part of this backend module.
// -----------------------------------------------------------------------------
async function createTransaction(req, res) {
    try {
        const userId = req.userId;
        const validation = createTransactionSchema.safeParse(req.body);

        if (!validation.success) {
            return res.status(400).json({
                success: false,
                message: "Validation failed",
                errors: validation.error.issues.map((issue) => issue.message),
            });
        }

        const body = validation.data;

        const transaction = await db.transaction(async (tx) => {
            const orm = tx.orm;
            const account = await orm.public.Account.first({ id: body.accountId, userId });
            if (!account) {
                const error = new Error("Account not found");
                error.code = "ACCOUNT_NOT_FOUND";
                throw error;
            }

            if (body.categoryId) {
                const category = await orm.public.Category.first({ id: body.categoryId, userId });
                if (!category) {
                    const error = new Error("Category not found");
                    error.code = "CATEGORY_NOT_FOUND";
                    throw error;
                }
            }

            const created = await orm.public.Transaction.create({
                userId,
                accountId: body.accountId,
                categoryId: body.categoryId ?? null,
                type: body.type,
                amount: body.amount,
                merchant: body.merchant ?? null,
                paymentMethod: body.paymentMethod ?? null,
                notes: body.notes ?? null,
                receiptUrl: body.receiptUrl ?? null,
                occurredAt: body.occurredAt,
            });

            const currentBalance = Number(account.balance);
            if (!Number.isFinite(currentBalance)) {
                const error = new Error("Invalid account balance");
                error.code = "INVALID_BALANCE";
                throw error;
            }

            await orm.public.Account
                .where({ id: body.accountId, userId })
                .update({ balance: currentBalance + getBalanceEffect(body.type, body.amount) });

            return created;
        });

        await safelyEvaluateBudget({
            userId,
            categoryId: body.categoryId,
            occurredAt: body.occurredAt,
        });

        return res.status(201).json({
            success: true,
            message: "Transaction created successfully",
            data: { transaction: serializeTransaction(transaction) },
        });
    } catch (error) {
        if (error.code === "ACCOUNT_NOT_FOUND" || error.code === "CATEGORY_NOT_FOUND") {
            return res.status(404).json({ success: false, message: error.message });
        }
        if (error.code === "INVALID_BALANCE") {
            return res.status(400).json({ success: false, message: error.message });
        }
        console.error("Create transaction error:", error);
        return res.status(500).json({ success: false, message: "Failed to create transaction" });
    }
}


// -----------------------------------------------------------------------------
// Section: getTransactions
// Purpose: Handles the get Transactions part of this backend module.
// -----------------------------------------------------------------------------
async function getTransactions(req, res) {
    try {
        const userId = req.userId;
        const transactions = await db.orm.public.Transaction.where({ userId }).all();
        transactions.sort((a, b) => new Date(b.occurredAt) - new Date(a.occurredAt));
        return res.status(200).json({
            success: true,
            message: "Transactions retrieved successfully",
            data: { transactions: transactions.map(serializeTransaction) },
        });
    } catch (error) {
        console.error("Get transactions error:", error);
        return res.status(500).json({ success: false, message: "Failed to retrieve transactions" });
    }
}


// -----------------------------------------------------------------------------
// Section: getTransactionById
// Purpose: Handles the get Transaction By Id part of this backend module.
// -----------------------------------------------------------------------------
async function getTransactionById(req, res) {
    try {
        const userId = req.userId;
        const transactionId = Number(req.params.id);
        if (!Number.isInteger(transactionId) || transactionId <= 0) {
            return res.status(400).json({ success: false, message: "Invalid transaction ID" });
        }

        const transaction = await db.orm.public.Transaction.first({ id: transactionId, userId });
        if (!transaction) {
            return res.status(404).json({ success: false, message: "Transaction not found" });
        }

        return res.status(200).json({
            success: true,
            message: "Transaction retrieved successfully",
            data: { transaction: serializeTransaction(transaction) },
        });
    } catch (error) {
        console.error("Get transaction error:", error);
        return res.status(500).json({ success: false, message: "Failed to retrieve transaction" });
    }
}


// -----------------------------------------------------------------------------
// Section: updateTransaction
// Purpose: Handles the update Transaction part of this backend module.
// -----------------------------------------------------------------------------
async function updateTransaction(req, res) {
    try {
        const userId = req.userId;
        const transactionId = Number(req.params.id);
        if (!Number.isInteger(transactionId) || transactionId <= 0) {
            return res.status(400).json({ success: false, message: "Invalid transaction ID" });
        }

        const validation = createTransactionSchema.safeParse(req.body);
        if (!validation.success) {
            return res.status(400).json({
                success: false,
                message: "Validation failed",
                errors: validation.error.issues.map((issue) => issue.message),
            });
        }

        const body = validation.data;
        let oldBudgetInfo = null;

        const updatedTransaction = await db.transaction(async (tx) => {
            const orm = tx.orm;
            const existing = await orm.public.Transaction.first({ id: transactionId, userId });
            if (!existing) {
                const error = new Error("Transaction not found");
                error.code = "TRANSACTION_NOT_FOUND";
                throw error;
            }

            const oldAccount = await orm.public.Account.first({ id: existing.accountId, userId });
            const newAccount = await orm.public.Account.first({ id: body.accountId, userId });
            if (!oldAccount || !newAccount) {
                const error = new Error("Account not found");
                error.code = "ACCOUNT_NOT_FOUND";
                throw error;
            }

            if (body.categoryId) {
                const category = await orm.public.Category.first({ id: body.categoryId, userId });
                if (!category) {
                    const error = new Error("Category not found");
                    error.code = "CATEGORY_NOT_FOUND";
                    throw error;
                }
            }

            const oldBalance = Number(oldAccount.balance);
            if (!Number.isFinite(oldBalance)) {
                const error = new Error("Invalid account balance");
                error.code = "INVALID_BALANCE";
                throw error;
            }

            await orm.public.Account
                .where({ id: existing.accountId, userId })
                .update({ balance: oldBalance + getReverseBalanceEffect(existing.type, existing.amount) });

            await orm.public.Transaction
                .where({ id: transactionId, userId })
                .update({
                    accountId: body.accountId,
                    categoryId: body.categoryId ?? null,
                    type: body.type,
                    amount: body.amount,
                    merchant: body.merchant ?? null,
                    paymentMethod: body.paymentMethod ?? null,
                    notes: body.notes ?? null,
                    receiptUrl: body.receiptUrl ?? null,
                    occurredAt: body.occurredAt,
                });

            const currentNewAccount = await orm.public.Account.first({ id: body.accountId, userId });
            const currentNewBalance = Number(currentNewAccount?.balance);
            if (!Number.isFinite(currentNewBalance)) {
                const error = new Error("Invalid account balance");
                error.code = "INVALID_BALANCE";
                throw error;
            }

            await orm.public.Account
                .where({ id: body.accountId, userId })
                .update({ balance: currentNewBalance + getBalanceEffect(body.type, body.amount) });

            oldBudgetInfo = {
                categoryId: existing.categoryId,
                occurredAt: existing.occurredAt,
            };

            return orm.public.Transaction.first({ id: transactionId, userId });
        });

        if (oldBudgetInfo) {
            await safelyEvaluateBudget({ userId, ...oldBudgetInfo });
        }
        await safelyEvaluateBudget({ userId, categoryId: body.categoryId, occurredAt: body.occurredAt });

        return res.status(200).json({
            success: true,
            message: "Transaction updated successfully",
            data: { transaction: serializeTransaction(updatedTransaction) },
        });
    } catch (error) {
        if (["TRANSACTION_NOT_FOUND", "ACCOUNT_NOT_FOUND", "CATEGORY_NOT_FOUND"].includes(error.code)) {
            return res.status(404).json({ success: false, message: error.message });
        }
        if (error.code === "INVALID_BALANCE") {
            return res.status(400).json({ success: false, message: error.message });
        }
        console.error("Update transaction error:", error);
        return res.status(500).json({ success: false, message: "Failed to update transaction" });
    }
}


// -----------------------------------------------------------------------------
// Section: deleteTransaction
// Purpose: Handles the delete Transaction part of this backend module.
// -----------------------------------------------------------------------------
async function deleteTransaction(req, res) {
    try {
        const userId = req.userId;
        const transactionId = Number(req.params.id);
        if (!Number.isInteger(transactionId) || transactionId <= 0) {
            return res.status(400).json({ success: false, message: "Invalid transaction ID" });
        }

        let budgetInfo = null;

        await db.transaction(async (tx) => {
            const orm = tx.orm;
            const transaction = await orm.public.Transaction.first({ id: transactionId, userId });
            if (!transaction) {
                const error = new Error("Transaction not found");
                error.code = "TRANSACTION_NOT_FOUND";
                throw error;
            }

            const account = await orm.public.Account.first({ id: transaction.accountId, userId });
            if (!account) {
                const error = new Error("Account not found");
                error.code = "ACCOUNT_NOT_FOUND";
                throw error;
            }

            const currentBalance = Number(account.balance);
            if (!Number.isFinite(currentBalance)) {
                const error = new Error("Invalid account balance");
                error.code = "INVALID_BALANCE";
                throw error;
            }

            await orm.public.Account
                .where({ id: account.id, userId })
                .update({ balance: currentBalance + getReverseBalanceEffect(transaction.type, transaction.amount) });

            await orm.public.Transaction.where({ id: transactionId, userId }).delete();
            budgetInfo = { categoryId: transaction.categoryId, occurredAt: transaction.occurredAt };
        });

        if (budgetInfo) {
            await safelyEvaluateBudget({ userId, ...budgetInfo });
        }

        return res.status(200).json({ success: true, message: "Transaction deleted successfully" });
    } catch (error) {
        if (error.code === "TRANSACTION_NOT_FOUND" || error.code === "ACCOUNT_NOT_FOUND") {
            return res.status(404).json({ success: false, message: error.message });
        }
        if (error.code === "INVALID_BALANCE") {
            return res.status(400).json({ success: false, message: error.message });
        }
        console.error("Delete transaction error:", error);
        return res.status(500).json({ success: false, message: "Failed to delete transaction" });
    }
}

module.exports = {
    createTransaction,
    getTransactions,
    getTransactionById,
    updateTransaction,
    deleteTransaction,
};
