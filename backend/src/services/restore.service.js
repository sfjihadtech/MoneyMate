// =============================================================================
// File: restore.service.js
// Purpose: Business/service layer for restore.service functionality.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { db } = require("../prisma/db.ts");

const {
    validateBackup,
} = require("../validators/backup.validator");


// ============================================================
// Delete Existing User Finance Data
// ============================================================

// -----------------------------------------------------------------------------
// Section: deleteExistingUserData
// Purpose: Handles the delete Existing User Data part of this backend module.
// -----------------------------------------------------------------------------
async function deleteExistingUserData(
    orm,
    userId
) {
    // IMPORTANT:
    // delete()    = one matching record
    // deleteAll() = all matching records
    //
    // Child/dependent records must be deleted first.

    await orm.public.Notification
        .where({
            userId,
        })
        .deleteAll();


    await orm.public.Budget
        .where({
            userId,
        })
        .deleteAll();


    await orm.public.Transaction
        .where({
            userId,
        })
        .deleteAll();


    await orm.public.Bill
        .where({
            userId,
        })
        .deleteAll();


    await orm.public.SavingsGoal
        .where({
            userId,
        })
        .deleteAll();


    await orm.public.Category
        .where({
            userId,
        })
        .deleteAll();


    await orm.public.Account
        .where({
            userId,
        })
        .deleteAll();
}


// ============================================================
// Restore Accounts
// ============================================================

// -----------------------------------------------------------------------------
// Section: restoreAccounts
// Purpose: Handles the restore Accounts part of this backend module.
// -----------------------------------------------------------------------------
async function restoreAccounts(
    orm,
    userId,
    accounts
) {
    const accountIdMap =
        new Map();

    for (const account of accounts) {
        const created =
            await orm.public.Account.create({
                userId,

                name:
                    account.name,

                type:
                    account.type,

                balance:
                    account.balance,

                currency:
                    account.currency,
            });

        accountIdMap.set(
            account.id,
            created.id
        );
    }

    return accountIdMap;
}


// ============================================================
// Restore Categories
// ============================================================

// -----------------------------------------------------------------------------
// Section: restoreCategories
// Purpose: Handles the restore Categories part of this backend module.
// -----------------------------------------------------------------------------
async function restoreCategories(
    orm,
    userId,
    categories
) {
    const categoryIdMap =
        new Map();

    for (const category of categories) {
        const created =
            await orm.public.Category.create({
                userId,

                name:
                    category.name,

                type:
                    category.type,

                icon:
                    category.icon ?? null,
            });

        categoryIdMap.set(
            category.id,
            created.id
        );
    }

    return categoryIdMap;
}


// ============================================================
// Restore Transactions
// ============================================================

// -----------------------------------------------------------------------------
// Section: restoreTransactions
// Purpose: Handles the restore Transactions part of this backend module.
// -----------------------------------------------------------------------------
async function restoreTransactions(
    orm,
    userId,
    transactions,
    accountIdMap,
    categoryIdMap
) {
    const transactionIdMap =
        new Map();

    for (
        const transaction
        of transactions
    ) {
        // ====================================================
        // Resolve Account
        // ====================================================
        const newAccountId =
            accountIdMap.get(
                transaction.accountId
            );

        if (!newAccountId) {
            throw new Error(
                `Missing restored account for transaction ${transaction.id}`
            );
        }


        // ====================================================
        // Resolve Category
        // ====================================================
        let newCategoryId = null;

        if (
            transaction.categoryId !== null &&
            transaction.categoryId !== undefined
        ) {
            newCategoryId =
                categoryIdMap.get(
                    transaction.categoryId
                );

            if (!newCategoryId) {
                throw new Error(
                    `Missing restored category for transaction ${transaction.id}`
                );
            }
        }


        // ====================================================
        // Create Transaction
        // ====================================================
        const created =
            await orm.public.Transaction.create({
                userId,

                accountId:
                    newAccountId,

                categoryId:
                    newCategoryId,

                type:
                    transaction.type,

                amount:
                    transaction.amount,

                merchant:
                    transaction.merchant ??
                    null,

                paymentMethod:
                    transaction.paymentMethod ??
                    null,

                notes:
                    transaction.notes ??
                    null,

                receiptUrl:
                    transaction.receiptUrl ??
                    null,

                occurredAt:
                    transaction.occurredAt,
            });

        transactionIdMap.set(
            transaction.id,
            created.id
        );
    }

    return transactionIdMap;
}


