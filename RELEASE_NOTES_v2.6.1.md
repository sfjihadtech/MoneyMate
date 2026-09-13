# MoneyMate v2.6.1

## UI change
- Removed the blue radial glow from the Welcome screen.
- Welcome now uses the selected theme brand color as a clean solid background.

## Code readability
- Added file-level purpose comments throughout Android Kotlin source.
- Added section/purpose comments for top-level screens, composables, classes, helpers, and major functions.
- Added backend file and handler/function comments across controllers, routes, services, middleware, validators, and server/database helpers.
- Added explanatory comments to Android XML resources and build/database configuration files.
- Added `CODE_COMMENT_GUIDE.md` to explain the project comment convention.

## Build fix
- Preserved the required `CircleShape` import in `Dialogs.kt`, fixing the compile error found during local testing.

## Version
- versionCode: 4
- versionName: 2.6.1
