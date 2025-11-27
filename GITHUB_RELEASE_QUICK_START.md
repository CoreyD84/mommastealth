# 🚀 Quick Start: Publishing MommaStealth to GitHub

## ❌ **DON'T Use:** `mommastealth-debug.apk`
## ✅ **DO Use:** `mommastealth-release.apk`

---

## 📦 **3-Step Process:**

### **Step 1: Generate Signing Key (One-Time)**
```powershell
cd C:/Dev/MommaNettie_New
keytool -genkey -v -keystore mommastealth-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias mommastealth
```
**Save the passwords!** You'll need them.

---

### **Step 2: Build Release APK**

**Option A: Android Studio**
1. **Build** → **Select Build Variant** → Change to **release**
2. **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
3. Click **locate** when done

**Option B: Command Line**
```powershell
# Set passwords (replace with your actual passwords)
$env:KEYSTORE_PASSWORD = "your-password"
$env:KEY_PASSWORD = "your-password"

# Build
./gradlew :mommastealth:assembleRelease
```

APK location: `mommastealth/build/outputs/apk/release/mommastealth-release.apk`

---

### **Step 3: Upload to GitHub**

1. Go to your GitHub repo
2. **Releases** → **Create a new release**
3. Tag: `v1.0.0`
4. Title: `MommaStealth v1.0 - Child Protection APK`
5. **Attach** `mommastealth-release.apk`
6. **Publish release**

---

## 📊 **File Comparison:**

| File | Size | Use |
|------|------|-----|
| `mommastealth-debug.apk` | ~14 MB | ❌ Testing only |
| `mommastealth-release.apk` | ~8-10 MB | ✅ GitHub/Production |

---

## ⚠️ **Security Checklist:**

- [ ] Using **release** APK (not debug)
- [ ] Keystore file (`.jks`) is **NOT** in GitHub
- [ ] Passwords are **NOT** in `build.gradle.kts`
- [ ] `.gitignore` includes `*.jks`
- [ ] Tested on real device

---

## 🔗 **Download Link for Users:**

After publishing, users can download from:
```
https://github.com/YOUR-USERNAME/MommaNettie_New/releases/latest/download/mommastealth-release.apk
```

---

## 📝 **Installation Instructions for Users:**

1. Download `mommastealth-release.apk`
2. Enable "Install from Unknown Sources" on Android device
3. Install the APK
4. Grant permissions (Accessibility, Location, VPN)
5. Link to guardian account via QR code

---

**For detailed instructions, see:** `RELEASE_BUILD_INSTRUCTIONS.md`
