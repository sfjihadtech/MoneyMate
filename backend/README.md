# MoneyMate Backend

Node.js + Express + PostgreSQL + Prisma 8 REST API used by the native MoneyMate Android app.

## Local development

1. Install Node.js 24+ and PostgreSQL 15+.
2. Copy `.env.example` to `.env` and set real values.
3. Run `npm install`.
4. Make sure the Prisma contract has been emitted for the database configuration used by this project.
5. Run `npm run dev`.
6. Verify `GET http://localhost:5000/api/health`.

The Android emulator reaches the Mac host through `http://10.0.2.2:5000/`.

## Production checklist

- Set `NODE_ENV=production`.
- Use a strong, private `JWT_SECRET`.
- Serve the API behind HTTPS/reverse proxy.
- Use managed PostgreSQL with backups.
- Configure a transactional email provider and keep SMTP credentials out of source control.
- Set `PASSWORD_RESET_URL=moneymate://reset-password` (or a verified HTTPS App Link if you deploy one).
- Restrict `CORS_ORIGINS` to trusted browser frontends if a web client is deployed.
- Store profile uploads in object storage rather than ephemeral local disk when deploying to stateless infrastructure.
- Run dependency/security audits and upgrade intentionally; do not apply breaking `npm audit fix --force` blindly.

## Core API groups

`/api/auth`, `/api/password-reset`, `/api/profile`, `/api/accounts`, `/api/categories`, `/api/transactions`, `/api/transfers`, `/api/budgets`, `/api/savings-goals`, `/api/bills`, `/api/dashboard`, `/api/insights`, `/api/notifications`, `/api/backup`, `/api/restore`.
