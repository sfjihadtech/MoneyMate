// =============================================================================
// File: backup.validator.js
// Purpose: Validation schema and request validation rules for backup.validator.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { z } = require("zod");


// ============================================================
// Reusable Schemas
// ============================================================

const idSchema =
    z.number()
        .int()
        .positive();


const nullableIdSchema =
    idSchema.nullable();


const moneySchema =
    z.union([
        z.string()
            .min(1),

        z.number()
            .finite(),
    ]);


// ============================================================
// User Schema
// ============================================================

const backupUserSchema =
    z.object({
        id:
            idSchema,

        email:
            z.string()
                .email(),

        name:
            z.string()
                .nullable()
                .optional(),

        username:
            z.string()
                .nullable()
                .optional(),

        profileImageUrl:
            z.string()
                .nullable()
                .optional(),

        currency:
            z.string()
                .min(3)
                .max(3),

        language:
            z.string()
                .min(1),

        createdAt:
            z.string()
                .min(1),

        updatedAt:
            z.string()
                .min(1),
    });


// ============================================================
// Account Schema
// ============================================================

const accountSchema =
    z.object({
        id:
            idSchema,

        userId:
            idSchema,

        name:
            z.string()
                .min(1),

        type:
            z.string()
                .min(1),

        balance:
            moneySchema,

        currency:
            z.string()
                .min(3)
                .max(3),

        createdAt:
            z.string()
                .min(1),

        updatedAt:
            z.string()
                .min(1),
    });


// ============================================================
// Category Schema
// ============================================================

const categorySchema =
    z.object({
        id:
            idSchema,

        userId:
            idSchema,

        name:
            z.string()
                .min(1),

        type:
            z.string()
                .min(1),

        icon:
            z.string()
                .nullable()
                .optional(),

        createdAt:
            z.string()
                .min(1),
    });


// ============================================================
// Transaction Schema
// ============================================================

const transactionSchema =
    z.object({
        id:
            idSchema,

        userId:
            idSchema,

        accountId:
            idSchema,

        categoryId:
            nullableIdSchema
                .optional(),

        type:
            z.enum([
                "income",
                "expense",
            ]),

        amount:
            moneySchema,

        merchant:
            z.string()
                .nullable()
                .optional(),

        paymentMethod:
            z.string()
                .nullable()
                .optional(),

        notes:
            z.string()
                .nullable()
                .optional(),

        receiptUrl:
            z.string()
                .nullable()
                .optional(),

        occurredAt:
            z.string()
                .min(1),

        createdAt:
            z.string()
                .min(1),

        updatedAt:
            z.string()
                .min(1),
    });


// ============================================================
// Budget Schema
// ============================================================

const budgetSchema =
    z.object({
        id:
            idSchema,

        userId:
            idSchema,

        categoryId:
            idSchema,

        amount:
            moneySchema,

        month:
            z.number()
                .int()
                .min(1)
                .max(12),

        year:
            z.number()
                .int()
                .min(2000)
                .max(3000),

        createdAt:
            z.string()
                .min(1),

        updatedAt:
            z.string()
                .min(1),
    });


// ============================================================
// Savings Goal Schema
// ============================================================

const savingsGoalSchema =
    z.object({
        id:
            idSchema,

        userId:
            idSchema,

        name:
            z.string()
                .min(1),

        targetAmount:
            moneySchema,

        savedAmount:
            moneySchema,

        targetDate:
            z.string()
                .nullable()
                .optional(),

        createdAt:
            z.string()
                .min(1),

        updatedAt:
            z.string()
                .min(1),
    });


// ============================================================
// Bill Schema
// ============================================================

const billSchema =
    z.object({
        id:
            idSchema,

        userId:
            idSchema,

        name:
            z.string()
                .min(1),

        amount:
            moneySchema,

        dueDate:
            z.string()
                .min(1),

        status:
            z.string()
                .min(1),

        createdAt:
            z.string()
                .min(1),

        updatedAt:
            z.string()
                .min(1),
    });


// ============================================================
// Notification Schema
// ============================================================

