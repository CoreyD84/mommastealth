# MommaStealth - Standalone Child App

## 🎯 Overview

MommaStealth is now configured as a **standalone Android application** that can be distributed separately from the guardian app. When a guardian generates a QR code, the child scans it, downloads the MommaStealth APK, installs it, and the devices automatically link via Firebase.

## 📦 What Changed

### 1. Module Conversion
- **Before:** `mommastealth` was a library module included in the guardian app
- **After:** `mommastealth` is a standalone application that builds its own APK

### 2. Build Configuration
- Changed from `com.android.library` to `com.android.application`
- Added `applicationId = "com.airnettie.mobile.child.mommastealth"`
- Added version code and name
- Added Firebase plugin for authentication and database

### 3. Manifest Updates
- Added LAUNCHER intent filter (makes it installable)
- Added internet and network permissions
- Added foreground service permissions
- Registered BlockedAppActivity

### 4. Distribution Setup
- Created GitHub Actions workflow (`.github/workflows/build-mommastealth.yml`)
- Created download landing page (`link/index.html`)
- Configured automatic APK building and releasing

## 🚀 How It Works

```
┌─────────────────────────────────────────────────────────────────┐
│                     GUARDIAN SIDE                                │
├─────────────────────────────────────────────────────────────────┤
│ 1. Guardian opens app and generates QR code                     │
│ 2. QR contains: https://YOUR_USERNAME.github.io/                │
│    MommaNettie_New/link/?guardianId=XXX&token=YYY              │
│ 3. Token stored in Firebase: guardianLinks/{id}/pendingTokens  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                      CHILD SIDE                                  │
├─────────────────────────────────────────────────────────────────┤
│ 1. Child scans QR code with any QR scanner                      │
│ 2. Opens download page in browser                               │
│ 3. Page shows installation instructions                         │
│ 4. Child clicks "Download MommaStealth"                         │
│ 5. Downloads APK from GitHub Releases                           │
│ 6. Installs APK (requires "Unknown Sources" enabled)            │
│ 7. App auto-opens with deep link containing guardian ID & token │
│ 8. App validates token and creates link in Firebase             │
│ 9. App requests permissions (location, accessibility, VPN)      │
│ 10. ChildSyncService starts and begins monitoring               │
└─────────────────────────────────────────────────────────────────┘
```

## 📋 Setup Checklist

