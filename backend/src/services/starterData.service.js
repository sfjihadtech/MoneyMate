// =============================================================================
// File: starterData.service.js
// Purpose: Business/service layer for starter Data.service functionality.
// Notes: Major executable sections are documented for easier maintenance.
// =============================================================================

const { db } = require("../prisma/db.ts");

const STARTER_ACCOUNTS = [
    ["Cash Wallet", "cash"],
    ["Main Bank", "bank"],
    ["Savings", "savings"],
    ["Credit Card", "credit_card"],
];

const CATEGORY_TAXONOMY = [
    ["Salary", "income", "payments"],
    ["Freelance", "income", "work"],
    ["Business", "income", "business_center"],
    ["Investment", "income", "trending_up"],
    ["Rental Income", "income", "apartment"],
    ["Gift", "income", "redeem"],
    ["Refund", "income", "undo"],
    ["Other", "income", "category"],
    ["Food & Dining", "expense", "restaurant"],
    ["Groceries", "expense", "shopping_cart"],
    ["Shopping", "expense", "shopping_bag"],
    ["Transport", "expense", "directions_car"],
    ["Bills & Utilities", "expense", "receipt_long"],
    ["Rent & Housing", "expense", "home"],
    ["Entertainment", "expense", "movie"],
    ["Subscriptions", "expense", "subscriptions"],
    ["Health", "expense", "health_and_safety"],
    ["Insurance", "expense", "shield"],
    ["Travel", "expense", "flight"],
    ["Education", "expense", "school"],
    ["Personal Care", "expense", "spa"],
    ["Kids & Family", "expense", "family_restroom"],
    ["Pets", "expense", "pets"],
    ["Gifts & Donations", "expense", "volunteer_activism"],
    ["Taxes", "expense", "account_balance"],
    ["Other", "expense", "category"],
];


// -----------------------------------------------------------------------------
// Section: ensureStarterData
// Purpose: Handles the ensure Starter Data part of this backend module.
// -----------------------------------------------------------------------------
async function ensureStarterData(user) {
    const userId = user.id;
    const currency = user.currency || "USD";

    // Accounts are seeded only for a genuinely empty account list. Never recreate
    // deleted starter accounts on every sign-in.
    const accounts = await db.orm.public.Account.where({ userId }).all();
    if (accounts.length === 0) {
        for (const [name, type] of STARTER_ACCOUNTS) {
            await db.orm.public.Account.create({
                userId,
                name,
                type,
                balance: 0,
                currency,
            });
        }
    }

    // Categories are backfilled individually so older users receive the complete
    // taxonomy without duplicates and without losing custom categories.
    const categories = await db.orm.public.Category.where({ userId }).all();
    const existing = new Set(
        categories.map((item) => `${String(item.type).toLowerCase()}::${String(item.name).toLowerCase()}`)
    );

    for (const [name, type, icon] of CATEGORY_TAXONOMY) {
        const key = `${type.toLowerCase()}::${name.toLowerCase()}`;
        if (existing.has(key)) continue;
        await db.orm.public.Category.create({ userId, name, type, icon });
        existing.add(key);
    }
}

module.exports = {
    CATEGORY_TAXONOMY,
    ensureStarterData,
};
