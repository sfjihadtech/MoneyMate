# MoneyMate — Production Publish Guide (বাংলা)

এই ZIP-এ native Android app + Node/Express/PostgreSQL backend আছে। Source code-এ কোনো real password, SMTP secret, JWT secret, signing key বা store credential রাখা হয়নি। এগুলো publish করার আগে owner হিসেবে আপনাকেই বসাতে হবে।

## 1. Android Studio-তে final build check

1. ZIP extract করুন।
2. Android Studio → **Open** → `moneymate_release/android` নির্বাচন করুন।
3. Gradle Sync শেষ হতে দিন।
4. **Build → Make Project** চালান।
5. Debug emulator test-এর সময় backend Mac-এ port `5000`-এ চললে app `http://10.0.2.2:5000/` ব্যবহার করবে।

## 2. Production backend প্রস্তুত করুন

1. Managed PostgreSQL বা production PostgreSQL তৈরি করুন।
2. `backend/.env.example` কপি করে `backend/.env` বানান।
3. অন্তত এই মানগুলো real value দিন:
   - `NODE_ENV=production`
   - `DATABASE_URL=...`
   - `JWT_SECRET=...` (strong random secret)
   - `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASS`, `EMAIL_FROM`
   - `PASSWORD_RESET_URL=moneymate://reset-password`
4. Terminal:

```bash
cd moneymate_release/backend
npm ci
npm run contract:emit
npm start
```

5. `GET /api/health` HTTPS domain থেকে success দেয় কিনা verify করুন।
6. Backend অবশ্যই HTTPS domain-এ deploy করুন, যেমন `https://api.yourdomain.com/`।

## 3. Android release API URL সেট করুন

Release build-এর আগে real HTTPS backend URL দিন। URL-এর শেষে `/` থাকা উচিত।

```bash
cd moneymate_release/android
./gradlew bundleRelease -PMONEYMATE_API_BASE_URL=https://api.yourdomain.com/
```

Production URL না দিলে release packaging ইচ্ছাকৃতভাবে block হবে। Debug build-এর উপর এর প্রভাব নেই।

## 4. Release signing

1. Android Studio → **Build → Generate Signed App Bundle / APK**।
2. **Android App Bundle** নির্বাচন করুন।
3. নিজের upload keystore তৈরি/নির্বাচন করুন।
4. Keystore password/private key source code বা Git-এ রাখবেন না।
5. Google Play App Signing enable করুন।

## 5. Subscription / Premium billing — আপনার manual কাজ

Premium plan UI আছে, কিন্তু real money নেওয়ার জন্য Google Play Console-এর real subscription products এবং Play Billing entitlement verification লাগবে। Fake/local premium activation রাখা হয়নি। Publish করার আগে:

1. Play Console-এ Monthly, 6 Months, Yearly subscription/base plans তৈরি করুন।
2. Product IDs final করুন।
3. Google Play Billing library/checkout flow-এ ওই IDs বসান।
4. Purchase acknowledgement + entitlement restore + server-side purchase verification যোগ করুন।
5. License testers দিয়ে test purchase করুন।
6. Successful purchase ছাড়া premium entitlement দেবেন না।

এই provider/store configuration ছাড়া subscription button revenue-collecting production feature হিসেবে complete নয়।

## 6. Google Sign-In

বর্তমান email/password sign-up/sign-in backend-connected। যদি Google Sign-In publish করতে চান, Google Cloud/Play Console OAuth credentials এবং backend ID-token verification আলাদাভাবে configure করতে হবে। Fake Google login রাখা হয়নি।

## 7. Password reset email

SMTP real credentials configure করার পর:

1. Forgot Password চাপুন।
2. Email আসে কিনা দেখুন।
3. `moneymate://reset-password?token=...` deep link app খুলে কিনা verify করুন।
4. Password reset করে old/new password login test করুন।

## 8. Categories

Built-in taxonomy Android এবং backend-এ একই source list অনুযায়ী রাখা হয়েছে:

**Income:** Salary, Freelance, Business, Investment, Rental Income, Gift, Refund, Other.

**Expense:** Food & Dining, Groceries, Shopping, Transport, Bills & Utilities, Rent & Housing, Entertainment, Subscriptions, Health, Insurance, Travel, Education, Personal Care, Kids & Family, Pets, Gifts & Donations, Taxes, Other.

New account-এ full taxonomy seed হয়। Existing user login করলে missing built-in categories `(type + name)` অনুযায়ী backfill হয়; duplicate create হয় না এবং custom category delete হয় না।

## 9. Store listing / policy manual কাজ

Publish করার আগে আপনাকে আরও করতে হবে:

- Real Privacy Policy URL
- Terms of Service URL
- Support email / support URL
- Data Safety form
- App content / financial-features declarations যেখানে প্রযোজ্য
- Final screenshots, feature graphic, app description
- Content rating questionnaire
- Closed/Internal testing track
- Crash/ANR test
- Production database backup/monitoring
- Profile uploads-এর জন্য production object storage যদি stateless hosting ব্যবহার করেন

## 10. Final acceptance test

Release candidate-এ অন্তত test করুন:

- Create Account
- Sign In / Sign Out
- Forgot / Reset Password
- Language switch
- Currency switch
- Theme + app icon switch
- Add Income / Add Expense এবং সব categories
- Edit/Delete transaction
- Accounts + transfer
- Budgets + goals + bills
- Analytics/charts
- Backup/restore/export/import
- PIN/biometric settings যেখানে device support করে
- App restart-এর পর settings persistence
- Network offline/error states
- Release build HTTPS API connection

সবগুলো pass করার পরই Play Console production track-এ rollout করুন।


## 11. v2.6.0-এ আমি যা final করেছি

- Sign In / Sign Up input field professional polish
- Password visibility + error state polish
- Sign Up password-strength indicator
- Android/backend category taxonomy exact match check
- Add Income/Expense, Budget, Recurring, Transaction History এবং Advanced Filters-এ canonical category ordering
- Release version: `versionCode 3`, `versionName 2.6.0`

### এই environment-এ যে verification করা যায়নি

এই package তৈরির environment-এ Gradle distribution download করার network access নেই। তাই final Android compilation এখানে শেষ করা যায়নি। আপনার Mac-এ Android Studio Gradle Sync হওয়ার পর নিচের command দুটো চালিয়ে final source compile verify করুন:

```bash
cd moneymate_release/android
./gradlew clean assembleDebug
./gradlew bundleRelease -PMONEYMATE_API_BASE_URL=https://YOUR_REAL_API_DOMAIN/
```

তারপর Android Studio-এর **Generate Signed App Bundle / APK** থেকে নিজের keystore দিয়ে signed `.aab` তৈরি করুন।
