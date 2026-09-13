// =============================================================================
// File: savingsGoal.controller.js
// Purpose: HTTP controller handlers for savings Goal.controller requests and responses.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { db } = require("../prisma/db.ts");

const {
    createSavingsGoalSchema,
    updateSavingsGoalSchema,
    savingsGoalContributionSchema,
} = require("../validators/savingsGoal.validator");

const {
    createSavingsProgressNotification,
} = require("../services/notification.service");


// ============================================================
// Savings Notification Milestones
// ============================================================
const SAVINGS_MILESTONES = [
    25,
    50,
    75,
    100,
];


// ============================================================
// Get Reached Savings Milestone
// ============================================================

// -----------------------------------------------------------------------------
// Section: getReachedMilestone
// Purpose: Handles the get Reached Milestone part of this backend module.
// -----------------------------------------------------------------------------
function getReachedMilestone(percentage) {
    let reachedMilestone = null;

    for (const milestone of SAVINGS_MILESTONES) {
        if (percentage >= milestone) {
            reachedMilestone = milestone;
        }
    }

    return reachedMilestone;
}


// ============================================================
// Remove Savings Progress Notifications
// ============================================================

// -----------------------------------------------------------------------------
// Section: removeSavingsProgressNotifications
// Purpose: Handles the remove Savings Progress Notifications part of this backend module.
// -----------------------------------------------------------------------------
async function removeSavingsProgressNotifications(
    userId,
    goalId
) {
    const notifications =
        await db.orm.public.Notification
            .where({
                userId,
                type: "savings_progress",
            })
            .all();

    const matchingNotifications =
        notifications.filter(
            (notification) =>
                notification.relatedEntityType ===
                    "savings_goal" &&
                notification.relatedEntityId ===
                    goalId
        );

    for (const notification of matchingNotifications) {
        await db.orm.public.Notification
            .where({
                id: notification.id,
                userId,
            })
            .delete();
    }
}


// ============================================================
// Evaluate Savings Progress Notification
// ============================================================

// -----------------------------------------------------------------------------
// Section: evaluateSavingsProgressNotification
// Purpose: Handles the evaluate Savings Progress Notification part of this backend module.
// -----------------------------------------------------------------------------
async function evaluateSavingsProgressNotification(
    userId,
    goal
) {
    const targetAmount =
        Number(goal.targetAmount);

    const savedAmount =
        Number(goal.savedAmount);

    const percentage =
        targetAmount > 0
            ? Number(
                  (
                      (savedAmount /
                          targetAmount) *
                      100
                  ).toFixed(2)
              )
            : 0;

    const milestone =
        getReachedMilestone(percentage);

    // Below first milestone
    if (!milestone) {
        await removeSavingsProgressNotifications(
            userId,
            goal.id
        );

        return;
    }

    await createSavingsProgressNotification({
        userId,
        goal,
        percentage: milestone,
    });
}


// ============================================================
// Create Savings Goal
// ============================================================

// -----------------------------------------------------------------------------
// Section: createSavingsGoal
// Purpose: Handles the create Savings Goal part of this backend module.
// -----------------------------------------------------------------------------
async function createSavingsGoal(req, res) {
    try {
        const userId = req.userId;

        const validation =
            createSavingsGoalSchema.safeParse(
                req.body
            );

        if (!validation.success) {
            return res.status(400).json({
                success: false,
                message: "Validation failed",
                errors: validation.error.issues.map(
                    (issue) => issue.message
                ),
            });
        }

        const {
            name,
            targetAmount,
            targetDate,
        } = validation.data;

        const savingsGoal =
            await db.orm.public.SavingsGoal.create({
                userId,
                name,
                targetAmount,
                savedAmount: 0,
                targetDate:
                    targetDate ?? null,
            });

        return res.status(201).json({
            success: true,
            message:
                "Savings goal created successfully",
            data: {
                savingsGoal: {
                    id: savingsGoal.id,
                    name: savingsGoal.name,
                    targetAmount:
                        savingsGoal.targetAmount,
                    savedAmount:
                        savingsGoal.savedAmount,
                    targetDate:
                        savingsGoal.targetDate,
                    createdAt:
                        savingsGoal.createdAt,
                    updatedAt:
                        savingsGoal.updatedAt,
                },
            },
        });
    } catch (error) {
        console.error(
            "Create savings goal error:",
            error
        );

        return res.status(500).json({
            success: false,
            message:
                "Failed to create savings goal",
        });
    }
}


// ============================================================
// Get Savings Goals
// ============================================================

