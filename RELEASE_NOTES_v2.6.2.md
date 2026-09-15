# MoneyMate v2.6.2

- Removed the hard-coded monthly percentage comparison from the balance card.
- Color-coded Total Income (green), Total Expenses (red), and Savings (blue).
- Added account removal with confirmation.
- Transaction entry now uses the user's real accounts as the payment-account choices; selected account receives income/expense balance changes through the existing backend transaction logic.
- Replaced export-ready inline text with app-wide toast feedback; export reports “Export successfully saved”.
- Removed the fake Cloud Sync switch; account-backed sync status now reflects Guest Mode vs authenticated MoneyMate data.
- Removed Help Center navigation and merged help topics into the FAQ category layout.
- Removed Open Source Licenses navigation/content.
- Added profile photo selection/upload/change and profile-photo display using the existing authenticated profile image API.
- Moved device authentication fingerprint into the keypad bottom-left position and removed the extra authentication text.
- Removed the bottom navigation surface border around the center FAB.
- Preserved the transfer fix so transfers are not counted as income or expense.
