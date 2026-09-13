# MoneyMate Release Checklist

## Required before Play Store release

- [ ] Set production API URL in Gradle property `MONEYMATE_API_BASE_URL`.
- [ ] Deploy backend behind HTTPS.
- [ ] Set `NODE_ENV=production` and all backend environment secrets.
- [ ] Configure production PostgreSQL backups/monitoring.
- [ ] Configure email sender and verify password-reset deep link/App Link.
- [ ] Replace local profile-image storage with object storage if backend is stateless.
- [ ] Add Google OAuth credentials if Google Sign-In is enabled.
- [ ] Add Google Play Billing product IDs and server-side entitlement verification if paid subscriptions are enabled.
- [ ] Replace support placeholder with real support channel/API.
- [ ] Review Privacy Policy and Terms for the operating jurisdiction.
- [ ] Create signing key outside source control and configure release signing locally/CI.
- [ ] Run Android lint, unit/instrumented tests, dependency audit, and release build.
- [ ] Test backup/restore using a disposable account before production rollout.

## Theme rules

- Normal app UI: Default Template tokens only.
- Premium Templates: selected Premium Theme tokens only.
- Positive = theme success/green.
- Negative/destructive = theme error/red.
- Warning/pending = theme warning/amber.
- Use background → surface → elevated-surface hierarchy in dark mode.
