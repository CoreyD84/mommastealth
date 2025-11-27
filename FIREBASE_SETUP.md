# Firebase Setup for MommaStealth

## Important: Add Child App to Firebase Console

The `mommastealth` module needs to be registered in Firebase as a separate Android app.

### Steps to Add Child App to Firebase:

1. **Go to Firebase Console**
   - Visit: https://console.firebase.google.com/
   - Select your project: `air-nettie`

2. **Add Android App**
   - Click the gear icon (⚙️) → Project settings
   - Scroll to "Your apps" section
   - Click "Add app" → Select Android icon

3. **Register App**
   - **Package name:** `com.airnettie.mobile.child.mommastealth`
   - **App nickname:** MommaStealth Child (optional)
   - **Debug signing certificate SHA-1:** (optional, for now)
   - Click "Register app"

4. **Download google-services.json**
   - Download the NEW `google-services.json` file
   - **IMPORTANT:** This file will now contain BOTH apps:
     - `com.airnettie.mobile` (Guardian app)
     - `com.airnettie.mobile.child.mommastealth` (Child app)

5. **Replace Files**
   - Replace `app/google-services.json` with the new file
   - Replace `mommastealth/google-services.json` with the new file
   - Both modules should use the SAME file (it contains both apps)

### Alternative: Use Shared Package Name

If you don't want to add a separate Firebase app, you can change the child app to use the same package name:

**Option 1: Change mommastealth package to match guardian**

Edit `mommastealth/build.gradle.kts`:
```kotlin
defaultConfig {
    applicationId = "com.airnettie.mobile"  // Same as guardian
    // ...
}
```

**Note:** This means both apps will share the same Firebase project and authentication, which might be what you want anyway.

### Current Status

The temporary `google-services.json` in the mommastealth folder has been updated with a placeholder entry. You need to:

1. Either add the child app to Firebase Console (recommended)
2. Or change the package name to match the guardian app

### Why This Matters

- Firebase uses the package name to identify which app is connecting
- Each unique package name needs to be registered in Firebase Console
- The `google-services.json` file contains configuration for all registered apps
- Without proper registration, Firebase services (Auth, Database) won't work

### Testing Without Firebase (Temporary)

If you want to test the build without Firebase first, you can temporarily comment out the Firebase plugin in `mommastealth/build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    // id("com.google.gms.google-services")  // Commented out temporarily
}
```

And remove Firebase dependencies. But this will break the linking functionality.
