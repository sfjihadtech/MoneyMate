# MoneyMate Final UI Parity Pass

Authoritative UI/interaction reference: `reference/moneymate-23.html`.
Normal app colors: `reference/Default_Template.pdf`.
Premium theme preview colors: `reference/Premium_Template.pdf`.

## Corrected in this pass

- Add Expense / Add Income transaction sheet: HTML-style handle/header, segmented type control, amount panel, four-column category grid, Merchant/Source behavior, date/time pair, payment chips, account chips, notes, receipt control, and 1:2 Cancel/Save actions.
- Analytics: HTML-style six-month paired bar chart, category donut with center total + legend, savings area/line chart, and Home monthly line chart.
- Premium Themes: HTML-style realistic mini Home previews, three swatches, selection indicator, compact card proportions, and premium header structure.
- Premium App Icons: two-column icon grid, realistic rounded icon tiles, selected ring/check, Exclusive badges, and PDF-token-driven preview colors.
- Subscription: HTML-style bottom sheet, exact three plan tiers ($5.55 / $25.25 / $45.45), savings labels, Best Value badge, radio selection, CTA and demo footnote.
- Currency Manager: HTML-style sheet/list, display-only note, radio/check selection, account/profile persistence.
- Language: English / বাংলা chooser with check/radio state; main navigation, key dashboard/transaction/analytics labels, and transaction sheet respond to language preference.
- Previously fixed compile issues remain included: Compose LocalContext usage, reset password parameter name, CSV returns, onboarding shape/imports, debug manifest merge override, MainTabs named callback, Dialogs clickable/icon imports, HtmlChip callback binding, ToolScreens horizontalScroll import.

## Backend connection

The Android debug build targets `http://10.0.2.2:5000/`, so the emulator connects to the bundled backend running on the Mac at port 5000. Auth register/login/me, password reset, profile, accounts, transactions, budgets, goals, bills, analytics, notifications, transfer and backup routes are wired through Retrofit/repository code.

For a production release build, set `MONEYMATE_API_BASE_URL` to the deployed HTTPS backend URL. The package intentionally does not embed production secrets.

## External provider requirements

Google OAuth, Google Play Billing/real subscription charging, remote push delivery, production support provider, production object storage, HTTPS/domain and release signing require the owner's real provider credentials/configuration. The UI/state hooks are included, but provider credentials cannot be bundled safely in source.
