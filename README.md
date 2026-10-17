# e-cloudstream for Android

An independent, unofficial Android/TV app forked from [recloudstream/cloudstream](https://github.com/recloudstream/cloudstream).

**e-cloudstream is developed around features conceived and architected by [@mohamedyassine-benmiled](https://github.com/mohamedyassine-benmiled).** Its direction includes website-specific account integration and cross-device cloud progress, rather than changes intended for every CloudStream user. These integrations are specific to certain websites: **there is no expectation or plan to submit this fork's features as pull requests to the upstream app.** Upstream fixes and improvements are still merged into this fork before new feature work.

This is not an official CloudStream release or an upstream-endorsed project. Please report fork-specific issues [here](../../issues), not to upstream.

## Download and updates

Use [this repository's Releases](https://github.com/mohamedyassine-benmiled/e-cloudstream-android/releases) for the APK and `SHA256SUMS.txt`. The app is named **e-cloudstream**. New version labels no longer contain `-eimon`; older releases keep their original names. The package stays `com.lagradost.cloudstream3.eimon`, separate from official CloudStream, to preserve the fork's installation identity.

New APKs use one persistent private signing key stored through GitHub Actions secrets. Future releases can update installations signed with that same key. **Older temporary-debug-key installations cannot be updated with the new key:** first export/back up supported app data, confirm the backup is usable, then perform a one-time reinstall. Saved extension sessions may not be included in backups; expect to sign in again. Do not uninstall before protecting your data. See [Android signing](docs/ANDROID_SIGNING.md).

Automatic and manual update checks use **this fork's repository**, including its prereleases, never the official app. Releases publish an `update.json` manifest with the package, version code, signing identity and APK SHA-256. Only a higher version code is offered, even if the displayed version is unchanged. Downloads must belong to this repository and pass SHA-256 verification; Android still enforces APK signing compatibility. Old temporary-key releases without a manifest are ignored. “Install official prerelease” is disabled in this fork. Update checks notify you; installation still follows Android's installer/permission rules.

The APK remains an experimental `stableNativeTest` build. Automated tests and APK assembly are verified; signed-in phone/TV acceptance for cloud details remains pending. The app includes no content sources: install extensions separately.

## Fork features

- Provider-owned account **Continue Watching**, with progress and episode/timestamp resume.
- Cloud series-page progress: earlier same-season/same-audio episodes are inferred watched in memory, not imported into local history.
- Native **Watching – New Episodes** starts the newest eligible episode in the saved audio edition at **00:00** after revalidation.
- Future-episode spoilers are hidden by default; episode options can show/hide cloud spoilers.

Account features require compatible extensions, currently AniKoto from [the Eimon extension repository](https://github.com/mohamedyassine-benmiled/eimon-cloudstream-repo). Ordinary extensions continue to work but do not automatically gain account integration. Website login, episode matching and optional uploads remain in the extension. Viewing rows/details does not advance cloud history. Optional uploads remain position-based, not immediate pause/exit reporting, and have no durable offline queue.

## Development

`main` is the maintained default branch. Read [AGENTS.md](AGENTS.md), [AI-POLICY.md](AI-POLICY.md) and [the native-resume design](docs/NATIVE_EXTENSION_RESUME.md) before changing code. Check and merge upstream updates before implementation, preserving the fork's website-specific behaviour.

The APK workflow runs tests and publishes a persistently signed prerelease on pushes to `main` or manual dispatch. It refuses to publish without all signing secrets; private keys never belong in commits or artifacts.

## Credits and license

Credit for the original app, engine and inherited functionality belongs to reCloudStream and other upstream contributors. This fork's product direction does not claim authorship of their code. Original notices and the [GPL-3.0 license](LICENSE) are retained. [Archived pre-rename documentation](docs/LEGACY_README.md) is available for reference; its support/community links belong to upstream, not this fork.
