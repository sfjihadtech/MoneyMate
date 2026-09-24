// =============================================================================
// File: transaction.controller.js
// Purpose: Handles income and expense transaction API requests.
// Notes:
// - Normal transactions may be "income" or "expense".
// - Account transfers are created by transfer.controller.js.
// - Transfer history entries must NOT be treated as income/expense.
// =============================================================================

const { db } = require("../prisma/db.ts");

const {
    createTransactionSchema,
} = require("../validators/transaction.validator");

const {
    evaluateBudgetsForTransaction,
} = require("../services/budgetNotification.service");


// -----------------------------------------------------------------------------
// Section: getBalanceEffect
// Purpose: Calculate how a NEW normal transaction affects an account balance.
// -----------------------------------------------------------------------------

function getBalanceEffect(type, amount) {

    const numericAmount = Number(amount);

    if (type === "income") {
        return numericAmount;
    }

    if (type === "expense") {
        return -numericAmount;
    }

    // Transfers are handled separately by transfer.controller.js.
    return 0;
}


// -----------------------------------------------------------------------------
// Section: getReverseBalanceEffect
// Purpose: Reverse a normal transaction's previous balance effect.
// -----------------------------------------------------------------------------

function getReverseBalanceEffect(type, amount) {

    const numericAmount = Number(amount);

    if (type === "income") {
        return -numericAmount;
    }

    if (type === "expense") {
        return numericAmount;
    }

    // Never alter account balance for transfer history here.
    return 0;
}


// -----------------------------------------------------------------------------
// Section: safelyEvaluateBudget
// Purpose: Recalculate budget notifications without breaking the request.
// -----------------------------------------------------------------------------

async function safelyEvaluateBudget({
    userId,
    categoryId,
    occurredAt,
}) {

    try {

        if (!categoryId || !occurredAt) {
            return;
        }

        await evaluateBudgetsForTransaction({
            userId,
            categoryId,
            occurredAt,
        });

    } catch (error) {

        console.error(
            "Budget notification evaluation error:",
            error
        );
    }
}


// -----------------------------------------------------------------------------
// Section: serializeTransaction
// Purpose: Convert database transaction into API response format.
// -----------------------------------------------------------------------------

