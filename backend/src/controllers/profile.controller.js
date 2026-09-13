// =============================================================================
// File: profile.controller.js
// Purpose: HTTP controller handlers for profile.controller requests and responses.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const bcrypt = require("bcryptjs");
const fs = require("fs");
const path = require("path");

const { db } = require("../prisma/db.ts");

const {
    updateProfileSchema,
    changePasswordSchema,
} = require("../validators/profile.validator");


// ============================================================
// Delete Local Profile Image Helper
// ============================================================

// -----------------------------------------------------------------------------
// Section: deleteLocalProfileImage
// Purpose: Handles the delete Local Profile Image part of this backend module.
// -----------------------------------------------------------------------------
function deleteLocalProfileImage(profileImageUrl) {
    try {
        if (!profileImageUrl) {
            return;
        }

        // Only delete files from our own profile image folder
        if (!profileImageUrl.startsWith("/uploads/profile-images/")) {
            return;
        }

        const fileName = path.basename(profileImageUrl);

        const filePath = path.join(
            process.cwd(),
            "uploads",
            "profile-images",
            fileName
        );

        if (fs.existsSync(filePath)) {
            fs.unlinkSync(filePath);
        }
    } catch (error) {
        console.error(
            "Delete local profile image error:",
            error.message
        );
    }
}


// ============================================================
// Delete Newly Uploaded File Helper
// Used when database update fails
// ============================================================

// -----------------------------------------------------------------------------
// Section: deleteUploadedFile
// Purpose: Handles the delete Uploaded File part of this backend module.
// -----------------------------------------------------------------------------
function deleteUploadedFile(file) {
    try {
        if (!file || !file.path) {
            return;
        }

        if (fs.existsSync(file.path)) {
            fs.unlinkSync(file.path);
        }
    } catch (error) {
        console.error(
            "Delete uploaded file error:",
            error.message
        );
    }
}


// ============================================================
// Get Profile
// ============================================================

// -----------------------------------------------------------------------------
// Section: getProfile
// Purpose: Handles the get Profile part of this backend module.
// -----------------------------------------------------------------------------
async function getProfile(req, res) {
    try {
        const userId = req.userId;

        const user = await db.orm.public.User.first({
            id: userId,
        });

        if (!user) {
            return res.status(404).json({
                success: false,
                message: "User not found",
            });
        }

        return res.status(200).json({
            success: true,
            message: "Profile retrieved successfully",
            data: {
                user: {
                    id: user.id,
                    name: user.name,
                    username: user.username,
                    email: user.email,
                    profileImageUrl: user.profileImageUrl,
                    currency: user.currency,
                    language: user.language,
                    createdAt: user.createdAt,
                    updatedAt: user.updatedAt,
                },
            },
        });
    } catch (error) {
        console.error("Get profile error:", error);

        return res.status(500).json({
            success: false,
            message: "Failed to retrieve profile",
        });
    }
}


// ============================================================
// Update Profile
// ============================================================

// -----------------------------------------------------------------------------
// Section: updateProfile
// Purpose: Handles the update Profile part of this backend module.
// -----------------------------------------------------------------------------
async function updateProfile(req, res) {
    try {
        const userId = req.userId;

        const validation = updateProfileSchema.safeParse(
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
            username,
            currency,
            language,
        } = validation.data;

        const currentUser =
            await db.orm.public.User.first({
                id: userId,
            });

        if (!currentUser) {
            return res.status(404).json({
                success: false,
                message: "User not found",
            });
        }

        // Check username uniqueness
        if (username) {
            const existingUsername =
                await db.orm.public.User.first({
                    username,
                });

            if (
                existingUsername &&
                existingUsername.id !== userId
            ) {
                return res.status(409).json({
                    success: false,
                    message: "Username is already taken",
                });
            }
        }

        await db.orm.public.User
            .where({
                id: userId,
            })
            .update({
                name,
                username: username ?? null,
                currency,
                language,
            });

        const updatedUser =
            await db.orm.public.User.first({
                id: userId,
            });

        return res.status(200).json({
            success: true,
            message: "Profile updated successfully",
            data: {
                user: {
                    id: updatedUser.id,
                    name: updatedUser.name,
                    username: updatedUser.username,
                    email: updatedUser.email,
                    profileImageUrl:
                        updatedUser.profileImageUrl,
                    currency: updatedUser.currency,
                    language: updatedUser.language,
                    createdAt: updatedUser.createdAt,
                    updatedAt: updatedUser.updatedAt,
                },
            },
        });
    } catch (error) {
        console.error("Update profile error:", error);

        return res.status(500).json({
            success: false,
            message: "Failed to update profile",
        });
    }
}


// ============================================================
// Upload / Replace Profile Image
// ============================================================

