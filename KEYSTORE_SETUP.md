# 🔑 Keystore Configuration for MommaStealth

Since you already have a keystore, you just need to configure it:

## 📝 **What You Need to Know About Your Keystore:**

1. **File location** - Where is your `.jks` or `.keystore` file?
2. **Keystore password** - The password you set when creating it
3. **Key alias** - The alias name (e.g., "mommastealth", "upload", "release")
4. **Key password** - The password for the specific key (often same as keystore password)

---

## ⚙️ **Configuration Steps:**

### **Step 1: Place Your Keystore**

**Option A: Copy to project root (Recommended)**
```powershell
# Copy your existing keystore
Copy-Item "C:/path/to/your/keystore.jks" -Destination "C:/Dev/MommaNettie_New/mommastealth-release-key.jks"
```

**Option B: Use absolute path**
- Keep keystore in its current location
- Update path in Step 2

---

### **Step 2: Update build.gradle.kts**

Open `mommastealth/build.gradle.kts` and update the `signingConfigs` section:

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("../mommastealth-release-key.jks")  // ← Update this path
        storePassword = "your-actual-keystore-password"       // ← Update this
        keyAlias = "your-key-alias"                           // ← Update this
        keyPassword = "your-actual-key-password"              // ← Update this
    }
}
```

**Example with absolute path:**
```kotlin
storeFile = file("C:/Users/YourName/keystores/my-keystore.jks")
```

---

### **Step 3: Build Release APK**

**In Android Studio:**
1. **Build** → **Select Build Variant** → Select **release**
2. **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**

**Command Line:**
```powershell
cd C:/Dev/MommaNettie_New
./gradlew :mommastealth:assembleRelease
```

**Output location:**
```
mommastealth/build/outputs/apk/release/mommastealth-release.apk
```

---

## 🔒 **Security Best Practices:**

### **Option 1: Environment Variables (Most Secure)**

**Set passwords as environment variables:**
```powershell
# PowerShell - Set for current session
$env:KEYSTORE_PASSWORD = "your-password"
$env:KEY_PASSWORD = "your-password"

# Or set permanently
[System.Environment]::SetEnvironmentVariable('KEYSTORE_PASSWORD', 'your-password', 'User')
[System.Environment]::SetEnvironmentVariable('KEY_PASSWORD', 'your-password', 'User')
```

**Then in build.gradle.kts:**
```kotlin
signingConfigs {
    create("release") {
        storeFile = file("../mommastealth-release-key.jks")
        storePassword = System.getenv("KEYSTORE_PASSWORD")
        keyAlias = "mommastealth"
        keyPassword = System.getenv("KEY_PASSWORD")
    }
}
```

### **Option 2: keystore.properties File**

**Create `keystore.properties` in project root:**
```properties
storeFile=../mommastealth-release-key.jks
storePassword=your-keystore-password
keyAlias=your-key-alias
keyPassword=your-key-password
```

**Update build.gradle.kts:**
```kotlin
// Load keystore properties
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }
}
```

**⚠️ Make sure `keystore.properties` is in `.gitignore`!** (Already added)

---

## 🧪 **Test Your Configuration:**

```powershell
# Clean build
./gradlew clean

# Build release APK
./gradlew :mommastealth:assembleRelease

# If successful, you'll see:
# BUILD SUCCESSFUL
# APK location: mommastealth/build/outputs/apk/release/mommastealth-release.apk
```

---

## ❌ **Common Errors:**

**"Keystore file not found"**
- Check the path in `storeFile`
- Use absolute path if relative path doesn't work

**"Incorrect keystore password"**
- Verify your password is correct
- Check for typos

**"Key alias not found"**
- List aliases in your keystore:
```powershell
keytool -list -v -keystore path/to/your/keystore.jks
```

**"Key password incorrect"**
- Key password might be different from keystore password
- Try using the same password for both

---

## 📋 **Quick Checklist:**

- [ ] Keystore file is accessible
- [ ] Updated `storeFile` path in build.gradle.kts
- [ ] Updated `storePassword` in build.gradle.kts
- [ ] Updated `keyAlias` in build.gradle.kts
- [ ] Updated `keyPassword` in build.gradle.kts
- [ ] Keystore file is in `.gitignore`
- [ ] Passwords are NOT committed to GitHub
- [ ] Successfully built release APK
- [ ] Tested APK on device

---

## 🚀 **Ready to Build?**

Once configured, just run:
```powershell
./gradlew :mommastealth:assembleRelease
```

Your signed release APK will be ready for GitHub! 🎉
