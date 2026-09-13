// =============================================================================
// File: budget.controller.js
// Purpose: HTTP controller handlers for budget.controller requests and responses.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { db } = require("../prisma/db.ts");

// Budget validation schemas
const {
    createBudgetSchema,
    updateBudgetSchema,
} = require("../validators/budget.validator");


// ============================================================
// POST /api/budgets
// Create a new budget
// ============================================================

// -----------------------------------------------------------------------------
// Section: createBudget
// Purpose: Handles the create Budget part of this backend module.
// -----------------------------------------------------------------------------
async function createBudget(req, res) {
    try {

        // --------------------------------------------------------
        // 1. Validate request data
        // --------------------------------------------------------
        const result = createBudgetSchema.safeParse(req.body);

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

        const {
            categoryId,
            amount,
            month,
            year,
        } = result.data;


        // --------------------------------------------------------
        // 2. Check category belongs to authenticated user
        // --------------------------------------------------------
        const category = await db.orm.public.Category.first({
            id: categoryId,
            userId: req.userId,
        });

        if (!category) {
            return res.status(404).json({
                success: false,
                message: "Category not found",
            });
        }


        // --------------------------------------------------------
        // 3. Budget can only be created for expense categories
        // --------------------------------------------------------
        if (category.type !== "expense") {
            return res.status(400).json({
                success: false,
                message: "Budget can only be created for an expense category",
            });
        }


        // --------------------------------------------------------
        // 4. Check duplicate budget
        //    Same category + same month + same year
        // --------------------------------------------------------
        const existingBudget = await db.orm.public.Budget.first({
            userId: req.userId,
            categoryId,
            month,
            year,
        });

        if (existingBudget) {
            return res.status(409).json({
                success: false,
                message: "A budget already exists for this category and month",
            });
        }


        // --------------------------------------------------------
        // 5. Create budget
        // --------------------------------------------------------
        const budget = await db.orm.public.Budget.create({
            userId: req.userId,
            categoryId,
            amount,
            month,
            year,
        });


        // --------------------------------------------------------
        // 6. Return created budget
        // --------------------------------------------------------
        return res.status(201).json({
            success: true,
            message: "Budget created successfully",
            data: {
                budget: {
                    id: budget.id,
                    categoryId: budget.categoryId,
                    amount: budget.amount,
                    month: budget.month,
                    year: budget.year,
                    createdAt: budget.createdAt,
                    updatedAt: budget.updatedAt,
                },
            },
        });

    } catch (error) {

        // --------------------------------------------------------
        // 7. Handle unexpected server errors
        // --------------------------------------------------------
        console.error("Create budget error:", error);

        return res.status(500).json({
            success: false,
            message: "Something went wrong while creating the budget",
        });
    }
}


// ============================================================
// GET /api/budgets
// Get all budgets of authenticated user
// ============================================================

// -----------------------------------------------------------------------------
// Section: getBudgets
// Purpose: Handles the get Budgets part of this backend module.
// -----------------------------------------------------------------------------
async function getBudgets(req, res) {
    try {

        // --------------------------------------------------------
        // 1. Get only budgets belonging to logged-in user
        // --------------------------------------------------------
        const budgets = await db.orm.public.Budget
            .where({
                userId: req.userId,
            })
            .all();


        // --------------------------------------------------------
        // 2. Return budgets
        // --------------------------------------------------------
        return res.status(200).json({
            success: true,
            message: "Budgets retrieved successfully",
            data: {
                budgets: budgets.map((budget) => ({
                    id: budget.id,
                    categoryId: budget.categoryId,
                    amount: budget.amount,
                    month: budget.month,
                    year: budget.year,
                    createdAt: budget.createdAt,
                    updatedAt: budget.updatedAt,
                })),
            },
        });

    } catch (error) {

        // --------------------------------------------------------
        // 3. Handle unexpected server errors
        // --------------------------------------------------------
        console.error("Get budgets error:", error);

        return res.status(500).json({
            success: false,
            message: "Something went wrong while retrieving budgets",
        });
    }
}


// ============================================================
// GET /api/budgets/:id
// Get a single budget of authenticated user
// ============================================================

// -----------------------------------------------------------------------------
// Section: getBudgetById
// Purpose: Handles the get Budget By Id part of this backend module.
// -----------------------------------------------------------------------------
async function getBudgetById(req, res) {
    try {

        // --------------------------------------------------------
        // 1. Convert URL parameter to number
        // --------------------------------------------------------
        const budgetId = Number(req.params.id);

        if (!Number.isInteger(budgetId) || budgetId <= 0) {
            return res.status(400).json({
                success: false,
                message: "Invalid budget ID",
            });
        }


        // --------------------------------------------------------
        // 2. Find budget belonging to authenticated user
        // --------------------------------------------------------
        const budget = await db.orm.public.Budget.first({
            id: budgetId,
            userId: req.userId,
        });

        if (!budget) {
            return res.status(404).json({
                success: false,
                message: "Budget not found",
            });
        }


        // --------------------------------------------------------
        // 3. Return budget
        // --------------------------------------------------------
        return res.status(200).json({
            success: true,
            message: "Budget retrieved successfully",
            data: {
                budget: {
                    id: budget.id,
                    categoryId: budget.categoryId,
                    amount: budget.amount,
                    month: budget.month,
                    year: budget.year,
                    createdAt: budget.createdAt,
                    updatedAt: budget.updatedAt,
                },
            },
        });

    } catch (error) {

        // --------------------------------------------------------
        // 4. Handle unexpected server errors
        // --------------------------------------------------------
        console.error("Get budget by ID error:", error);

        return res.status(500).json({
            success: false,
            message: "Something went wrong while retrieving the budget",
        });
    }
}