// -----------------------------------------------------------------------------
// Section: getSavingsGoals
// Purpose: Handles the get Savings Goals part of this backend module.
// -----------------------------------------------------------------------------
async function getSavingsGoals(req, res) {
    try {
        const userId = req.userId;

        const savingsGoals =
            await db.orm.public.SavingsGoal
                .where({
                    userId,
                })
                .all();

        return res.status(200).json({
            success: true,
            message:
                "Savings goals retrieved successfully",
            data: {
                savingsGoals:
                    savingsGoals.map(
                        (goal) => ({
                            id: goal.id,
                            name: goal.name,
                            targetAmount:
                                goal.targetAmount,
                            savedAmount:
                                goal.savedAmount,
                            targetDate:
                                goal.targetDate,
                            createdAt:
                                goal.createdAt,
                            updatedAt:
                                goal.updatedAt,
                        })
                    ),
            },
        });
    } catch (error) {
        console.error(
            "Get savings goals error:",
            error
        );

        return res.status(500).json({
            success: false,
            message:
                "Failed to retrieve savings goals",
        });
    }
}


// ============================================================
// Get Savings Goal By ID
// ============================================================

// -----------------------------------------------------------------------------
// Section: getSavingsGoalById
// Purpose: Handles the get Savings Goal By Id part of this backend module.
// -----------------------------------------------------------------------------
async function getSavingsGoalById(req, res) {
    try {
        const userId = req.userId;

        const goalId =
            Number(req.params.id);

        if (
            !Number.isInteger(goalId) ||
            goalId <= 0
        ) {
            return res.status(400).json({
                success: false,
                message:
                    "Invalid savings goal ID",
            });
        }

        const goal =
            await db.orm.public.SavingsGoal.first({
                id: goalId,
                userId,
            });

        if (!goal) {
            return res.status(404).json({
                success: false,
                message:
                    "Savings goal not found",
            });
        }

        const targetAmount =
            Number(goal.targetAmount);

        const savedAmount =
            Number(goal.savedAmount);

        const progressPercentage =
            targetAmount > 0
                ? Number(
                      (
                          (savedAmount /
                              targetAmount) *
                          100
                      ).toFixed(2)
                  )
                : 0;

        return res.status(200).json({
            success: true,
            message:
                "Savings goal retrieved successfully",
            data: {
                savingsGoal: {
                    id: goal.id,
                    name: goal.name,
                    targetAmount:
                        goal.targetAmount,
                    savedAmount:
                        goal.savedAmount,
                    targetDate:
                        goal.targetDate,
                    progressPercentage,
                    remainingAmount:
                        Math.max(
                            targetAmount -
                                savedAmount,
                            0
                        ),
                    createdAt:
                        goal.createdAt,
                    updatedAt:
                        goal.updatedAt,
                },
            },
        });
    } catch (error) {
        console.error(
            "Get savings goal error:",
            error
        );

        return res.status(500).json({
            success: false,
            message:
                "Failed to retrieve savings goal",
        });
    }
}


// ============================================================
// Update Savings Goal
// ============================================================

// -----------------------------------------------------------------------------
// Section: updateSavingsGoal
// Purpose: Handles the update Savings Goal part of this backend module.
// -----------------------------------------------------------------------------
async function updateSavingsGoal(req, res) {
    try {
        const userId = req.userId;

        const goalId =
            Number(req.params.id);

        if (
            !Number.isInteger(goalId) ||
            goalId <= 0
        ) {
            return res.status(400).json({
                success: false,
                message:
                    "Invalid savings goal ID",
            });
        }

        const validation =
            updateSavingsGoalSchema.safeParse(
                req.body
            );

        if (!validation.success) {
            return res.status(400).json({
                success: false,
                message: "Validation failed",
                errors: validation.error.issues.map(
                    (issue) => issue.message
                ),
            });
        }

        const existingGoal =
            await db.orm.public.SavingsGoal.first({
                id: goalId,
                userId,
            });

        if (!existingGoal) {
            return res.status(404).json({
                success: false,
                message:
                    "Savings goal not found",
            });
        }

        const {
            name,
            targetAmount,
            targetDate,
        } = validation.data;

        await db.orm.public.SavingsGoal
            .where({
                id: goalId,
                userId,
            })
            .update({
                name,
                targetAmount,
                targetDate:
                    targetDate ?? null,
            });

        const updatedGoal =
            await db.orm.public.SavingsGoal.first({
                id: goalId,
                userId,
            });

        // Recalculate notification if target changes
        try {
            await evaluateSavingsProgressNotification(
                userId,
                updatedGoal
            );
        } catch (notificationError) {
            console.error(
                "Savings notification error:",
                notificationError
            );
        }

        return res.status(200).json({
            success: true,
            message:
                "Savings goal updated successfully",
            data: {
                savingsGoal: {
                    id: updatedGoal.id,
                    name: updatedGoal.name,
                    targetAmount:
                        updatedGoal.targetAmount,
                    savedAmount:
                        updatedGoal.savedAmount,
                    targetDate:
                        updatedGoal.targetDate,
                    createdAt:
                        updatedGoal.createdAt,
                    updatedAt:
                        updatedGoal.updatedAt,
                },
            },
        });
    } catch (error) {
        console.error(
            "Update savings goal error:",
            error
        );

        return res.status(500).json({
            success: false,
            message:
                "Failed to update savings goal",
        });
    }
}