// ============================================================
// Restore Budgets
// ============================================================

// -----------------------------------------------------------------------------
// Section: restoreBudgets
// Purpose: Handles the restore Budgets part of this backend module.
// -----------------------------------------------------------------------------
async function restoreBudgets(
    orm,
    userId,
    budgets,
    categoryIdMap
) {
    const budgetIdMap =
        new Map();

    for (const budget of budgets) {
        const newCategoryId =
            categoryIdMap.get(
                budget.categoryId
            );

        if (!newCategoryId) {
            throw new Error(
                `Missing restored category for budget ${budget.id}`
            );
        }


        const created =
            await orm.public.Budget.create({
                userId,

                categoryId:
                    newCategoryId,

                amount:
                    budget.amount,

                month:
                    budget.month,

                year:
                    budget.year,
            });

        budgetIdMap.set(
            budget.id,
            created.id
        );
    }

    return budgetIdMap;
}


// ============================================================
// Restore Savings Goals
// ============================================================

// -----------------------------------------------------------------------------
// Section: restoreSavingsGoals
// Purpose: Handles the restore Savings Goals part of this backend module.
// -----------------------------------------------------------------------------
async function restoreSavingsGoals(
    orm,
    userId,
    savingsGoals
) {
    const savingsGoalIdMap =
        new Map();

    for (
        const goal
        of savingsGoals
    ) {
        const created =
            await orm.public.SavingsGoal.create({
                userId,

                name:
                    goal.name,

                targetAmount:
                    goal.targetAmount,

                savedAmount:
                    goal.savedAmount,

                targetDate:
                    goal.targetDate ??
                    null,
            });

        savingsGoalIdMap.set(
            goal.id,
            created.id
        );
    }

    return savingsGoalIdMap;
}


// ============================================================
// Restore Bills
// ============================================================

// -----------------------------------------------------------------------------
// Section: restoreBills
// Purpose: Handles the restore Bills part of this backend module.
// -----------------------------------------------------------------------------
async function restoreBills(
    orm,
    userId,
    bills
) {
    const billIdMap =
        new Map();

    for (const bill of bills) {
        const created =
            await orm.public.Bill.create({
                userId,

                name:
                    bill.name,

                amount:
                    bill.amount,

                dueDate:
                    bill.dueDate,

                status:
                    bill.status,
            });

        billIdMap.set(
            bill.id,
            created.id
        );
    }

    return billIdMap;
}


// ============================================================
// Resolve Notification Related Entity ID
// ============================================================

// -----------------------------------------------------------------------------
// Section: resolveRelatedEntityId
// Purpose: Handles the resolve Related Entity Id part of this backend module.
// -----------------------------------------------------------------------------
function resolveRelatedEntityId(
    notification,
    maps
) {
    if (
        !notification.relatedEntityType ||
        !notification.relatedEntityId
    ) {
        return null;
    }


    switch (
        notification.relatedEntityType
    ) {
        case "account":
            return (
                maps.accountIdMap.get(
                    notification.relatedEntityId
                ) ?? null
            );

        case "category":
            return (
                maps.categoryIdMap.get(
                    notification.relatedEntityId
                ) ?? null
            );

        case "transaction":
            return (
                maps.transactionIdMap.get(
                    notification.relatedEntityId
                ) ?? null
            );

        case "budget":
            return (
                maps.budgetIdMap.get(
                    notification.relatedEntityId
                ) ?? null
            );

        case "savings_goal":
            return (
                maps.savingsGoalIdMap.get(
                    notification.relatedEntityId
                ) ?? null
            );

        case "bill":
            return (
                maps.billIdMap.get(
                    notification.relatedEntityId
                ) ?? null
            );

        default:
            return null;
    }
}


// ============================================================
// Restore Notifications
// ============================================================

