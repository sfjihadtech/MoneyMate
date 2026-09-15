// =============================================================================
// File: transfer.controller.js
// Purpose: Handles account-to-account money transfers.
// Notes:
// - Transfers move money between the user's own accounts.
// - Transfers must NOT count as income or expense.
// - Two transaction records are kept for account history:
//   one outgoing transfer and one incoming transfer.
// =============================================================================

const { db } = require("../prisma/db.ts");

const {
    transferSchema,
} = require("../validators/transfer.validator");


// -----------------------------------------------------------------------------
// Section: createTransfer
// Purpose: Transfer money from one user-owned account to another.
// -----------------------------------------------------------------------------

async function createTransfer(req, res) {

    const validation =
        transferSchema.safeParse(req.body);

    if (!validation.success) {

        return res.status(400).json({
            success: false,
            message: "Validation failed",

            errors:
                validation.error.issues.map(
                    (issue) => ({
                        field: issue.path.join("."),
                        message: issue.message,
                    })
                ),
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


    // Prevent transferring money to the same account.
    if (fromAccountId === toAccountId) {

        return res.status(400).json({
            success: false,
            message:
                "Source and destination accounts must be different",
        });
    }


    try {

        const result =
            await db.transaction(
                async (tx) => {

                    const orm = tx.orm;


                    // ---------------------------------------------------------
                    // Load source account.
                    // ---------------------------------------------------------

                    const fromAccount =
                        await orm.public.Account.first({
                            id: fromAccountId,
                            userId,
                        });


                    // ---------------------------------------------------------
                    // Load destination account.
                    // ---------------------------------------------------------

                    const toAccount =
                        await orm.public.Account.first({
                            id: toAccountId,
                            userId,
                        });


                    // Both accounts must belong to the logged-in user.
                    if (
                        !fromAccount ||
                        !toAccount
                    ) {

                        const error =
                            new Error(
                                "Account not found"
                            );

                        error.code =
                            "ACCOUNT_NOT_FOUND";

                        throw error;
                    }


                    const fromBalance =
                        Number(
                            fromAccount.balance
                        );

                    const toBalance =
                        Number(
                            toAccount.balance
                        );


                    // Validate stored balances.
                    if (
                        !Number.isFinite(
                            fromBalance
                        ) ||
                        !Number.isFinite(
                            toBalance
                        )
                    ) {

                        const error =
                            new Error(
                                "Invalid account balance"
                            );

                        error.code =
                            "INVALID_BALANCE";

                        throw error;
                    }


                    // Source account must contain enough money.
                    if (
                        fromBalance < amount
                    ) {

                        const error =
                            new Error(
                                "Insufficient funds in source account"
                            );

                        error.code =
                            "INSUFFICIENT_FUNDS";

                        throw error;
                    }


                    // ---------------------------------------------------------
                    // Update source account balance.
                    // ---------------------------------------------------------

                    await orm.public.Account
                        .where({
                            id: fromAccountId,
                            userId,
                        })
                        .update({
                            balance:
                                fromBalance -
                                amount,
                        });


                    // ---------------------------------------------------------
                    // Update destination account balance.
                    // ---------------------------------------------------------

                    await orm.public.Account
                        .where({
                            id: toAccountId,
                            userId,
                        })
                        .update({
                            balance:
                                toBalance +
                                amount,
                        });


                    // ---------------------------------------------------------
                    // Create outgoing TRANSFER history record.
                    //
                    // IMPORTANT:
                    // This is intentionally "transfer", NOT "expense".
                    // Therefore it must not increase expense totals.
                    // ---------------------------------------------------------

                    const outgoing =
                        await orm.public.Transaction.create({

                            userId,

                            accountId:
                                fromAccountId,

                            categoryId: null,

                            type: "transfer",

                            amount,

                            merchant:
                                `Transfer to ${toAccount.name}`,

                            paymentMethod:
                                "Transfer",

                            notes:
                                notes ||
                                `Transfer to ${toAccount.name}`,

                            receiptUrl: null,

                            occurredAt,
                        });


                    // ---------------------------------------------------------
                    // Create incoming TRANSFER history record.
                    //
                    // IMPORTANT:
                    // This is intentionally "transfer", NOT "income".
                    // Therefore it must not increase income totals.
                    // ---------------------------------------------------------

                    const incoming =
                        await orm.public.Transaction.create({

                            userId,

                            accountId:
                                toAccountId,

                            categoryId: null,

                            type: "transfer",

                            amount,

                            merchant:
                                `Transfer from ${fromAccount.name}`,

                            paymentMethod:
                                "Transfer",

                            notes:
                                notes ||
                                `Transfer from ${fromAccount.name}`,

                            receiptUrl: null,

                            occurredAt,
                        });


                    // ---------------------------------------------------------
                    // Return updated account information.
                    // ---------------------------------------------------------

                    return {

                        fromAccount: {
                            id:
                                fromAccount.id,

                            name:
                                fromAccount.name,

                            balance:
                                fromBalance -
                                amount,
                        },

                        toAccount: {
                            id:
                                toAccount.id,

                            name:
                                toAccount.name,

                            balance:
                                toBalance +
                                amount,
                        },

                        outgoingTransactionId:
                            outgoing.id,

                        incomingTransactionId:
                            incoming.id,
                    };
                }
            );


        return res.status(201).json({
            success: true,

            message:
                "Transfer completed successfully",

            data: {
                transfer: result,
            },
        });


    } catch (error) {

        if (
            error.code ===
            "ACCOUNT_NOT_FOUND"
        ) {

            return res.status(404).json({
                success: false,
                message: error.message,
            });
        }


        if (
            error.code ===
                "INSUFFICIENT_FUNDS" ||
            error.code ===
                "INVALID_BALANCE"
        ) {

            return res.status(400).json({
                success: false,
                message: error.message,
            });
        }


        console.error(
            "Create transfer error:",
            error
        );


        return res.status(500).json({
            success: false,

            message:
                "Failed to complete transfer",
        });
    }
}


module.exports = {
    createTransfer,
};