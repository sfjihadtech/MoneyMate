// =============================================================================
// File: profile.controller.js
// Purpose: HTTP controller handlers for profile.controller requests and responses.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const bcrypt = require("bcryptjs");
const fs = require("fs");
const path = require("path");

const { v2: cloudinary } = require("cloudinary");

// Use CLOUDINARY_URL from Render environment variables.
cloudinary.config({
    secure: true,
});

const { db } = require("../prisma/db.ts");
const { pool } = require("../prisma/pg");

const {
    updateProfileSchema,
    changePasswordSchema,
    changeEmailSchema,
    deleteAccountSchema,
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
// Upload Profile Image to Cloudinary
// ============================================================

function uploadProfileImageToCloudinary(file, userId) {
    return new Promise((resolve, reject) => {
        const uploadStream = cloudinary.uploader.upload_stream(
            {
                folder: "moneymate/profile-images",
                resource_type: "image",
                public_id: `profile-${userId}-${Date.now()}`,
                overwrite: false,
            },
            (error, result) => {
                if (error) {
                    reject(error);
                    return;
                }

                if (!result?.secure_url) {
                    reject(new Error("Cloudinary image URL missing"));
                    return;
                }

                resolve(result);
            }
        );

        uploadStream.end(file.buffer);
    });
}


 // ============================================================
 // Extract Cloudinary Profile Image Public ID
 // ============================================================

function getCloudinaryProfileImagePublicId(imageUrl) {
    if (!imageUrl || typeof imageUrl !== "string") {
        return null;
    }

    try {
        const parsedUrl = new URL(imageUrl);

        // Only accept HTTPS images from our Cloudinary account.
        const cloudName = cloudinary.config().cloud_name;

        if (
            parsedUrl.protocol !== "https:" ||
            parsedUrl.hostname !== "res.cloudinary.com" ||
            !cloudName
        ) {
            return null;
        }

        const prefix = `/${cloudName}/image/upload/`;

        if (!parsedUrl.pathname.startsWith(prefix)) {
            return null;
        }

        const imagePath = decodeURIComponent(
            parsedUrl.pathname.slice(prefix.length)
        );

        // Remove Cloudinary transformation/version prefixes.
        const match = imagePath.match(
            /(?:^|\/)(moneymate\/profile-images\/[^/]+)$/
        );

        if (!match) {
            return null;
        }

        // Remove the file extension from the public ID.
        return match[1].replace(/\.[^.]+$/, "");
    } catch (error) {
        return null;
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
    let uploadedImage = null;

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
            return res.status(404).json({
                success: false,
                message: "User not found",
            });
        }

        // Upload image to Cloudinary.
        uploadedImage = await uploadProfileImageToCloudinary(
            req.file,
            userId
        );

        const profileImageUrl = uploadedImage.secure_url;

        // Save permanent HTTPS URL in PostgreSQL.
        await db.orm.public.User
            .where({ id: userId })
            .update({ profileImageUrl });

        const updatedUser = await db.orm.public.User.first({
            id: userId,
        });

        // Database update succeeded.
        // Do not delete the new image during later cleanup.
        uploadedImage = null;

        // Remove the previous image when replacing it.
        if (
            user.profileImageUrl &&
            user.profileImageUrl !== profileImageUrl
        ) {
            if (user.profileImageUrl.startsWith(
                "/uploads/profile-images/"
            )) {
                deleteLocalProfileImage(user.profileImageUrl);
            } else {
                try {
                    const oldPublicId =
                        getCloudinaryProfileImagePublicId(
                            user.profileImageUrl
                        );

                    if (oldPublicId) {
                        await cloudinary.uploader.destroy(
                            oldPublicId,
                            { resource_type: "image" }
                        );
                    }
                } catch (cleanupError) {
                    console.error(
                        "Previous profile image cleanup failed:",
                        cleanupError
                    );
                }
            }
        }

        return res.status(200).json({
            success: true,
            message: "Profile image uploaded successfully",
            data: {
                profileImageUrl: updatedUser.profileImageUrl,
                user: {
                    id: updatedUser.id,
                    name: updatedUser.name,
                    username: updatedUser.username,
                    email: updatedUser.email,
                    profileImageUrl: updatedUser.profileImageUrl,
                    currency: updatedUser.currency,
                    language: updatedUser.language,
                },
            },
        });
    } catch (error) {
        // Roll back a newly uploaded image if DB saving fails.
        if (uploadedImage?.public_id) {
            try {
                await cloudinary.uploader.destroy(
                    uploadedImage.public_id,
                    { resource_type: "image" }
                );
            } catch (cleanupError) {
                console.error(
                    "New profile image rollback failed:",
                    cleanupError
                );
            }
        }

        console.error("Upload profile image error:", error);

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

        if (!user.profileImageUrl) {
            return res.status(200).json({
                success: true,
                message: "Profile image already removed",
                data: {
                    profileImageUrl: null,
                },
            });
        }

        const oldProfileImageUrl = user.profileImageUrl;

        // Clear the image URL from PostgreSQL first.
        await db.orm.public.User
            .where({ id: userId })
            .update({
                profileImageUrl: null,
            });

        // Delete the previous image from its storage.
        try {
            if (oldProfileImageUrl.startsWith(
                "/uploads/profile-images/"
            )) {
                deleteLocalProfileImage(oldProfileImageUrl);
            } else {
                const publicId =
                    getCloudinaryProfileImagePublicId(
                        oldProfileImageUrl
                    );

                if (publicId) {
                    await cloudinary.uploader.destroy(
                        publicId,
                        { resource_type: "image" }
                    );
                }
            }
        } catch (cleanupError) {
            // Profile deletion succeeded in DB.
            // Log cloud cleanup failure without failing the request.
            console.error(
                "Profile image cleanup failed:",
                cleanupError
            );
        }

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

       // Update password and revoke all refresh tokens atomically.
       const client = await pool.connect();

       try {
           await client.query("BEGIN");

           // Update the user's password.
           await client.query(
               `UPDATE "user"
                SET "passwordHash" = $1
                WHERE "id" = $2`,
               [newPasswordHash, userId]
           );

           // Revoke all active refresh tokens.
           await client.query(
               `UPDATE "refreshToken"
                SET "revokedAt" = NOW()
                WHERE "userId" = $1
                  AND "revokedAt" IS NULL`,
               [userId]
           );

           await client.query("COMMIT");
       } catch (error) {
           await client.query("ROLLBACK").catch(() => {});
           throw error;
       } finally {
           client.release();
       }

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
// Change Email
// ============================================================

// -----------------------------------------------------------------------------
// Section: changeEmail
// Purpose: Securely changes the authenticated user's email address.
// -----------------------------------------------------------------------------
async function changeEmail(req, res) {
    try {
        const userId = req.userId;

        const validation =
            changeEmailSchema.safeParse(
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
            newEmail,
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

        // Verify the user's current password before changing email.
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

        // Do not allow changing to the same email address.
        if (
            user.email.toLowerCase() ===
            newEmail.toLowerCase()
        ) {
            return res.status(400).json({
                success: false,
                message:
                    "New email must be different from current email",
            });
        }

        // Make sure another account does not already use this email.
        const existingUser =
            await db.orm.public.User.first({
                email: newEmail,
            });

        if (
            existingUser &&
            existingUser.id !== userId
        ) {
            return res.status(409).json({
                success: false,
                message:
                    "An account with this email already exists",
            });
        }

        await db.orm.public.User
            .where({
                id: userId,
            })
            .update({
                email: newEmail,
            });

        const updatedUser =
            await db.orm.public.User.first({
                id: userId,
            });

        return res.status(200).json({
            success: true,
            message: "Email changed successfully",
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
        console.error(
            "Change email error:",
            error
        );

        return res.status(500).json({
            success: false,
            message: "Failed to change email",
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

        const validation = deleteAccountSchema.safeParse(req.body);

        if (!validation.success) {
            return res.status(400).json({
                success: false,
                message: "Validation failed",
                errors: validation.error.issues.map(
                    (issue) => issue.message
                ),
            });
        }

        const { currentPassword } = validation.data;

        const user = await db.orm.public.User.first({ id: userId });

        if (!user) {
            return res.status(404).json({
                success: false,
                message: "User not found",
            });
        }

        const isPasswordValid = await bcrypt.compare(
            currentPassword,
            user.passwordHash
        );

        if (!isPasswordValid) {
            return res.status(401).json({
                success: false,
                message: "Password is incorrect",
            });
        }


        await db.transaction(async (tx) => {
            const orm = tx.orm;

            // Remove dependent data first so foreign-key constraints remain valid.
            await orm.public.Notification.where({ userId }).deleteAll();
            await orm.public.PasswordResetToken.where({ userId }).deleteAll();
            await orm.public.RefreshToken.where({ userId }).deleteAll();
            await orm.public.TrialDevice.where({ userId }).deleteAll();
            await orm.public.FcmDevice.where({ userId }).deleteAll();
            await orm.public.Budget.where({ userId }).deleteAll();
            await orm.public.Transaction.where({ userId }).deleteAll();
            await orm.public.Bill.where({ userId }).deleteAll();
            await orm.public.SavingsGoal.where({ userId }).deleteAll();
            await orm.public.Category.where({ userId }).deleteAll();
            await orm.public.Account.where({ userId }).deleteAll();
            await orm.public.User.where({ id: userId }).delete();
        });


        // Clean up the user's profile image after account deletion.
        // Cloud cleanup failures must not undo a successful DB deletion.
        try {
            const imageUrl = user.profileImageUrl;

            if (imageUrl?.startsWith("/uploads/profile-images/")) {
                deleteLocalProfileImage(imageUrl);
            } else {
                const publicId =
                    getCloudinaryProfileImagePublicId(imageUrl);

                if (publicId) {
                    await cloudinary.uploader.destroy(
                        publicId,
                        { resource_type: "image" }
                    );
                }
            }
        } catch (cleanupError) {
            console.error(
                "Deleted account profile image cleanup failed:",
                cleanupError
            );
        }


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
    changeEmail,
    deleteUserAccount,
};
