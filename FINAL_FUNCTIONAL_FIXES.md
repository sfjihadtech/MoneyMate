# Final functional settings/data pass

This pass removes fake/demo state changes and wires the production-facing settings/data flows.

- Language and currency are Compose-observable state and update immediately.
- Signed-in language/currency changes are persisted to the backend profile; local mode keeps device preferences.
- Premium theme selection is observable and applies instantly after explicit selection. Fresh installs stay on the Default Template theme until the user picks a premium theme.
- Premium app icon selection now changes the actual Android launcher component through activity aliases; eight launcher icon resources are bundled.
- New and existing accounts are backfilled with the complete Income/Expense category taxonomy used by the HTML reference.
- Backend registration seeds starter accounts and the full category taxonomy server-side.
- Fake subscription activation was removed. The subscription UI no longer grants premium locally without a real store billing provider.
- Release builds require `MONEYMATE_API_BASE_URL`; there is no example.com production fallback.
- Debug emulator builds continue to use `http://10.0.2.2:5000/`.

External services still require owner credentials before public release: app-store billing products/verification, production HTTPS/API domain, SMTP credentials, signing key, and any OAuth provider credentials you choose to ship.