// -----------------------------------------------------------------------------
// Section: restoreNotifications
// Purpose: Handles the restore Notifications part of this backend module.
// -----------------------------------------------------------------------------
async function restoreNotifications(
    orm,
    userId,
    notifications,
    maps
) {
    let restoredCount = 0;

    for (
        const notification
        of notifications
    ) {
        const relatedEntityId =
            resolveRelatedEntityId(
                notification,
                maps
            );


        await orm.public.Notification.create({
            userId,

            type:
                notification.type,

            title:
                notification.title,

            message:
                notification.message,

            isRead:
                notification.isRead,

            relatedEntityType:
                relatedEntityId
                    ? notification.relatedEntityType
                    : null,

            relatedEntityId,

            readAt:
                notification.readAt ??
                null,
        });

        restoredCount += 1;
    }

    return restoredCount;
}


// ============================================================
// Restore User Backup
// ============================================================

// -----------------------------------------------------------------------------
// Section: restoreUserBackup
// Purpose: Handles the restore User Backup part of this backend module.
// -----------------------------------------------------------------------------
async function restoreUserBackup(
    userId,
    rawBackup
) {
    // ========================================================
    // Validate Backup BEFORE Touching Database
    // ========================================================
    const validation =
        validateBackup(
            rawBackup
        );

    if (!validation.success) {
        const error =
            new Error(
                "Invalid MoneyMate backup"
            );

        error.code =
            "INVALID_BACKUP";

        error.validationErrors =
            validation.errors;

        throw error;
    }


    const backup =
        validation.data;

    const data =
        backup.data;


    // ========================================================
    // Atomic Database Transaction
    //
    // Everything below either:
    // - commits together
    // - rolls back together
    // ========================================================
    const result =
        await db.transaction(
            async (tx) => {
                const orm =
                    tx.orm;


                // ============================================
                // Verify Current User Exists
                // ============================================
                const currentUser =
                    await orm.public.User.first({
                        id: userId,
                    });

                if (!currentUser) {
                    const error =
                        new Error(
                            "User not found"
                        );

                    error.code =
                        "USER_NOT_FOUND";

                    throw error;
                }


                // ============================================
                // Delete ALL Existing Finance Data
                // ============================================
                await deleteExistingUserData(
                    orm,
                    userId
                );


                // ============================================
                // Restore Accounts
                // ============================================
                const accountIdMap =
                    await restoreAccounts(
                        orm,
                        userId,
                        data.accounts
                    );


                // ============================================
                // Restore Categories
                // ============================================
                const categoryIdMap =
                    await restoreCategories(
                        orm,
                        userId,
                        data.categories
                    );


                // ============================================
                // Restore Transactions
                // ============================================
                const transactionIdMap =
                    await restoreTransactions(
                        orm,
                        userId,
                        data.transactions,
                        accountIdMap,
                        categoryIdMap
                    );


                // ============================================
                // Restore Budgets
                // ============================================
                const budgetIdMap =
                    await restoreBudgets(
                        orm,
                        userId,
                        data.budgets,
                        categoryIdMap
                    );


                // ============================================
                // Restore Savings Goals
                // ============================================
                const savingsGoalIdMap =
                    await restoreSavingsGoals(
                        orm,
                        userId,
                        data.savingsGoals
                    );


                // ============================================
                // Restore Bills
                // ============================================
                const billIdMap =
                    await restoreBills(
                        orm,
                        userId,
                        data.bills
                    );


                // ============================================
                // Restore Notifications
                // ============================================
                const restoredNotifications =
                    await restoreNotifications(
                        orm,
                        userId,
                        data.notifications,
                        {
                            accountIdMap,
                            categoryIdMap,
                            transactionIdMap,
                            budgetIdMap,
                            savingsGoalIdMap,
                            billIdMap,
                        }
                    );


                // ============================================
                // Transaction Result
                // ============================================
                return {
                    accounts:
                        accountIdMap.size,

                    categories:
                        categoryIdMap.size,

                    transactions:
                        transactionIdMap.size,

                    budgets:
                        budgetIdMap.size,

                    savingsGoals:
                        savingsGoalIdMap.size,

                    bills:
                        billIdMap.size,

                    notifications:
                        restoredNotifications,
                };
            }
        );


    // ========================================================
    // Transaction Successfully Committed
    // ========================================================
    return {
        success: true,

        message:
            "MoneyMate backup restored successfully",

        restored:
            result,

        backup: {
            version:
                backup.metadata
                    .backupVersion,

            exportedAt:
                backup.metadata
                    .exportedAt,
        },
    };
}


// ============================================================
// Exports
// ============================================================
module.exports = {
    restoreUserBackup,
};