// -----------------------------------------------------------------------------
// Section: uploadProfileImage
// Purpose: Handles the upload Profile Image part of this backend module.
// -----------------------------------------------------------------------------
async function uploadProfileImage(req, res) {
    try {
        const userId = req.userId;

        if (!req.file) {
            return res.status(400).json({
                success: false,
                message: "Profile image is required",
            });
        }

        const user = await db.orm.public.User.first({
            id: userId,
        });

        if (!user) {
            deleteUploadedFile(req.file);

            return res.status(404).json({
                success: false,
                message: "User not found",
            });
        }

        // Save relative URL in database
        const profileImageUrl =
            `/uploads/profile-images/${req.file.filename}`;

        try {
            await db.orm.public.User
                .where({
                    id: userId,
                })
                .update({
                    profileImageUrl,
                });
        } catch (error) {
            // Remove new file if database update fails
            deleteUploadedFile(req.file);

            throw error;
        }

        // Delete previous profile image after successful update
        if (
            user.profileImageUrl &&
            user.profileImageUrl !== profileImageUrl
        ) {
            deleteLocalProfileImage(
                user.profileImageUrl
            );
        }

        const updatedUser =
            await db.orm.public.User.first({
                id: userId,
            });

        return res.status(200).json({
            success: true,
            message: "Profile image uploaded successfully",
            data: {
                profileImageUrl:
                    updatedUser.profileImageUrl,

                user: {
                    id: updatedUser.id,
                    name: updatedUser.name,
                    username: updatedUser.username,
                    email: updatedUser.email,
                    profileImageUrl:
                        updatedUser.profileImageUrl,
                    currency: updatedUser.currency,
                    language: updatedUser.language,
                },
            },
        });
    } catch (error) {
        console.error(
            "Upload profile image error:",
            error
        );

        return res.status(500).json({
            success: false,
            message: "Failed to upload profile image",
        });
    }
}


// ============================================================
// Delete Profile Image
// ============================================================

// -----------------------------------------------------------------------------
// Section: deleteProfileImage
// Purpose: Handles the delete Profile Image part of this backend module.
// -----------------------------------------------------------------------------
async function deleteProfileImage(req, res) {
    try {
        const userId = req.userId;

        const user = await db.orm.public.User.first({
            id: userId,
        });

        if (!user) {
            return res.status(404).json({
                success: false,
                message: "User not found",
            });
        }

        // Already has no profile image
        if (!user.profileImageUrl) {
            return res.status(200).json({
                success: true,
                message: "Profile image already removed",
                data: {
                    profileImageUrl: null,
                },
            });
        }

        const oldProfileImageUrl =
            user.profileImageUrl;

        // Remove URL from database first
        await db.orm.public.User
            .where({
                id: userId,
            })
            .update({
                profileImageUrl: null,
            });

        // Then remove physical file
        deleteLocalProfileImage(
            oldProfileImageUrl
        );

        return res.status(200).json({
            success: true,
            message: "Profile image deleted successfully",
            data: {
                profileImageUrl: null,
            },
        });
    } catch (error) {
        console.error(
            "Delete profile image error:",
            error
        );

        return res.status(500).json({
            success: false,
            message: "Failed to delete profile image",
        });
    }
}


// ============================================================
// Change Password
// ============================================================

// -----------------------------------------------------------------------------
// Section: changePassword
// Purpose: Handles the change Password part of this backend module.
// -----------------------------------------------------------------------------
async function changePassword(req, res) {
    try {
        const userId = req.userId;

        const validation =
            changePasswordSchema.safeParse(
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
            currentPassword,
            newPassword,
        } = validation.data;

        const user = await db.orm.public.User.first({
            id: userId,
        });

        if (!user) {
            return res.status(404).json({
                success: false,
                message: "User not found",
            });
        }

        // Verify current password
        const isPasswordValid =
            await bcrypt.compare(
                currentPassword,
                user.passwordHash
            );

        if (!isPasswordValid) {
            return res.status(401).json({
                success: false,
                message: "Current password is incorrect",
            });
        }

        // Prevent using same password
        const isSamePassword =
            await bcrypt.compare(
                newPassword,
                user.passwordHash
            );

        if (isSamePassword) {
            return res.status(400).json({
                success: false,
                message:
                    "New password must be different from current password",
            });
        }

        const newPasswordHash =
            await bcrypt.hash(
                newPassword,
                12
            );

        await db.orm.public.User
            .where({
                id: userId,
            })
            .update({
                passwordHash: newPasswordHash,
            });

        return res.status(200).json({
            success: true,
            message: "Password changed successfully",
        });
    } catch (error) {
        console.error(
            "Change password error:",
            error
        );

        return res.status(500).json({
            success: false,
            message: "Failed to change password",
        });
    }
}




// ============================================================
// Delete User Account Permanently
// ============================================================

// -----------------------------------------------------------------------------
// Section: deleteUserAccount
// Purpose: Handles the delete User Account part of this backend module.
// -----------------------------------------------------------------------------
async function deleteUserAccount(req, res) {
    try {
        const userId = req.userId;
        const user = await db.orm.public.User.first({ id: userId });

        if (!user) {
            return res.status(404).json({
                success: false,
                message: "User not found",
            });
        }

        await db.transaction(async (tx) => {
            const orm = tx.orm;

            // Remove dependent data first so foreign-key constraints remain valid.
            await orm.public.Notification.where({ userId }).deleteAll();
            await orm.public.PasswordResetToken.where({ userId }).deleteAll();
            await orm.public.Budget.where({ userId }).deleteAll();
            await orm.public.Transaction.where({ userId }).deleteAll();
            await orm.public.Bill.where({ userId }).deleteAll();
            await orm.public.SavingsGoal.where({ userId }).deleteAll();
            await orm.public.Category.where({ userId }).deleteAll();
            await orm.public.Account.where({ userId }).deleteAll();
            await orm.public.User.where({ id: userId }).delete();
        });

        deleteLocalProfileImage(user.profileImageUrl);

        return res.status(200).json({
            success: true,
            message: "Account deleted permanently",
        });
    } catch (error) {
        console.error("Delete user account error:", error);
        return res.status(500).json({
            success: false,
            message: "Failed to delete account",
        });
    }
}


// ============================================================
// Export Profile Controller
// ============================================================
module.exports = {
    getProfile,
    updateProfile,
    uploadProfileImage,
    deleteProfileImage,
    changePassword,
    deleteUserAccount,
};