const notificationSchema =
    z.object({
        id:
            idSchema,

        userId:
            idSchema,

        type:
            z.string()
                .min(1),

        title:
            z.string()
                .min(1),

        message:
            z.string()
                .min(1),

        isRead:
            z.boolean(),

        relatedEntityType:
            z.string()
                .nullable()
                .optional(),

        relatedEntityId:
            z.number()
                .int()
                .positive()
                .nullable()
                .optional(),

        readAt:
            z.string()
                .nullable()
                .optional(),

        createdAt:
            z.string()
                .min(1),
    });


// ============================================================
// Metadata Counts Schema
// ============================================================

const countsSchema =
    z.object({
        accounts:
            z.number()
                .int()
                .nonnegative(),

        categories:
            z.number()
                .int()
                .nonnegative(),

        transactions:
            z.number()
                .int()
                .nonnegative(),

        budgets:
            z.number()
                .int()
                .nonnegative(),

        savingsGoals:
            z.number()
                .int()
                .nonnegative(),

        bills:
            z.number()
                .int()
                .nonnegative(),

        notifications:
            z.number()
                .int()
                .nonnegative(),
    });


// ============================================================
// Complete MoneyMate Backup Schema
// ============================================================

const backupRestoreSchema =
    z.object({
        metadata:
            z.object({
                app:
                    z.literal(
                        "MoneyMate"
                    ),

                backupVersion:
                    z.literal(1),

                exportedAt:
                    z.string()
                        .min(1),

                counts:
                    countsSchema,
            }),

        user:
            backupUserSchema,

        data:
            z.object({
                accounts:
                    z.array(
                        accountSchema
                    ),

                categories:
                    z.array(
                        categorySchema
                    ),

                transactions:
                    z.array(
                        transactionSchema
                    ),

                budgets:
                    z.array(
                        budgetSchema
                    ),

                savingsGoals:
                    z.array(
                        savingsGoalSchema
                    ),

                bills:
                    z.array(
                        billSchema
                    ),

                notifications:
                    z.array(
                        notificationSchema
                    ),
            }),
    });


// ============================================================
// Validate Backup
// ============================================================


// -----------------------------------------------------------------------------
// Section: validateBackup
// Purpose: Handles the validate Backup part of this backend module.
// -----------------------------------------------------------------------------
function validateBackup(
    backup
) {
    const result =
        backupRestoreSchema.safeParse(
            backup
        );

    if (!result.success) {
        return {
            success: false,
            errors:
                result.error.issues.map(
                    (issue) => ({
                        path:
                            issue.path.join(
                                "."
                            ),

                        message:
                            issue.message,
                    })
                ),
        };
    }


    const data =
        result.data.data;

    const expectedCounts =
        result.data.metadata.counts;


    // ========================================================
    // Verify Metadata Counts
    // ========================================================

    const actualCounts = {
        accounts:
            data.accounts.length,

        categories:
            data.categories.length,

        transactions:
            data.transactions.length,

        budgets:
            data.budgets.length,

        savingsGoals:
            data.savingsGoals.length,

        bills:
            data.bills.length,

        notifications:
            data.notifications.length,
    };


    const countMismatches =
        Object.keys(
            actualCounts
        )
            .filter(
                (key) =>
                    actualCounts[key] !==
                    expectedCounts[key]
            )
            .map(
                (key) => ({
                    field:
                        key,

                    expected:
                        expectedCounts[key],

                    actual:
                        actualCounts[key],
                })
            );


    if (
        countMismatches.length > 0
    ) {
        return {
            success: false,

            errors: [
                {
                    path:
                        "metadata.counts",

                    message:
                        "Backup record counts do not match the backup metadata.",

                    mismatches:
                        countMismatches,
                },
            ],
        };
    }


    // ========================================================
    // Valid Backup
    // ========================================================

    return {
        success: true,
        data:
            result.data,
        counts:
            actualCounts,
    };
}


// ============================================================
// Exports
// ============================================================

module.exports = {
    backupRestoreSchema,
    validateBackup,
};
