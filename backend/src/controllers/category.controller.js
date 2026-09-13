// =============================================================================
// File: category.controller.js
// Purpose: HTTP controller handlers for category.controller requests and responses.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { db } = require("../prisma/db.ts");
const {
    createCategorySchema,
} = require("../validators/category.validator");


// POST /api/categories

// -----------------------------------------------------------------------------
// Section: createCategory
// Purpose: Handles the create Category part of this backend module.
// -----------------------------------------------------------------------------
async function createCategory(req, res) {
    try {
        // Validate request data
        const result = createCategorySchema.safeParse(req.body);

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

        const { name, type, icon } = result.data;

        // Create category for authenticated user
        const category = await db.orm.public.Category.create({
            userId: req.userId,
            name,
            type,
            icon,
        });

        return res.status(201).json({
            success: true,
            message: "Category created successfully",
            data: {
                category: {
                    id: category.id,
                    name: category.name,
                    type: category.type,
                    icon: category.icon,
                    createdAt: category.createdAt,
                },
            },
        });
    } catch (error) {
        console.error("Create category error:", error);

        return res.status(500).json({
            success: false,
            message: "Something went wrong while creating the category",
        });
    }
}


// GET /api/categories

// -----------------------------------------------------------------------------
// Section: getCategories
// Purpose: Handles the get Categories part of this backend module.
// -----------------------------------------------------------------------------
async function getCategories(req, res) {
    try {
        // Get only categories belonging to authenticated user
        const categories = await db.orm.public.Category
            .where({
                userId: req.userId,
            })
            .all();

        return res.status(200).json({
            success: true,
            message: "Categories retrieved successfully",
            data: {
                categories: categories.map((category) => ({
                    id: category.id,
                    name: category.name,
                    type: category.type,
                    icon: category.icon,
                    createdAt: category.createdAt,
                })),
            },
        });
    } catch (error) {
        console.error("Get categories error:", error);

        return res.status(500).json({
            success: false,
            message: "Something went wrong while retrieving categories",
        });
    }
}


// GET /api/categories/:id

// -----------------------------------------------------------------------------
// Section: getCategoryById
// Purpose: Handles the get Category By Id part of this backend module.
// -----------------------------------------------------------------------------
async function getCategoryById(req, res) {
    try {
        const categoryId = Number(req.params.id);

        // Validate category ID
        if (!Number.isInteger(categoryId) || categoryId <= 0) {
            return res.status(400).json({
                success: false,
                message: "Invalid category ID",
            });
        }

        // Find category belonging to authenticated user
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

        return res.status(200).json({
            success: true,
            message: "Category retrieved successfully",
            data: {
                category: {
                    id: category.id,
                    name: category.name,
                    type: category.type,
                    icon: category.icon,
                    createdAt: category.createdAt,
                },
            },
        });
    } catch (error) {
        console.error("Get category error:", error);

        return res.status(500).json({
            success: false,
            message: "Something went wrong while retrieving the category",
        });
    }
}


// PUT /api/categories/:id

// -----------------------------------------------------------------------------
// Section: updateCategory
// Purpose: Handles the update Category part of this backend module.
// -----------------------------------------------------------------------------
async function updateCategory(req, res) {
    try {
        const categoryId = Number(req.params.id);

        // Validate category ID
        if (!Number.isInteger(categoryId) || categoryId <= 0) {
            return res.status(400).json({
                success: false,
                message: "Invalid category ID",
            });
        }

        // Check that the category belongs to authenticated user
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

        // Validate update data
        const result = createCategorySchema.safeParse(req.body);

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

        const { name, type, icon } = result.data;

        // Update category
        const updatedCategory = await db.orm.public.Category
            .where({
                id: categoryId,
                userId: req.userId,
            })
            .update({
                name,
                type,
                icon,
            });

        return res.status(200).json({
            success: true,
            message: "Category updated successfully",
            data: {
                category: {
                    id: updatedCategory.id,
                    name: updatedCategory.name,
                    type: updatedCategory.type,
                    icon: updatedCategory.icon,
                    createdAt: updatedCategory.createdAt,
                },
            },
        });
    } catch (error) {
        console.error("Update category error:", error);

        return res.status(500).json({
            success: false,
            message: "Something went wrong while updating the category",
        });
    }
}


// DELETE /api/categories/:id

// -----------------------------------------------------------------------------
// Section: deleteCategory
// Purpose: Handles the delete Category part of this backend module.
// -----------------------------------------------------------------------------
async function deleteCategory(req, res) {
    try {
        const categoryId = Number(req.params.id);

        // Validate category ID
        if (!Number.isInteger(categoryId) || categoryId <= 0) {
            return res.status(400).json({
                success: false,
                message: "Invalid category ID",
            });
        }

        // Check that the category belongs to authenticated user
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

        // Delete category
        await db.orm.public.Category
            .where({
                id: categoryId,
                userId: req.userId,
            })
            .delete();

        return res.status(200).json({
            success: true,
            message: "Category deleted successfully",
        });
    } catch (error) {
        console.error("Delete category error:", error);

        return res.status(500).json({
            success: false,
            message: "Something went wrong while deleting the category",
        });
    }
}


module.exports = {
    createCategory,
    getCategories,
    getCategoryById,
    updateCategory,
    deleteCategory,
};
