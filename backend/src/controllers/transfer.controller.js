// =============================================================================
// File: transfer.controller.js
// Purpose: HTTP controller handlers for transfer.controller requests and responses.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { db } = require("../prisma/db.ts");
const { transferSchema } = require("../validators/transfer.validator");


// -----------------------------------------------------------------------------
// Section: createTransfer
// Purpose: Handles the create Transfer part of this backend module.
// -----------------------------------------------------------------------------
async function createTransfer(req, res) {
    const validation = transferSchema.safeParse(req.body);

    if (!validation.success) {
        return res.status(400).json({
            success: false,
            message: "Validation failed",
            errors: validation.error.issues.map((issue) => ({
                field: issue.path.join("."),
                message: issue.message,
            })),
        });
    }

    const userId = req.userId;
    const {
        fromAccountId,
        toAccountId,
        amount,
        notes,
        occurredAt,
    } = validation.data;

    try {
        const result = await db.transaction(async (tx) => {
            const orm = tx.orm;

            const fromAccount = await orm.public.Account.first({
                id: fromAccountId,
                userId,
            });
            const toAccount = await orm.public.Account.first({
                id: toAccountId,
                userId,
            });

            if (!fromAccount || !toAccount) {
                const error = new Error("Account not found");
                error.code = "ACCOUNT_NOT_FOUND";
                throw error;
            }

            const fromBalance = Number(fromAccount.balance);
            const toBalance = Number(toAccount.balance);

            if (!Number.isFinite(fromBalance) || !Number.isFinite(toBalance)) {
                const error = new Error("Invalid account balance");
                error.code = "INVALID_BALANCE";
                throw error;
            }

            if (fromBalance < amount) {
                const error = new Error("Insufficient funds in source account");
                error.code = "INSUFFICIENT_FUNDS";
                throw error;
            }

            await orm.public.Account
                .where({ id: fromAccountId, userId })
                .update({ balance: fromBalance - amount });

            await orm.public.Account
                .where({ id: toAccountId, userId })
                .update({ balance: toBalance + amount });

            const outgoing = await orm.public.Transaction.create({
                userId,
                accountId: fromAccountId,
                categoryId: null,
                type: "expense",
                amount,
                merchant: `Transfer to ${toAccount.name}`,
                paymentMethod: "Transfer",
                notes: notes || `Transfer to ${toAccount.name}`,
                receiptUrl: null,
                occurredAt,
            });

            const incoming = await orm.public.Transaction.create({
                userId,
                accountId: toAccountId,
                categoryId: null,
                type: "income",
                amount,
                merchant: `Transfer from ${fromAccount.name}`,
                paymentMethod: "Transfer",
                notes: notes || `Transfer from ${fromAccount.name}`,
                receiptUrl: null,
                occurredAt,
            });

            return {
                fromAccount: {
                    id: fromAccount.id,
                    name: fromAccount.name,
                    balance: fromBalance - amount,
                },
                toAccount: {
                    id: toAccount.id,
                    name: toAccount.name,
                    balance: toBalance + amount,
                },
                outgoingTransactionId: outgoing.id,
                incomingTransactionId: incoming.id,
            };
        });

        return res.status(201).json({
            success: true,
            message: "Transfer completed successfully",
            data: { transfer: result },
        });
    } catch (error) {
        if (error.code === "ACCOUNT_NOT_FOUND") {
            return res.status(404).json({ success: false, message: error.message });
        }
        if (error.code === "INSUFFICIENT_FUNDS" || error.code === "INVALID_BALANCE") {
            return res.status(400).json({ success: false, message: error.message });
        }

        console.error("Create transfer error:", error);
        return res.status(500).json({
            success: false,
            message: "Failed to complete transfer",
        });
    }
}

module.exports = { createTransfer };
