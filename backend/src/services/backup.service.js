// =============================================================================
// File: backup.service.js
// Purpose: Business/service layer for backup.service functionality.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { db } = require("../prisma/db.ts");


// ============================================================
// Create User Data Backup
// ============================================================

// -----------------------------------------------------------------------------
// Section: createUserBackup
// Purpose: Handles the create User Backup part of this backend module.
// -----------------------------------------------------------------------------
async function createUserBackup(userId) {
    // ========================================================
    // Get User
    // ========================================================
    const user =
        await db.orm.public.User.first({
            id: userId,
        });

    if (!user) {
        throw new Error("User not found");
    }


    // ========================================================
    // Get Accounts
    // ========================================================
    const accounts =
        await db.orm.public.Account
            .where({
                userId,
            })
            .all();


    // ========================================================
    // Get Categories
    // ========================================================
    const categories =
        await db.orm.public.Category
            .where({
                userId,
            })
            .all();


    // ========================================================
    // Get Transactions
    // ========================================================
    const transactions =
        await db.orm.public.Transaction
            .where({
                userId,
            })
            .all();


    // ========================================================
    // Get Budgets
    // ========================================================
    const budgets =
        await db.orm.public.Budget
            .where({
                userId,
            })
            .all();


    // ========================================================
    // Get Savings Goals
    // ========================================================
    const savingsGoals =
        await db.orm.public.SavingsGoal
            .where({
                userId,
            })
            .all();


    // ========================================================
    // Get Bills
    // ========================================================
    const bills =
        await db.orm.public.Bill
            .where({
                userId,
            })
            .all();


    // ========================================================
    // Get Notifications
    // ========================================================
    const notifications =
        await db.orm.public.Notification
            .where({
                userId,
            })
            .all();


    // ========================================================
    // Safe User Profile
    //
    // IMPORTANT:
    // Do NOT export:
    //
    // passwordHash
    // password reset tokens
    // JWT tokens
    // ========================================================
    const safeUser = {
        id: user.id,
        email: user.email,
        name: user.name,
        username: user.username,
        profileImageUrl:
            user.profileImageUrl,
        currency: user.currency,
        language: user.language,
        createdAt: user.createdAt,
        updatedAt: user.updatedAt,
    };


    // ========================================================
    // Backup Metadata
    // ========================================================
    const metadata = {
        app: "MoneyMate",
        backupVersion: 1,
        exportedAt:
            new Date().toISOString(),

        counts: {
            accounts:
                accounts.length,

            categories:
                categories.length,

            transactions:
                transactions.length,

            budgets:
                budgets.length,

            savingsGoals:
                savingsGoals.length,

            bills:
                bills.length,

            notifications:
                notifications.length,
        },
    };


    // ========================================================
    // Final Backup Object
    // ========================================================
    return {
        metadata,

        user: safeUser,

        data: {
            accounts,
            categories,
            transactions,
            budgets,
            savingsGoals,
            bills,
            notifications,
        },
    };
}


// ============================================================
// Export Backup Service
// ============================================================
module.exports = {
    createUserBackup,
};
