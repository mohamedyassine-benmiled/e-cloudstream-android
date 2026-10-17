# Persistent Eimon APK signing

The Native test APK workflow requires these **repository Actions secrets**:

- `EIMON_SIGNING_KEYSTORE_BASE64`: base64 of the private PKCS12 keystore.
- `EIMON_SIGNING_STORE_PASSWORD`: keystore password.
- `EIMON_SIGNING_KEY_ALIAS`: signing alias.
- `EIMON_SIGNING_KEY_PASSWORD`: private-key password.

The workflow refuses to publish without all four secrets. The decoded key lives only in the runner's temporary directory and is removed afterward; it is never an artifact. Keep an encrypted backup of the original keystore and passwords outside GitHub and the repository. Reuse this identity for every release; do not regenerate it for updates.

The public certificate's SHA-256 fingerprint is pinned in `docs/android-signing-certificate.sha256`. The workflow verifies the final APK against it before uploading or publishing. This fingerprint is public metadata, not a private key or password; do not change it to bypass an unexpected signer mismatch.

Local builds retain debug signing unless `EIMON_SIGNING_STORE_FILE` and the three password/alias environment variables are supplied. Do not distribute local debug-signed builds as updates to the persistently signed app. Never commit keystores or secret-value files.

The package remains `com.lagradost.cloudstream3.eimon`, the app is named **e-cloudstream**, and displayed versions no longer use `-eimon`. Updates require the same signing identity and an appropriate version code. The new key cannot update an older installation signed with a different temporary key: back up/export supported data before a one-time uninstall/reinstall, and expect to sign in again. Do not uninstall before confirming a usable backup; app backup may not include extension sessions.
