package com.lagradost.cloudstream3.ui.settings

import com.lagradost.cloudstream4.viewmodels.isNewerThan
import org.junit.Assert.*
import org.junit.Test
import kotlinx.serialization.json.Json

class ForkUpdatesTest {
    private val prefix = "https://github.com/${ForkUpdates.OWNER}/${ForkUpdates.REPOSITORY}/releases/download/v4.8.0.6/"
    private fun release(date: String = "2026-10-17T18:00:00Z") = ForkUpdates.Release(
        tag = "v4.8.0.6", nodeId = "release-6", publishedAt = date,
        assets = listOf(ForkUpdates.Asset("update.json", prefix + "update.json"),
            ForkUpdates.Asset("e-cloudstream-4.8.0.apk", prefix + "e-cloudstream-4.8.0.apk")),
    )
    private fun manifest() = ForkUpdates.Manifest(ForkUpdates.PACKAGE, 72, "4.8.0",
        "e-cloudstream-4.8.0.apk", "a".repeat(64), ForkUpdates.CERTIFICATE_SHA256)

    @Test fun onlyTheRenamedForkRepositoryIsTrusted() {
        assertEquals("mohamedyassine-benmiled", GithubReleases.APK_USERNAME)
        assertEquals("e-cloudstream-android", GithubReleases.APK_REPOSITORY)
        assertTrue(ForkUpdates.trustedDownload(prefix + "app.apk"))
        assertFalse(ForkUpdates.trustedDownload("https://github.com/recloudstream/cloudstream/releases/download/pre-release/app.apk"))
        assertFalse(ForkUpdates.trustedDownload(prefix.replace("github.com", "github.com.evil.test") + "app.apk"))
        assertFalse(ForkUpdates.trustedDownload(prefix.replace("https:", "http:") + "app.apk"))
        assertFalse(ForkUpdates.trustedDownload(prefix + "../../app.apk"))
        assertFalse(ForkUpdates.trustedDownload(prefix + "%2e%2e/app.apk"))
        assertFalse(ForkUpdates.trustedDownload(prefix + "app.apk?redirect=elsewhere"))
    }

    @Test fun newestManifestReleaseWinsWithoutRelyingOnListOrderOrStableTag() {
        val old = release("2026-10-16T18:00:00Z")
        val newest = release()
        assertEquals(newest, ForkUpdates.newestRelease(listOf(newest, old)))
        assertEquals(newest, ForkUpdates.newestRelease(listOf(old, newest)))
        assertEquals(old, ForkUpdates.newestRelease(listOf(old, newest.copy(draft = true))))
        assertNull(ForkUpdates.newestRelease(listOf(newest.copy(assets = newest.assets.drop(1)))))
    }

    @Test fun manifestBindsPackageSignerAssetAndChecksum() {
        val file = ForkUpdates.file(release(), manifest())
        assertEquals("sha256:" + "a".repeat(64), file.digest)
        assertEquals(prefix + "e-cloudstream-4.8.0.apk", file.downloadUrl)
        assertEquals(72L, file.versionCode)
        for (bad in listOf(manifest().copy(applicationId = "com.lagradost.cloudstream3"),
            manifest().copy(signingCertificateSha256 = "b".repeat(64)),
            manifest().copy(apkSha256 = "bad"), manifest().copy(apkName = "official.apk"))) {
            assertTrue(runCatching { ForkUpdates.file(release(), bad) }.isFailure)
        }
        assertTrue(runCatching { ForkUpdates.file(release().copy(assets = listOf(
            ForkUpdates.Asset(manifest().apkName, "https://example.org/app.apk"))), manifest()) }.isFailure)
    }

    @Test fun higherVersionCodeUpdatesEvenWhenDisplayVersionIsUnchanged() {
        val file = ForkUpdates.file(release(), manifest())
        assertTrue(file.isNewerThan(71))
        assertFalse(file.isNewerThan(72))
        assertFalse(file.isNewerThan(73))
        assertFalse(file.copy(versionCode = null).isNewerThan(71))
    }

    @Test fun githubAndPublishedManifestJsonDecodeIntoTheUpdatePolicy() {
        val json = Json { ignoreUnknownKeys = true }
        val release = json.decodeFromString<ForkUpdates.Release>("""{
            "tag_name":"v4.8.0.6", "node_id":"release-6", "published_at":"2026-10-17T18:00:00Z",
            "prerelease":true, "draft":false, "body":null, "assets":[
              {"name":"update.json","browser_download_url":"${prefix}update.json","size":500},
              {"name":"e-cloudstream-4.8.0.apk","browser_download_url":"${prefix}e-cloudstream-4.8.0.apk"}]
        }""")
        val manifest = json.decodeFromString<ForkUpdates.Manifest>("""{
            "applicationId":"${ForkUpdates.PACKAGE}","versionCode":72,"versionName":"4.8.0",
            "apkName":"e-cloudstream-4.8.0.apk","apkSha256":"${"a".repeat(64)}",
            "signingCertificateSha256":"${ForkUpdates.CERTIFICATE_SHA256}"
        }""")
        assertEquals(72L, ForkUpdates.file(release, manifest).versionCode)
        assertEquals("", ForkUpdates.file(release, manifest).changeLog)
    }
}