// ============================================================
// PUT /api/budgets/:id
// Update a single budget of authenticated user
// ============================================================

// -----------------------------------------------------------------------------
// Section: updateBudget
// Purpose: Handles the update Budget part of this backend module.
// -----------------------------------------------------------------------------
async function updateBudget(req, res) {
    try {

        // --------------------------------------------------------
        // 1. Convert URL parameter to number
        // --------------------------------------------------------
        const budgetId = Number(req.params.id);

        if (!Number.isInteger(budgetId) || budgetId <= 0) {
            return res.status(400).json({
                success: false,
                message: "Invalid budget ID",
            });
        }


        // --------------------------------------------------------
        // 2. Validate request data
        // --------------------------------------------------------
        const result = updateBudgetSchema.safeParse(req.body);

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

        const {
            categoryId,
            amount,
            month,
            year,
        } = result.data;


        // --------------------------------------------------------
        // 3. Find existing budget
        //    Only the authenticated user's budget is accessible
        // --------------------------------------------------------
        const existingBudget = await db.orm.public.Budget.first({
            id: budgetId,
            userId: req.userId,
        });

        if (!existingBudget) {
            return res.status(404).json({
                success: false,
                message: "Budget not found",
            });
        }


        // --------------------------------------------------------
        // 4. Check category belongs to authenticated user
        // --------------------------------------------------------
        const category = await db.orm.public.Category.first({
            id: categoryId,
            userId: req.userId,
        });

        if (!category) {
            return res.status(404).json({
                success: false,
                message: "Category not found",
            });
        }


        // --------------------------------------------------------
        // 5. Budget can only use expense categories
        // --------------------------------------------------------
        if (category.type !== "expense") {
            return res.status(400).json({
                success: false,
                message: "Budget can only be created for an expense category",
            });
        }


        // --------------------------------------------------------
        // 6. Check duplicate budget
        //    Ignore the budget currently being updated
        // --------------------------------------------------------
        const duplicateBudget = await db.orm.public.Budget
            .where({
                userId: req.userId,
                categoryId,
                month,
                year,
            })
            .all();

        const hasDuplicate = duplicateBudget.some(
            (budget) => budget.id !== budgetId
        );

        if (hasDuplicate) {
            return res.status(409).json({
                success: false,
                message: "A budget already exists for this category and month",
            });
        }


        // --------------------------------------------------------
        // 7. Update budget
        // --------------------------------------------------------
        const updatedBudget = await db.orm.public.Budget
            .where({
                id: budgetId,
                userId: req.userId,
            })
            .update({
                categoryId,
                amount,
                month,
                year,
            });


        // --------------------------------------------------------
        // 8. Return updated budget
        // --------------------------------------------------------
        return res.status(200).json({
            success: true,
            message: "Budget updated successfully",
            data: {
                budget: {
                    id: updatedBudget.id,
                    categoryId: updatedBudget.categoryId,
                    amount: updatedBudget.amount,
                    month: updatedBudget.month,
                    year: updatedBudget.year,
                    createdAt: updatedBudget.createdAt,
                    updatedAt: updatedBudget.updatedAt,
                },
            },
        });

    } catch (error) {

        // --------------------------------------------------------
        // 9. Handle unexpected server errors
        // --------------------------------------------------------
        console.error("Update budget error:", error);

        return res.status(500).json({
            success: false,
            message: "Something went wrong while updating the budget",
        });
    }
}


// ============================================================
// DELETE /api/budgets/:id
// Delete a single budget of authenticated user
// ============================================================

// -----------------------------------------------------------------------------
// Section: deleteBudget
// Purpose: Handles the delete Budget part of this backend module.
// -----------------------------------------------------------------------------
async function deleteBudget(req, res) {
    try {

        // --------------------------------------------------------
        // 1. Convert URL parameter to number
        // --------------------------------------------------------
        const budgetId = Number(req.params.id);

        if (!Number.isInteger(budgetId) || budgetId <= 0) {
            return res.status(400).json({
                success: false,
                message: "Invalid budget ID",
            });
        }


        // --------------------------------------------------------
        // 2. Find budget belonging to authenticated user
        // --------------------------------------------------------
        const budget = await db.orm.public.Budget.first({
            id: budgetId,
            userId: req.userId,
        });

        if (!budget) {
            return res.status(404).json({
                success: false,
                message: "Budget not found",
            });
        }


        // --------------------------------------------------------
        // 3. Delete budget
        // --------------------------------------------------------
        await db.orm.public.Budget
            .where({
                id: budgetId,
                userId: req.userId,
            })
            .delete();


        // --------------------------------------------------------
        // 4. Return success response
        // --------------------------------------------------------
        return res.status(200).json({
            success: true,
            message: "Budget deleted successfully",
        });

    } catch (error) {

        // --------------------------------------------------------
        // 5. Handle unexpected server errors
        // --------------------------------------------------------
        console.error("Delete budget error:", error);

        return res.status(500).json({
            success: false,
            message: "Something went wrong while deleting the budget",
        });
    }
}



 // ============================================================
 // Export budget controller functions
 // ============================================================
 module.exports = {
     createBudget,
     getBudgets,
     getBudgetById,
     updateBudget,
     deleteBudget,
 };
