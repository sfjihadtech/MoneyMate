# MoneyMate Code Comment Guide

This version was prepared so the source is easier to learn and maintain.

## Comment convention

- Every main Android Kotlin source file has a **File / Purpose / Notes** header.
- Top-level screens, composables, classes, state holders, helpers, and major functions use a visible **Section / Purpose** comment block.
- Backend controllers, routes, services, middleware, validators, and server/database helpers include file-level purpose comments and major handler/function section comments.
- Build/configuration and Prisma schema files include concise purpose comments.
- Comments explain **why a section exists and what it owns**; they intentionally avoid commenting every obvious line so the actual implementation remains readable.

## Welcome screen update

The previous blue radial glow was removed. `WelcomeScreen.kt` now uses the theme `brand` color as a clean solid background.

## Build fix retained

`Dialogs.kt` includes the required `CircleShape` import so the category/selection UI compiles successfully.
