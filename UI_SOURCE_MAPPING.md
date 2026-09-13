# UI Source Mapping

| Native area | HTML source behavior/structure | Color source |
|---|---|---|
| Splash | Splash composition/tagline | Default Template PDF |
| Welcome/Auth | `authWelcome`, `authSignIn`, account creation, forgot/reset flows | Default Template PDF |
| Main shell | four navigation tabs + centered raised Add FAB | Default Template PDF |
| Home | greeting/top actions, balance hero, quick actions, health, overview, budgets, recent transactions | Default Template PDF |
| Activity | search, chips/filtering, transaction rows | Default Template PDF |
| Insights | period chips, income/expense, categories, savings trend, insight cards | Default Template PDF |
| Profile | profile header + grouped settings/tool cards | Default Template PDF |
| Editors | HTML sheet behavior recreated with native Material bottom sheets | Default Template PDF |
| Tool/subscreens | Accounts/Budgets/Goals/Bills/Calendar/Notifications/etc. pushed tool stack | Default Template PDF |
| Premium themes | Premium theme preview/content only | Premium Template PDF |

## Important precedence

HTML controls **what the UI is and where it goes**. The PDFs control **what colors it uses**. If an HTML color conflicts with a PDF token, the PDF wins.