// ============================================================
// Add Contribution to Savings Goal
// ============================================================

// -----------------------------------------------------------------------------
// Section: addSavingsGoalContribution
// Purpose: Handles the add Savings Goal Contribution part of this backend module.
// -----------------------------------------------------------------------------
async function addSavingsGoalContribution(
    req,
    res
) {
    try {
        const userId = req.userId;

        const goalId =
            Number(req.params.id);

        if (
            !Number.isInteger(goalId) ||
            goalId <= 0
        ) {
            return res.status(400).json({
                success: false,
                message:
                    "Invalid savings goal ID",
            });
        }

        const validation =
            savingsGoalContributionSchema.safeParse(
                req.body
            );

        if (!validation.success) {
            return res.status(400).json({
                success: false,
                message: "Validation failed",
                errors: validation.error.issues.map(
                    (issue) => issue.message
                ),
            });
        }

        const { amount } =
            validation.data;

        const goal =
            await db.orm.public.SavingsGoal.first({
                id: goalId,
                userId,
            });

        if (!goal) {
            return res.status(404).json({
                success: false,
                message:
                    "Savings goal not found",
            });
        }

        const currentSavedAmount =
            Number(goal.savedAmount);

        const newSavedAmount =
            currentSavedAmount +
            Number(amount);

        await db.orm.public.SavingsGoal
            .where({
                id: goalId,
                userId,
            })
            .update({
                savedAmount:
                    newSavedAmount,
            });

        const updatedGoal =
            await db.orm.public.SavingsGoal.first({
                id: goalId,
                userId,
            });

        // Automatic savings progress notification
        try {
            await evaluateSavingsProgressNotification(
                userId,
                updatedGoal
            );
        } catch (notificationError) {
            console.error(
                "Savings notification error:",
                notificationError
            );
        }

        const targetAmount =
            Number(
                updatedGoal.targetAmount
            );

        const savedAmount =
            Number(
                updatedGoal.savedAmount
            );

        const progressPercentage =
            targetAmount > 0
                ? Number(
                      (
                          (savedAmount /
                              targetAmount) *
                          100
                      ).toFixed(2)
                  )
                : 0;

        return res.status(200).json({
            success: true,
            message:
                "Savings contribution added successfully",
            data: {
                goal: {
                    id: updatedGoal.id,
                    name: updatedGoal.name,
                    targetAmount:
                        updatedGoal.targetAmount,
                    savedAmount:
                        updatedGoal.savedAmount,
                    targetDate:
                        updatedGoal.targetDate,
                    progressPercentage,
                    remainingAmount:
                        Math.max(
                            targetAmount -
                                savedAmount,
                            0
                        ),
                },
            },
        });
    } catch (error) {
        console.error(
            "Add savings contribution error:",
            error
        );

        return res.status(500).json({
            success: false,
            message:
                "Failed to add savings contribution",
        });
    }
}


// ============================================================
// Delete Savings Goal
// ============================================================

// -----------------------------------------------------------------------------
// Section: deleteSavingsGoal
// Purpose: Handles the delete Savings Goal part of this backend module.
// -----------------------------------------------------------------------------
async function deleteSavingsGoal(req, res) {
    try {
        const userId = req.userId;

        const goalId =
            Number(req.params.id);

        if (
            !Number.isInteger(goalId) ||
            goalId <= 0
        ) {
            return res.status(400).json({
                success: false,
                message:
                    "Invalid savings goal ID",
            });
        }

        const goal =
            await db.orm.public.SavingsGoal.first({
                id: goalId,
                userId,
            });

        if (!goal) {
            return res.status(404).json({
                success: false,
                message:
                    "Savings goal not found",
            });
        }

        // Remove related notifications
        try {
            await removeSavingsProgressNotifications(
                userId,
                goalId
            );
        } catch (notificationError) {
            console.error(
                "Delete savings notification error:",
                notificationError
            );
        }

        await db.orm.public.SavingsGoal
            .where({
                id: goalId,
                userId,
            })
            .delete();

        return res.status(200).json({
            success: true,
            message:
                "Savings goal deleted successfully",
        });
    } catch (error) {
        console.error(
            "Delete savings goal error:",
            error
        );

        return res.status(500).json({
            success: false,
            message:
                "Failed to delete savings goal",
        });
    }
}


// ============================================================
// Export Savings Goal Controller
// ============================================================
module.exports = {
    createSavingsGoal,
    getSavingsGoals,
    getSavingsGoalById,
    updateSavingsGoal,
    addSavingsGoalContribution,
    deleteSavingsGoal,
};
