# MoneyMate Feature Matrix

| Capability | Status in this package |
|---|---|
| HTML-structured Splash -> Auth -> App flow | Implemented natively |
| Hidden/removed prototype onboarding | Not inserted into production launch flow |
| Sign in / create account / guest mode | Implemented |
| Forgot / reset password | Implemented with backend + deep link |
| HTML-style Home dashboard | Implemented |
| HTML-style bottom nav + raised center Add FAB | Implemented |
| Add transaction bottom sheet | Implemented |
| Income / expense entry | Implemented |
| Activity/search/filter | Implemented |
| Edit / delete transactions | Implemented |
| Accounts | Implemented |
| Transfer between accounts | Implemented with atomic backend transaction |
| Budgets / progress / alerts | Implemented |
| Savings goals / contributions / milestone notifications | Implemented |
| Bills / upcoming / paid / reminders | Implemented |
| Calendar / daily summary | Implemented |
| Notifications | Implemented |
| Analytics / category breakdown | Implemented |
| Advanced analytics | Implemented |
| Private rules-based financial insights | Implemented |
| Advanced search & filters | Implemented |
| Recurring transaction templates | Implemented |
| Profile / edit profile / photo | Implemented |
| Appearance / dark mode | Implemented |
| Language / currency | Implemented |
| Push preference | Implemented; remote push provider not bundled |
| PIN lock / device authentication / auto-lock | Implemented |
| Change password | Implemented |
| Permanent account deletion | Implemented |
| Backup / restore | Implemented |
| CSV import/export | Implemented |
| PDF export | Implemented |
| Premium themes | Supplied PDF themes implemented as premium-only previews/content |
| Premium app icon selection | State/UI implemented; signed alternate icon assets need owner assets |
| Trial/plans/paywall shell | Implemented; real store billing requires owner credentials |
| Help / FAQ / Privacy / Terms / Licenses / About | Implemented |
| Contact support | UI shell; production provider/destination required |
| Google Sign-In | Credential-dependent integration point; not falsely hard-coded |
| Play Billing | Credential/product-ID-dependent; not falsely hard-coded |

## Theme precedence

Normal MoneyMate UI always uses `Default_Template.pdf`. Premium theme selections apply only to premium templates/content. HTML colors are never the theme source.
