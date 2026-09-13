# MoneyMate production status

This package removes fake/demo activation paths and wires account creation, login, profile preferences, categories, transactions, budgets, goals, bills, analytics, backup/restore, language/currency persistence, and dynamic launcher icons to real app state/backend APIs.

Before store publication you still must provide operator-owned production configuration: a real HTTPS API URL, production database, SMTP credentials, release signing key, privacy/legal text review, and app-store billing product IDs plus server-side entitlement verification if paid subscriptions are enabled. No fake subscription activation is used.


## v2.6.0 final UI/category consistency pass

- Sign In and Create Account now share a more polished finance-grade input component: stronger hierarchy, 16dp field radius, themed icon containers, focused/error states, password visibility treatment and clearer inline error presentation.
- Create Account includes a live four-step password-strength indicator while preserving the existing production validation rules.
- Built-in categories are canonically ordered through `CategoryCatalog` across Add Income/Expense, budget category selection, recurring transactions, transaction-history filters and advanced filters. Custom categories are preserved and appear after the built-ins.
- Android and backend starter taxonomy verified identical: 26 total categories (8 income, 18 expense).