function serializeTransaction(transaction) {

    return {
        id: transaction.id,
        accountId: transaction.accountId,
        categoryId: transaction.categoryId,
        type: transaction.type,
        transferGroupId: transaction.transferGroupId,
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
// Purpose: Create a normal income or expense transaction.
// -----------------------------------------------------------------------------

async function createTransaction(req, res) {

    try {

        const userId = req.userId;

        const validation =
            createTransactionSchema.safeParse(req.body);

        if (!validation.success) {

            return res.status(400).json({
                success: false,
                message: "Validation failed",
                errors:
                    validation.error.issues.map(
                        (issue) => issue.message
                    ),
            });
        }


        const body = validation.data;


        const transaction =
            await db.transaction(async (tx) => {

                const orm = tx.orm;


                const account =
                    await orm.public.Account.first({
                        id: body.accountId,
                        userId,
                    });


                if (!account) {

                    const error =
                        new Error("Account not found");

                    error.code =
                        "ACCOUNT_NOT_FOUND";

                    throw error;
                }


                if (body.categoryId) {

                    const category =
                        await orm.public.Category.first({
                            id: body.categoryId,
                            userId,
                        });


                    if (!category) {

                        const error =
                            new Error(
                                "Category not found"
                            );

                        error.code =
                            "CATEGORY_NOT_FOUND";

                        throw error;
                    }
                }


                const created =
                    await orm.public.Transaction.create({

                        userId,

                        accountId:
                            body.accountId,

                        categoryId:
                            body.categoryId ?? null,

                        type:
                            body.type,

                        amount:
                            body.amount,

                        merchant:
                            body.merchant ?? null,

                        paymentMethod:
                            body.paymentMethod ?? null,

                        notes:
                            body.notes ?? null,

                        receiptUrl:
                            body.receiptUrl ?? null,

                        occurredAt:
                            body.occurredAt,
                    });


                const currentBalance =
                    Number(account.balance);


                if (
                    !Number.isFinite(
                        currentBalance
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


                await orm.public.Account
                    .where({
                        id: body.accountId,
                        userId,
                    })
                    .update({
                        balance:
                            currentBalance +
                            getBalanceEffect(
                                body.type,
                                body.amount
                            ),
                    });


                return created;
            });


        await safelyEvaluateBudget({
            userId,
            categoryId: body.categoryId,
            occurredAt: body.occurredAt,
        });


        return res.status(201).json({
            success: true,
            message:
                "Transaction created successfully",
            data: {
                transaction:
                    serializeTransaction(
                        transaction
                    ),
            },
        });


    } catch (error) {

        if (
            error.code ===
                "ACCOUNT_NOT_FOUND" ||
            error.code ===
                "CATEGORY_NOT_FOUND"
        ) {

            return res.status(404).json({
                success: false,
                message: error.message,
            });
        }


        if (
            error.code ===
            "INVALID_BALANCE"
        ) {

            return res.status(400).json({
                success: false,
                message: error.message,
            });
        }


        console.error(
            "Create transaction error:",
            error
        );


        return res.status(500).json({
            success: false,
            message:
                "Failed to create transaction",
        });
    }
}


// -----------------------------------------------------------------------------
// Section: getTransactions
// Purpose: Retrieve all transactions including transfer history.
// -----------------------------------------------------------------------------

async function getTransactions(req, res) {

    try {

        const userId = req.userId;


        const transactions =
            await db.orm.public.Transaction
                .where({ userId })
                .all();


                const accounts =
                    await db.orm.public.Account
                        .where({ userId })
                        .all();

                const accountsById =
                    new Map(
                        accounts.map(
                            (account) => [
                                account.id,
                                account,
                            ]
                        )
                    );



                    const transferGroups = new Map();

                    for (const transaction of transactions) {

                        if (
                            transaction.type === "transfer" &&
                            transaction.transferGroupId
                        ) {

                            const group =
                                transferGroups.get(
                                    transaction.transferGroupId
                                ) || [];

                            group.push(transaction);

                            transferGroups.set(
                                transaction.transferGroupId,
                                group
                            );
                        }
                    }


        transactions.sort(
            (a, b) =>
                new Date(b.occurredAt) -
                new Date(a.occurredAt)
        );




        const serializedTransactions = [];
        const handledTransferGroups = new Set();

        for (const transaction of transactions) {

            if (
                transaction.type !== "transfer" ||
                !transaction.transferGroupId
            ) {
                serializedTransactions.push(
                    serializeTransaction(transaction)
                );
                continue;
            }

            if (
                handledTransferGroups.has(
                    transaction.transferGroupId
                )
            ) {
                continue;
            }

            const pair =
                transferGroups.get(
                    transaction.transferGroupId
                ) || [];

            const outgoing =
                pair.find((item) =>
                    item.merchant?.startsWith("Transfer to ")
                );

            const incoming =
                pair.find((item) =>
                    item.merchant?.startsWith("Transfer from ")
                );

            if (!outgoing || !incoming) {
                serializedTransactions.push(
                    serializeTransaction(transaction)
                );
                continue;
            }

            const fromAccount =
                accountsById.get(outgoing.accountId);

            const toAccount =
                accountsById.get(incoming.accountId);

            serializedTransactions.push({
                ...serializeTransaction(outgoing),

                fromAccountId:
                    fromAccount?.id ?? outgoing.accountId,

                fromAccountName:
                    fromAccount?.name ?? "Unknown Account",

                fromAccountType:
                    fromAccount?.type ?? null,

                toAccountId:
                    toAccount?.id ?? incoming.accountId,

                toAccountName:
                    toAccount?.name ?? "Unknown Account",

                toAccountType:
                    toAccount?.type ?? null,
            });

            handledTransferGroups.add(
                transaction.transferGroupId
            );
        }




        return res.status(200).json({
            success: true,
            message:
                "Transactions retrieved successfully",
            data: {
                transactions:
                    serializedTransactions,
            },
        });


    } catch (error) {

        console.error(
            "Get transactions error:",
            error
        );


        return res.status(500).json({
            success: false,
            message:
                "Failed to retrieve transactions",
        });
    }
}


// -----------------------------------------------------------------------------
// Section: getTransactionById
// Purpose: Retrieve a single transaction.
// -----------------------------------------------------------------------------

async function getTransactionById(req, res) {

    try {

        const userId = req.userId;

        const transactionId =
            Number(req.params.id);


        if (
            !Number.isInteger(
                transactionId
            ) ||
            transactionId <= 0
        ) {

            return res.status(400).json({
                success: false,
                message:
                    "Invalid transaction ID",
            });
        }


        const transaction =
            await db.orm.public.Transaction.first({
                id: transactionId,
                userId,
            });


        if (!transaction) {

            return res.status(404).json({
                success: false,
                message:
                    "Transaction not found",
            });
        }


        return res.status(200).json({
            success: true,
            message:
                "Transaction retrieved successfully",
            data: {
                transaction:
                    serializeTransaction(
                        transaction
                    ),
            },
        });


    } catch (error) {

        console.error(
            "Get transaction error:",
            error
        );


        return res.status(500).json({
            success: false,
            message:
                "Failed to retrieve transaction",
        });
    }
}


// -----------------------------------------------------------------------------
// Section: updateTransaction
// Purpose: Update a normal income/expense transaction.
// Transfer records cannot be edited using this endpoint.
// -----------------------------------------------------------------------------

async function updateTransaction(req, res) {

    try {

        const userId = req.userId;

        const transactionId =
            Number(req.params.id);


        if (
            !Number.isInteger(
                transactionId
            ) ||
            transactionId <= 0
        ) {

            return res.status(400).json({
                success: false,
                message:
                    "Invalid transaction ID",
            });
        }


        const validation =
            createTransactionSchema.safeParse(req.body);


        if (!validation.success) {

            return res.status(400).json({
                success: false,
                message: "Validation failed",
                errors:
                    validation.error.issues.map(
                        (issue) => issue.message
                    ),
            });
        }


        const body = validation.data;

        let oldBudgetInfo = null;


        const updatedTransaction =
            await db.transaction(async (tx) => {

                const orm = tx.orm;


                const existing =
                    await orm.public.Transaction.first({
                        id: transactionId,
                        userId,
                    });


                if (!existing) {

                    const error =
                        new Error(
                            "Transaction not found"
                        );

                    error.code =
                        "TRANSACTION_NOT_FOUND";

                    throw error;
                }


                // Transfer transactions are paired records.
                // Editing one side would corrupt transfer history.
                if (
                    existing.type === "transfer"
                ) {

                    const error =
                        new Error(
                            "Transfer transactions cannot be edited individually"
                        );

                    error.code =
                        "TRANSFER_PROTECTED";

                    throw error;
                }


                const oldAccount =
                    await orm.public.Account.first({
                        id: existing.accountId,
                        userId,
                    });


                const newAccount =
                    await orm.public.Account.first({
                        id: body.accountId,
                        userId,
                    });


                if (
                    !oldAccount ||
                    !newAccount
                ) {

                    const error =
                        new Error(
                            "Account not found"
                        );

                    error.code =
                        "ACCOUNT_NOT_FOUND";

                    throw error;
                }


                if (body.categoryId) {

                    const category =
                        await orm.public.Category.first({
                            id: body.categoryId,
                            userId,
                        });


                    if (!category) {

                        const error =
                            new Error(
                                "Category not found"
                            );

                        error.code =
                            "CATEGORY_NOT_FOUND";

                        throw error;
                    }
                }


                const oldBalance =
                    Number(
                        oldAccount.balance
                    );


                if (
                    !Number.isFinite(
                        oldBalance
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


                // Reverse old transaction effect.
                await orm.public.Account
                    .where({
                        id: existing.accountId,
                        userId,
                    })
                    .update({
                        balance:
                            oldBalance +
                            getReverseBalanceEffect(
                                existing.type,
                                existing.amount
                            ),
                    });


                // Update transaction record.
                await orm.public.Transaction
                    .where({
                        id: transactionId,
                        userId,
                    })
                    .update({

                        accountId:
                            body.accountId,

                        categoryId:
                            body.categoryId ?? null,

                        type:
                            body.type,

                        amount:
                            body.amount,

                        merchant:
                            body.merchant ?? null,

                        paymentMethod:
                            body.paymentMethod ?? null,

                        notes:
                            body.notes ?? null,

                        receiptUrl:
                            body.receiptUrl ?? null,

                        occurredAt:
                            body.occurredAt,
                    });


                const currentNewAccount =
                    await orm.public.Account.first({
                        id: body.accountId,
                        userId,
                    });


                const currentNewBalance =
                    Number(
                        currentNewAccount?.balance
                    );


                if (
                    !Number.isFinite(
                        currentNewBalance
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


                // Apply new transaction effect.
                await orm.public.Account
                    .where({
                        id: body.accountId,
                        userId,
                    })
                    .update({
                        balance:
                            currentNewBalance +
                            getBalanceEffect(
                                body.type,
                                body.amount
                            ),
                    });


                oldBudgetInfo = {
                    categoryId:
                        existing.categoryId,
                    occurredAt:
                        existing.occurredAt,
                };


                return orm.public.Transaction.first({
                    id: transactionId,
                    userId,
                });
            });


        if (oldBudgetInfo) {

            await safelyEvaluateBudget({
                userId,
                ...oldBudgetInfo,
            });
        }


        await safelyEvaluateBudget({
            userId,
            categoryId:
                body.categoryId,
            occurredAt:
                body.occurredAt,
        });


        return res.status(200).json({
            success: true,
            message:
                "Transaction updated successfully",
            data: {
                transaction:
                    serializeTransaction(
                        updatedTransaction
                    ),
            },
        });


    } catch (error) {

        if (
            [
                "TRANSACTION_NOT_FOUND",
                "ACCOUNT_NOT_FOUND",
                "CATEGORY_NOT_FOUND",
            ].includes(error.code)
        ) {

            return res.status(404).json({
                success: false,
                message: error.message,
            });
        }


        if (
            error.code ===
            "TRANSFER_PROTECTED"
        ) {

            return res.status(400).json({
                success: false,
                message: error.message,
            });
        }


        if (
            error.code ===
            "INVALID_BALANCE"
        ) {

            return res.status(400).json({
                success: false,
                message: error.message,
            });
        }


        console.error(
            "Update transaction error:",
            error
        );


        return res.status(500).json({
            success: false,
            message:
                "Failed to update transaction",
        });
    }
}


// -----------------------------------------------------------------------------
// Section: deleteTransaction
// Purpose: Delete a normal income/expense transaction.
// Transfer records cannot be deleted individually.
// -----------------------------------------------------------------------------

async function deleteTransaction(req, res) {

    try {

        const userId = req.userId;

        const transactionId =
            Number(req.params.id);


        if (
            !Number.isInteger(
                transactionId
            ) ||
            transactionId <= 0
        ) {

            return res.status(400).json({
                success: false,
                message:
                    "Invalid transaction ID",
            });
        }


        let budgetInfo = null;


        await db.transaction(async (tx) => {

            const orm = tx.orm;


            const transaction =
                await orm.public.Transaction.first({
                    id: transactionId,
                    userId,
                });


            if (!transaction) {

                const error =
                    new Error(
                        "Transaction not found"
                    );

                error.code =
                    "TRANSACTION_NOT_FOUND";

                throw error;
            }


            // A transfer has two linked history entries.
            // Never delete only one side through this endpoint.
            if (
                transaction.type === "transfer"
            ) {

                const error =
                    new Error(
                        "Transfer transactions cannot be deleted individually"
                    );

                error.code =
                    "TRANSFER_PROTECTED";

                throw error;
            }


            const account =
                await orm.public.Account.first({
                    id: transaction.accountId,
                    userId,
                });


            if (!account) {

                const error =
                    new Error(
                        "Account not found"
                    );

                error.code =
                    "ACCOUNT_NOT_FOUND";

                throw error;
            }


            const currentBalance =
                Number(account.balance);


            if (
                !Number.isFinite(
                    currentBalance
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


            // Reverse normal income/expense effect.
            await orm.public.Account
                .where({
                    id: account.id,
                    userId,
                })
                .update({
                    balance:
                        currentBalance +
                        getReverseBalanceEffect(
                            transaction.type,
                            transaction.amount
                        ),
                });


            await orm.public.Transaction
                .where({
                    id: transactionId,
                    userId,
                })
                .delete();


            budgetInfo = {
                categoryId:
                    transaction.categoryId,
                occurredAt:
                    transaction.occurredAt,
            };
        });


        if (budgetInfo) {

            await safelyEvaluateBudget({
                userId,
                ...budgetInfo,
            });
        }


        return res.status(200).json({
            success: true,
            message:
                "Transaction deleted successfully",
        });


    } catch (error) {

        if (
            error.code ===
                "TRANSACTION_NOT_FOUND" ||
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
            "TRANSFER_PROTECTED"
        ) {

            return res.status(400).json({
                success: false,
                message: error.message,
            });
        }


        if (
            error.code ===
            "INVALID_BALANCE"
        ) {

            return res.status(400).json({
                success: false,
                message: error.message,
            });
        }


        console.error(
            "Delete transaction error:",
            error
        );


        return res.status(500).json({
            success: false,
            message:
                "Failed to delete transaction",
        });
    }
}


module.exports = {
    createTransaction,
    getTransactions,
    getTransactionById,
    updateTransaction,
    deleteTransaction,
};
