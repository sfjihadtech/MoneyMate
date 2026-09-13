# Functional fixes in this package

- Language and currency are Compose-observable state and update immediately.
- Language/currency changes persist locally and update the authenticated backend profile.
- Premium theme selection is observable and persists immediately; premium palette remains scoped to premium content/previews, per the supplied theme rules.
- Premium app icon selection now changes the actual Android launcher alias and persists across launches.
- Full Income category set: Salary, Freelance, Business, Investment, Rental Income, Gift, Refund, Other.
- Full Expense category set: Food & Dining, Groceries, Shopping, Transport, Bills & Utilities, Rent & Housing, Entertainment, Subscriptions, Health, Insurance, Travel, Education, Personal Care, Kids & Family, Pets, Gifts & Donations, Taxes, Other.
- Existing authenticated users are backfilled with missing starter categories on refresh without duplicating existing categories.
- New backend registrations create starter accounts and the full category taxonomy transactionally.
- Guest/demo entry was removed from the production navigation path.
- Fake premium/trial activation was removed. Paid subscription activation must be connected to an app-store billing provider and server-side entitlement verification before enabling paid plans.
- Release Android builds now require an explicit production API URL instead of silently falling back to a placeholder host.
