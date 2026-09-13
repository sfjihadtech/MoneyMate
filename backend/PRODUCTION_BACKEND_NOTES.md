# MoneyMate Backend Production Notes

This backend requires PostgreSQL, a strong JWT secret, HTTPS termination, and SMTP credentials for password reset email.

The authentication flow seeds starter accounts only when the user has no accounts. The complete MoneyMate category taxonomy is backfilled by `(type, name)` without duplicates, so every signed-in user gets the same built-in category choices while custom categories remain intact.

Never commit `.env`. Use `.env.example` as the deployment template.
