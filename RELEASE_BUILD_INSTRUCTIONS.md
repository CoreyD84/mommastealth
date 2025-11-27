# MommaStealth APK - Release Build Instructions
## 🎯 Quick Answer: Which APK for GitHub?

**For GitHub Release:** Use `mommastealth-release.apk` (NOT the debug version)

---

## 📦 Building the Release APK

### **Step 1: Generate Signing Key (One-Time Setup)**

```powershell
# Navigate to project root
cd C:/Dev/MommaNettie_New

# Generate release keystore
keytool -genkey -v -keystore mommastealth-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias mommastealth
```

**You'll be asked for:**
- Keystore password (choose a strong password)
- Key password (can be same as keystore password)
- Your name, organization, etc.

**⚠️ IMPORTANT:**
- Save these passwords securely!
- Never commit the `.jks` file to GitHub!
- Backup the keystore file - you can't regenerate it!

---

### **Step 2: Set Environment Variables (Recommended)**

**Windows PowerShell:**
```powershell
# Set for current session
$env:KEYSTORE_PASSWORD = "your-keystore-password"
$env:KEY_PASSWORD = "your-key-password"

# Or set permanently (User level)
[System.Environment]::SetEnvironmentVariable('KEYSTORE_PASSWORD', 'your-keystore-password', 'User')
[System.Environment]::SetEnvironmentVariable('KEY_PASSWORD', 'your-key-password', 'User')
```

**Alternative: Edit build.gradle.kts directly (Less secure)**
Replace the environment variable lines with your actual passwords (but don't commit this!)

---

### **Step 3: Build Release APK**

**Option A: Android Studio**
1. Open Android Studio
2. Select **Build** → **Select Build Variant**
3. Change `mommastealth` to **release**
4. Select **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
5. Wait for build to complete
6. Click **locate** in the notification

**Option B: Command Line**
```powershell
# Navigate to project
cd C:/Dev/MommaNettie_New

# Build release APK
./gradlew :mommastealth:assembleRelease

# APK will be at:
# mommastealth/build/outputs/apk/release/mommastealth-release.apk
```

---

## 📤 Publishing to GitHub

### **Step 1: Create GitHub Release**

1. Go to your GitHub repository
2. Click **Releases** → **Create a new release**
3. Tag version: `v1.0.0`
4. Release title: `MommaStealth v1.0 - Child Protection APK`
5. Description:

```markdown
# MommaStealth v1.0

## 🛡️ Child-Side Protection App

MommaStealth is the child-side component of MommaNettie that provides:
- ✅ Real-time message monitoring for grooming, bullying, and predators
- ✅ AI-powered intervention (MommaTakeover)
- ✅ App blocking (Messenger, Discord, Roblox, TikTok, etc.)
- ✅ SafeScope VPN for web filtering
- ✅ Location tracking
- ✅ Stealth operation

## 📥 Installation

1. Download `mommastealth-release.apk`
2. Enable "Install from Unknown Sources" on child's device
3. Install the APK
4. Grant required permissions (Accessibility, Location, VPN)
5. Link to guardian account via QR code

## ⚠️ Requirements

- Android 7.0 (API 24) or higher
- Guardian must have MommaNettie main app installed
- Active internet connection
- Firebase account configured

## 🔐 Permissions Required

- Accessibility Service (for message monitoring and app blocking)
- Location (for tracking)
- VPN (for SafeScope web filtering)
- Internet (for Firebase sync)

## 📱 App ID

`com.airnettie.mobile.child.mommastealth`

## 🔗 Links

- [Main Guardian App](link-to-main-app)
- [Documentation](link-to-docs)
- [Support](link-to-support)
```

6. **Attach the APK:**
   - Drag and drop `mommastealth-release.apk` into the release
   - Or click "Attach binaries" and select the APK

7. Click **Publish release**

---

## 📊 APK Comparison

| Type | File | Size | Use Case |
|------|------|------|----------|
| **Debug** | `mommastealth-debug.apk` | ~14 MB | Testing only |
| **Release** | `mommastealth-release.apk` | ~8-10 MB | Production/GitHub |

---

## 🔒 Security Checklist

Before publishing:

- [ ] Built with release configuration
- [ ] Signed with release keystore
- [ ] ProGuard/R8 enabled (code obfuscation)
- [ ] Keystore file NOT in repository
- [ ] Passwords NOT in build.gradle.kts
- [ ] `.gitignore` includes `*.jks`
- [ ] Tested on real device
- [ ] All features working (FeelScope, AppBlocker, SafeScope, Location)
- [ ] Firebase configured correctly
- [ ] Version code incremented

---

## 🚀 Distribution Options

### **1. GitHub Releases (Current)**
- ✅ Free
- ✅ Version control
- ✅ Easy updates
- ❌ Users must enable "Unknown Sources"

### **2. Google Play Store (Future)**
- ✅ Trusted source
- ✅ Automatic updates
- ✅ Better discovery
- ❌ Requires developer account ($25 one-time)
- ❌ Review process (can take days)
- ⚠️ Accessibility services may require justification

### **3. Direct Download (Website)**
- ✅ Full control
- ✅ Custom landing page
- ❌ Users must enable "Unknown Sources"
- ❌ Hosting costs

---

## 📝 Version Management

Update these before each release:

**In `mommastealth/build.gradle.kts`:**
```kotlin
versionCode = 2  // Increment by 1
versionName = "1.1"  // Update version number
```

**Versioning scheme:**
- Major: `1.0` → `2.0` (breaking changes)
- Minor: `1.0` → `1.1` (new features)
- Patch: `1.0.0` → `1.0.1` (bug fixes)

---

## 🆘 Troubleshooting

**"Keystore not found"**
- Make sure `mommastealth-release-key.jks` is in project root
- Check the path in `build.gradle.kts`

**"Incorrect keystore password"**
- Verify environment variables are set
- Or update passwords in `build.gradle.kts`

**"APK not signed"**
- Make sure you're building `release` variant, not `debug`
- Check signing config in `build.gradle.kts`

**"Build failed"**
- Run `./gradlew clean`
- Then `./gradlew :mommastealth:assembleRelease`

---

## 📞 Support

For issues or questions:
- GitHub Issues: [link]
- Email: [your-email]
- Documentation: [link]
