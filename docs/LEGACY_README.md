# Archived README (before e-cloudstream branding)

Historical documentation only. Versions, signing instructions, release links and upstream community links below may be outdated. Use the repository-root README for current project scope and downloads.

This repository is an **unofficial fork** of [recloudstream/cloudstream](https://github.com/recloudstream/cloudstream), not an official CloudStream release or an upstream-endorsed project. Report fork-specific issues here, not to upstream.

## Provider-owned account Continue Watching

- Account Continue Watching is a normal Home row **inside the owning extension/provider**, not a global row. Both provider and plugin ownership are checked.
- Native cards retain episode/audio/progress information and automatic resume at the account's saved episode and timestamp. Fetching the row does not populate local watch history.
- Extensions that advertise the required account-list and resume capabilities can use this feature. Other extensions continue to work normally without account Continue Watching or remote auto-resume.
- Website login, episode matching and optional progress uploads remain the extension's responsibility. This host update does not add native player-progress reporting.

### Eimon downloads

**4.8.0-eimon.2 + a capability-compatible extension:** cloud episode progress drives series details and built-in Resume, with earlier same-season/same-audio episodes inferred watched in memory. Future episode spoilers are hidden by default; episode options can show/hide them. Native **Watching – New Episodes** cards start the newest eligible same-edition episode at zero. Viewing these rows/pages does not import watches into local history or advance website progress. Stock/older hosts retain the ordinary row. Automated tests and APK assembly are verified; signed-in Android/TV acceptance testing remains pending.

Use **[this fork's Releases](https://github.com/mohamedyassine-benmiled/eimon-cloudstream-android/releases)** for the `-eimon` APK and `SHA256SUMS.txt`. The Native test APK workflow tests and publishes a GitHub prerelease on each push to `feature/native-extension-resume`, or when manually dispatched. The APK enables native account resume and uses the separate package `com.lagradost.cloudstream3.eimon`; it does not replace the official app.

These are debug-signed test builds, not official or production-signed releases. CI debug signing keys are not persistent across runners, so an APK may not update an existing test installation signed with a different key. Preserve your app data before changing installations.

The documentation and links below are inherited from upstream; upstream download links do **not** contain the Eimon feature.

**⚠️ Warning: By default, this app doesn't provide any video sources; you have to install extensions to add functionality to the app.**
<p align="left"><a href="https://github.com/recloudstream/cloudstream/releases/download/pre-release/app-prerelease-release.apk"><img src=".github/images/download-cloudstream-pre.png" alt="Download Cloudstream Pre-Release" width=430/></a></p>

[![Discord](https://invidget.switchblade.xyz/5Hus6fM)](https://discord.gg/5Hus6fM)


## Table of Contents:
+ [About Us:](#about_us)
+ [Installation Steps:](#install_rules)
+ [Contributing:](#contributing)
+ [Issues:](#issues)
  + [Bugs Reports:](#bug_report)
  + [Enhancement:](#enhancment)
+ [Extension Development:](#extensions)
+ [Language Support:](#languages)
+ [Further Sources](#contact_and_sources)


<a id="about_us"></a>

## About us:

**CloudStream is a media center that prioritizes and emphasizes complete freedom and flexibility for users and developers.**

CloudStream is an extension-based multimedia player with tracking support. There are extensions to view videos from:

+ [Librevox (audio-books)](https://librivox.org/)
+ [Youtube](https://www.youtube.com/)
+ [Twitch](https://www.twitch.tv/)
+ [iptv-org (A collection of publicly available IPTV (Internet Protocol television) channels from all over the world.)](https://github.com/iptv-org/iptv)
+ [nginx](https://nginx.org/)
+ And more...


**Please don't create illegal extensions or use any that host any copyrighted media.** For more details about our stance on the DMCA and EUCD, you can read about it on our organization: [reCloudStream](https://github.com/recloudstream)

#### Important Copyright Note:

Our documentation is unmaintained and open to contributions; therefore, apps and sources, extensions in recommended sources, and recommended apps are not officially moderated or endorsed by CloudStream; if you or another copyright owner identify an extension that breaches your copyright, please let us know.


#### Features:
+ **AdFree**, No ads whatsoever
+ No tracking/analytics
+ Bookmarks
+ Phone and TV support
+ Chromecast
+ Extension system for personal customization


<a id="install_rules"></a>

## Installation:

Our documentation provides the steps to install and configure CloudStream for your streaming needs.

[Getting Started With CloudStream:](https://recloudstream.github.io/csdocs/)

<a id="contributing"></a>

## Contributing:
We **happily** accept any contributions to our project. To find out where you can start contributing towards the project, please look [at our issues tab](/cloudstream/issues)



<a id="issues"></a>

### Issues:
While we **actively** accept issues and pull requests, we do require you fill out an [template](https://github.com/recloudstream/cloudstream/issues/new/choose) for issues. These include the following:

<a id="bug_report"></a>

- [Bug Report Template: ](https://github.com/recloudstream/cloudstream/issues/new?assignees=&labels=bug&projects=&template=application-bug.yml)
  - For bug reports, we want as much info as possible, including your downloaded version of CloudeStream, device and updated version (if possible, current API),
    expected behavior of the program, and the actual behavior that the program did, most importantly we require clear, reproducible steps of the bug. If your bug can't be       reproduced, it is unlikely we'll work on your issue.

<a id="enhancment"></a>

- [Feature Request Template: ](https://github.com/recloudstream/cloudstream/issues/new?assignees=&labels=enhancement&projects=&template=feature-request.yml)
  - Before adding a feature request, please check to see if a feature request already has been requested.


### Extensions:

**Further details on creating extensions for CloudStream are found in our documentation.**

[Guide: For Extension Developers](https://recloudstream.github.io/csdocs/devs/gettingstarted/)

<a id="contact_and_sources"></a>

## Further Sources:

As well as providing clear install steps, our [website](https://dweb.link/ipns/cloudstream.on.fleek.co/) includes a wide variety of other tools, such as:
- [Troubleshooting](https://recloudstream.github.io/csdocs/troubleshooting/)
- [Further CloudStream Repositories](https://recloudstream.github.io/csdocs/repositories/)
- Set-Up for other devices, such as:
  - [Android TV](https://recloudstream.github.io/csdocs/other-devices/tv/)
  - [Windows](https://recloudstream.github.io/csdocs/other-devices/windows/)
  - [Linux](https://recloudstream.github.io/csdocs/other-devices/linux/)
- And more...

<a id="languages"> </a>

### Supported languages:

Even if you can't contribute to the code or documentation, we always look for those who can contribute to translation and language support. Your contribution is exceptionally appreciated; you can check our translation from the figure below.

<a href="https://hosted.weblate.org/engage/cloudstream/">
  <img src="https://hosted.weblate.org/widgets/cloudstream/-/app/multi-auto.svg" alt="Translation status" />
</a>