### Step 1: Firebase Configuration
- [ ] Go to Firebase Console (https://console.firebase.google.com/)
- [ ] Add new Android app with package: `com.airnettie.mobile.child.mommastealth`
- [ ] Download updated `google-services.json` (contains both apps)
- [ ] Replace `app/google-services.json` with new file
- [ ] Replace `mommastealth/google-services.json` with new file

### Step 2: GitHub Configuration
- [ ] Create Android signing keystore (see SETUP_INSTRUCTIONS.md)
- [ ] Add GitHub Secrets:
  - `SIGNING_KEY` (Base64 encoded keystore)
  - `ALIAS` (keystore alias)
  - `KEY_STORE_PASSWORD` (keystore password)
  - `KEY_PASSWORD` (key password)

### Step 3: Update URLs
- [ ] Edit `link/index.html` - Replace `YOUR_GITHUB_USERNAME` with your username
- [ ] Edit `GenerateLinkQrActivity.kt` - Replace `YOUR_GITHUB_USERNAME` with your username

### Step 4: Enable GitHub Pages
- [ ] Go to repository Settings → Pages
- [ ] Source: Deploy from branch `main` / `(root)`
- [ ] Save

### Step 5: Build and Release
- [ ] Push changes to GitHub
- [ ] GitHub Actions will automatically build and release the APK
- [ ] APK will be available at: `https://github.com/YOUR_USERNAME/MommaNettie_New/releases/latest/download/mommastealth-release.apk`

## 🔧 Local Testing

### Build the APK locally:
```bash
./gradlew :mommastealth:assembleDebug
```

The APK will be at:
```
mommastealth/build/outputs/apk/debug/mommastealth-debug.apk
```

### Install on device:
```bash
adb install mommastealth/build/outputs/apk/debug/mommastealth-debug.apk
```

### Test deep link:
```bash
adb shell am start -a android.intent.action.VIEW -d "nettielink://child/link?guardianId=TEST&token=TEST123"
```

## 📱 Child App Features

### Stealth Mode
- No visible app icon (uses Theme.NoDisplay)
- Runs entirely in background
- Minimal notifications (low priority)
- Excludes from recent apps list

### Monitoring Capabilities
1. **Location Tracking**
   - Updates every 5 minutes
   - Stores in Firebase: `guardianLinks/{guardianId}/location/{childId}`

2. **App Blocking**
   - Uses Accessibility Service
   - Monitors: Messenger, Discord, Roblox, TikTok, Snapchat, Instagram, YouTube, Facebook, Twitter
   - Shows "CLOSED" overlay when blocked app is opened

3. **SafeScope VPN**
   - Routes all traffic through VPN
   - Filters unsafe content
   - Controlled by guardian toggle

4. **Heartbeat**
   - Updates `lastSeen` timestamp
   - Stored in: `guardianLinks/{guardianId}/childProfiles/{childId}/lastSeen`

### Required Permissions
- `ACCESS_FINE_LOCATION` - GPS tracking
- `ACCESS_COARSE_LOCATION` - Network location
- `ACCESS_BACKGROUND_LOCATION` - Background tracking
- `BIND_ACCESSIBILITY_SERVICE` - App blocking
- `INTERNET` - Firebase communication
- `FOREGROUND_SERVICE` - Background operation
- VPN permission (user grants via system dialog)

## 🔐 Security Notes

1. **Signing Keys**
   - Never commit keystore files to repository
   - Store in GitHub Secrets only
   - Use strong passwords

2. **Firebase Security**
   - Configure Firebase security rules
   - Validate tokens server-side if possible
   - Tokens expire after use

3. **Distribution**
   - APK is signed with your keystore
   - Distributed via GitHub Releases
   - Child must enable "Unknown Sources"

## 🐛 Troubleshooting

### Build Fails
- **Error:** "No matching client found for package name"
  - **Solution:** Add child app to Firebase Console (see FIREBASE_SETUP.md)

### APK Won't Install
- **Error:** "App not installed"
  - **Solution:** Enable "Install from Unknown Sources" in Android settings
  - **Solution:** Check minimum SDK version (24 / Android 7.0)

### App Won't Link
- **Error:** Link fails silently
  - **Solution:** Check Firebase configuration
  - **Solution:** Verify token exists in Firebase
  - **Solution:** Check internet connection

### Services Don't Start
- **Error:** Location not updating
  - **Solution:** Grant location permissions
  - **Solution:** Disable battery optimization for the app

### App Blocking Not Working
- **Error:** Blocked apps still open
  - **Solution:** Enable Accessibility Service in Android settings
  - **Solution:** Grant accessibility permission to "Nettie App Blocker"

## 📂 File Structure

```
mommastealth/
├── src/main/
│   ├── java/com/airnettie/mobile/child/
│   │   ├── MainActivity.kt              # Deep link handler & linking
│   │   ├── ChildSyncService.kt          # Main background service
│   │   ├── AppBlockerService.kt         # Accessibility service
│   │   ├── SafeScopeVpnService.kt       # VPN content filtering
│   │   ├── PlatformControlReceiver.kt   # App blocking receiver
│   │   └── ui/
│   │       └── BlockedAppActivity.kt    # "CLOSED" overlay
│   ├── res/
│   │   ├── values/strings.xml
│   │   ├── values/themes.xml
│   │   └── xml/app_blocker_service_config.xml
│   └── AndroidManifest.xml
├── build.gradle.kts                     # Application config
└── google-services.json                 # Firebase config

.github/workflows/
└── build-mommastealth.yml               # Auto-build workflow

link/
└── index.html                           # Download landing page
```

## 🔄 Update Process

When you make changes to the child app:

1. Commit and push changes to GitHub
2. GitHub Actions automatically builds new APK
3. New release is created with incremented version
4. Guardian app QR codes point to "latest" release
5. Children automatically get the newest version when scanning

## 📞 Support

For issues or questions:
1. Check SETUP_INSTRUCTIONS.md
2. Check FIREBASE_SETUP.md
3. Review GitHub Actions logs
4. Check Firebase Console for errors

## ✅ Current Status

- [x] Module converted to standalone app
- [x] Build configuration updated
- [x] Manifest configured
- [x] GitHub Actions workflow created
- [x] Download page created
- [x] Firebase configuration documented
- [x] Local build tested successfully
- [ ] Firebase child app registered (YOU NEED TO DO THIS)
- [ ] GitHub Secrets configured (YOU NEED TO DO THIS)
- [ ] GitHub Pages enabled (YOU NEED TO DO THIS)
- [ ] URLs updated with your GitHub username (YOU NEED TO DO THIS)

## 🎉 Next Steps

1. Follow FIREBASE_SETUP.md to add child app to Firebase
2. Follow SETUP_INSTRUCTIONS.md to configure GitHub
3. Update URLs with your GitHub username
4. Push to GitHub and test the full flow
5. Generate QR code from guardian app
6. Scan with child device and verify installation
