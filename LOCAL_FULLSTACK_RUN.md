# Local full-stack test

The Android emulator uses `http://10.0.2.2:5000/` for debug builds.

Backend:

```bash
cd backend
cp .env.example .env
# Put your own PostgreSQL/SMTP/JWT values in .env. Never commit or share real secrets.
npm install
npm start
```

Health check:

```bash
curl http://localhost:5000/health
```

Android:

Open `android/` in Android Studio, let Gradle sync, then Build > Make Project and Run on the emulator.

Production release API URL:

```bash
./gradlew bundleRelease -PMONEYMATE_API_BASE_URL=https://your-api-domain.example/
```
