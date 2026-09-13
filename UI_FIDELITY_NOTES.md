# MoneyMate native UI fidelity pass

Authoritative layout/flow reference: `reference/moneymate-23.html`.
Authoritative normal color reference: `reference/Default_Template.pdf`.
Premium color reference: `reference/Premium_Template.pdf`, scoped to Premium content only.

Native Compose fidelity work in this package includes:
- HTML-style top bars, cards, radii, spacing hierarchy, four-tab bottom navigation and raised center FAB.
- Home hierarchy: greeting/actions, Available Balance hero, income/expense stats, quick actions, Financial Health, Monthly Overview, Budget Progress and Recent Transactions.
- Activity hierarchy: top filter action, search, horizontal category filters and transaction rows.
- Insights hierarchy and Financial Health pushed screen.
- Profile grouped sections, quick-profile pushed screen, trial/premium presentation, settings rows and footer.
- HTML-style pushed screens for Accounts, Budgets, Goals, Bills, Calendar, Notifications, Edit Profile, Accessibility, Help Center, Contact Support and About.
- HTML-style modal sheets for Add Transaction, Transfer, Language, Currency, Plans and Filter & Sort.
- Data screens for Backup, Restore, Export and Import retain real repository/file behavior while matching the prototype's visual hierarchy as closely as possible.

The implementation intentionally does not copy the HTML prototype's color palette. Normal UI uses the Default Template color tokens.
