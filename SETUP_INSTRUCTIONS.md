# MommaStealth Setup Instructions

This guide will help you set up the MommaStealth child app for distribution via GitHub.

## 📋 Prerequisites

1. A GitHub account
2. Your repository pushed to GitHub
3. GitHub Pages enabled for your repository

## 🔧 Step 1: Update Configuration Files

### 1.1 Update the GitHub Workflow
The workflow file is already created at `.github/workflows/build-mommastealth.yml`

You need to set up signing keys as GitHub Secrets:
- `SIGNING_KEY` - Base64 encoded keystore file
- `ALIAS` - Keystore alias
- `KEY_STORE_PASSWORD` - Keystore password
- `KEY_PASSWORD` - Key password

### 1.2 Update the Download Page
Edit `link/index.html` and replace:
```javascript
const githubUser = 'YOUR_GITHUB_USERNAME';
const githubRepo = 'MommaNettie_New';
```

With your actual GitHub username and repository name.

### 1.3 Update the Guardian App
Edit `app/src/main/java/com/airnettie/mobile/features/guardian/activities/GenerateLinkQrActivity.kt`

Replace `YOUR_GITHUB_USERNAME` with your actual GitHub username on line 31.

## 🔑 Step 2: Create Android Signing Keys

If you don't have a keystore, create one:

```bash
keytool -genkey -v -keystore mommastealth.keystore -alias mommastealth -keyalg RSA -keysize 2048 -validity 10000
```

Then convert it to Base64 for GitHub Secrets:

**Windows PowerShell:**
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("mommastealth.keystore")) | Set-Clipboard
```

**Linux/Mac:**
```bash
base64 mommastealth.keystore | pbcopy
```

## 🔐 Step 3: Add GitHub Secrets

1. Go to your GitHub repository
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add these secrets:
   - `SIGNING_KEY`: Paste the Base64 encoded keystore
   - `ALIAS`: Your keystore alias (e.g., "mommastealth")
   - `KEY_STORE_PASSWORD`: Your keystore password
   - `KEY_PASSWORD`: Your key password

## 🌐 Step 4: Enable GitHub Pages

1. Go to your repository **Settings**
2. Scroll to **Pages** section
3. Under **Source**, select **Deploy from a branch**
4. Select branch: **main** (or **master**)
5. Select folder: **/ (root)**
6. Click **Save**

Your download page will be available at:
`https://YOUR_GITHUB_USERNAME.github.io/MommaNettie_New/link/`

## 🚀 Step 5: Build and Release

### Option A: Automatic Build (Recommended)
1. Push changes to your repository:
   ```bash
   git add .
   git commit -m "Setup MommaStealth standalone app"
   git push
   ```

2. The GitHub Action will automatically:
   - Build the APK
   - Sign it
   - Create a release
   - Upload the APK

### Option B: Manual Build
1. Build locally:
   ```bash
   ./gradlew :mommastealth:assembleRelease
   ```

2. Sign the APK manually
3. Create a GitHub release manually
4. Upload the signed APK

## 📱 Step 6: Test the Flow

1. **Generate QR Code**: Open the guardian app and generate a link QR code
2. **Scan QR Code**: Scan it with a child device
3. **Download Page**: Should open the download page
4. **Download APK**: Click download button
5. **Install**: Install the APK (enable Unknown Sources if needed)
6. **Auto-Link**: The app should automatically link to the guardian device

## 🔄 How It Works

```
Guardian App
    ↓
Generates QR Code with URL:
https://YOUR_USERNAME.github.io/MommaNettie_New/link/?guardianId=XXX&token=YYY
    ↓
Child Scans QR
    ↓
Opens Download Page (link/index.html)
    ↓
Downloads APK from:
https://github.com/YOUR_USERNAME/MommaNettie_New/releases/latest/download/mommastealth-release.apk
    ↓
Installs APK
    ↓
Opens Deep Link:
nettielink://child/link?guardianId=XXX&token=YYY
    ↓
App Links to Guardian Device via Firebase
```

## 🛠️ Troubleshooting

### Build Fails
- Check that all secrets are set correctly
- Verify the keystore is valid
- Check GitHub Actions logs

### Download Fails
- Ensure GitHub Pages is enabled
- Verify the release was created
- Check the APK was uploaded to the release

### App Won't Install
- Enable "Install from Unknown Sources"
- Check Android version compatibility (min SDK 24)
- Verify APK is signed correctly

### App Won't Link
- Check Firebase configuration
- Verify `google-services.json` is in mommastealth folder
- Check network connectivity
- Verify guardian ID and token are valid

## 📝 Notes

- The child app is now completely separate from the guardian app
- Each push to the `mommastealth` folder triggers a new build
- Releases are automatically versioned
- The app runs in stealth mode (invisible to child)
- All permissions must be granted for full functionality

## 🔒 Security Considerations

- Keep your signing keys secure
- Never commit keystore files to the repository
- Use GitHub Secrets for sensitive data
- Regularly update dependencies
- Monitor Firebase security rules
