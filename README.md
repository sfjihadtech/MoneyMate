# MoneyMate — HTML-Structure Native Android Package

This package contains the native Jetpack Compose Android app, the Node/Express/PostgreSQL backend, and the exact design references used for this rebuild.

## Source-of-truth precedence

1. `reference/moneymate-23.html` — screen structure, hierarchy, navigation, card placement, sheets, actions, labels and interaction flow.
2. `reference/Default_Template.pdf` — all normal MoneyMate UI colors/tokens.
3. `reference/Premium_Template.pdf` — colors only for Premium Template/theme previews and premium content.

The Android app is native Compose; it does **not** embed the HTML in a WebView. The HTML prototype's old colors are deliberately not copied.

## UI rebuild in this package

- Splash follows the MoneyMate branded launch composition.
- Production navigation follows the HTML prototype: Splash -> Welcome/Auth -> main app. The prototype's hidden/removed onboarding flow is not inserted into the normal launch path.
- Welcome, Sign In, Create Account, Forgot Password and Reset Password were rebuilt around the HTML structure.
- Home mirrors the prototype's greeting top bar, balance hero, three summary stats, four quick actions, Financial Health, Monthly Overview, Budget Progress and Recent Transactions.
- Activity mirrors the prototype's search/filter chips and transaction-list hierarchy.
- Insights mirrors the prototype's range controls, income/expense summary, category breakdown, savings trend and insight cards.
- Profile mirrors the prototype's profile header and grouped Financial Tools, General, Security, Data, Help, Legal, About and Sign Out sections.
- Main bottom navigation uses the prototype's four tabs plus raised center Add FAB.
- Add Transaction, Transfer, Account, Budget, Goal, Contribution and Bill editors use bottom sheets instead of generic centered dialogs.
- Accounts, Budgets, Goals, Bills, Calendar, Notifications, Advanced Analytics, AI Insights, Recurring, Filters, Appearance, Language, Currency, Accessibility, Premium Themes, Security, Backup/Restore, Export/Import, Profile editing, Plans, Help/FAQ/Legal/About are included in the native tool stack.

## Color rules

Normal app UI uses the supplied Default Template tokens only:

- Brand / Deep Navy `#0B1F33`
- Action / Royal Blue `#1769FF`
- Success `#12B76A`
- Error `#D92D20`
- Warning `#F79009`
- App Background `#F7F9FC`
- Surface `#FFFFFF`
- Primary Text `#101828`
- Secondary Text `#667085`
- Muted Text `#98A2B3`
- Border `#E4E7EC`
- Divider `#F0F2F5`

Premium theme tokens are intentionally scoped to premium template previews/content and never recolor the normal app chrome.

## Android local development

Open only `android/` in Android Studio.

The included debug build uses:

```text
http://10.0.2.2:5000/
```

for the Android Emulator. `app/src/debug/AndroidManifest.xml` permits cleartext only for debug; the main/release manifest keeps cleartext disabled.

For production, set:

```properties
MONEYMATE_API_BASE_URL=https://api.yourdomain.com/
```

The URL must end with `/`.

## Backend local development

If you already have a working private `.env`, copy it into this package's `backend/` locally. Do not share the secrets in chat.

Otherwise:

```bash
cd backend
cp .env.example .env
npm install
npm run dev
```

Health endpoint:

```text
GET http://localhost:5000/api/health
```

## Provider-dependent production work

Real third-party credentials cannot be bundled in source. Before a public store release, configure your own credentials/infrastructure for Google OAuth (if enabled), Google Play Billing/subscription verification, production SMTP, support/contact destination, production HTTPS API domain, Android release signing, and remote push if desired.

## Final production pass additions

- Sign In/Create Account input fields use professional outlined containers, clear focus/error states, typed keyboards, and password visibility controls.
- The built-in Income/Expense taxonomy is centralized on Android and mirrored by the backend starter-data service.
- Activity filters expose the complete category set instead of truncating it. Budget and recurring transaction pickers use the same type-specific category data.
- Backend sign-in no longer creates duplicate starter accounts/categories on every login. Missing built-in categories are backfilled without deleting custom categories.
- Public guest/demo entry flow and fake trial-status presentation were removed from the normal production navigation.
- Member-since information is sourced from the authenticated backend user timestamp instead of a fixed sample date.
- Release version is `2.5.0` / versionCode `2`.
- See `PUBLISHING_GUIDE_BN.md` for the owner-required deployment/signing/store steps.

## Verification in the packaging environment

- Backend JavaScript source is syntax-checked with `node --check`.
- Known Kotlin source errors found during the earlier Android Studio run were incorporated into this rebuild: Compose `LocalContext` usage, reset-password parameter naming, CSV return-string syntax, `RoundedCornerShape`, and debug manifest merge override.
- Static Kotlin/parser scans are run for malformed source patterns.
- Secrets/build caches are excluded from the release ZIP.
- ZIP integrity is tested after packaging.
- A full Android Gradle compile cannot be completed in this environment because the required Gradle 9.5 distribution is not available offline. Open **this exact package's `android/` folder** in Android Studio and run `Build > Make Project` as the final compile verification.


## UI fidelity rule
- `reference/moneymate-23.html` is the authoritative source for screen hierarchy, component order, spacing proportions, navigation, sheets, dialogs, and interaction flow.
- `reference/Default_Template.pdf` is the authoritative normal-app color system. HTML colors are intentionally not copied into the native app.
- `reference/Premium_Template.pdf` is used only for Premium template/theme content; Premium selections do not recolor normal app chrome.
- The Android app remains native Jetpack Compose; it is not a WebView wrapper.
