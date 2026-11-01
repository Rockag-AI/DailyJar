# Daily Jar (Android)
Splits your monthly money into a daily limit, reads bank debit SMS automatically, and moves unspent money into a savings jar.

## Get the APK (no Android Studio needed)
1. Create a new GitHub repository and upload everything in this folder (keep the .github folder).
2. Open the repo's **Actions** tab. The "Build APK" workflow runs on every push (or press Run workflow).
3. When it finishes, open the run and download the **DailyJar-apk** artifact. Unzip it to get app-debug.apk.
4. Copy the APK to your phone, open it, and allow "install unknown apps" when asked.

## First run on the phone
- Allow the SMS and notification permissions.
- If SMS tracking stays off on Android 13+: Settings > Apps > Daily Jar > menu (three dots) > **Allow restricted settings**, then allow the SMS permission.
- You get a message at 7:00 every morning with today's limit.

## Notes
- Only debit SMS (debited / spent / paid / sent / withdrawn) are counted. Credits, OTPs and promos are ignored.
- Data stays on the phone. Nothing is uploaded.
