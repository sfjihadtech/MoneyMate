# MoneyMate v2.6.0 — Publishable Source Candidate

## Completed in this pass

- Professional Sign In / Sign Up fields with consistent Default/Premium theme tokens.
- Improved focused, disabled and error input states.
- Password visibility UI and live password-strength feedback.
- One canonical category ordering layer for every category selector/filter.
- Verified Android and backend built-in taxonomy are identical: 8 income + 18 expense = 26 categories.
- Existing custom user categories are preserved.
- Release version bumped to versionCode 3 / versionName 2.6.0.

## Required owner configuration before Play publication

1. Deploy the backend behind HTTPS and configure production PostgreSQL, JWT secret and SMTP.
2. Set `MONEYMATE_API_BASE_URL` to the real HTTPS API URL.
3. Build and test the release variant on your Mac.
4. Generate and securely retain your upload keystore.
5. Configure Google Play subscription products and purchase verification if Premium will charge real money.
6. Provide real Privacy Policy / Terms / support URLs and complete Play Console policy forms.
7. Run the acceptance checklist in `PUBLISHING_GUIDE_BN.md`.

## Build-environment note

The source was packaged after static/source consistency checks. A full Gradle compile could not be completed in the packaging environment because the required Gradle distribution was not locally cached and outbound download was unavailable.
