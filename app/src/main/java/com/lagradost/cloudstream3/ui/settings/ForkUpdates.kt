package com.lagradost.cloudstream3.ui.settings

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream4.viewmodels.GithubFile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URI

/** Only this fork's persistently signed APK releases are eligible for in-app updates. */
object ForkUpdates {
    const val OWNER = "mohamedyassine-benmiled"
    const val REPOSITORY = "e-cloudstream-android"
    const val PACKAGE = "com.lagradost.cloudstream3.eimon"
    const val CERTIFICATE_SHA256 = "a5d9b34c09bdfe98c794c6752b82464f2fc66df8887c5f97b718bf3243bd30f7"
    const val RELEASES_API = "https://api.github.com/repos/$OWNER/$REPOSITORY/releases"

    @Serializable
    data class Asset(
        @JsonProperty("name") @SerialName("name") val name: String,
        @JsonProperty("browser_download_url") @SerialName("browser_download_url") val url: String,
    )

    @Serializable
    data class Release(
        @JsonProperty("tag_name") @SerialName("tag_name") val tag: String,
        @JsonProperty("node_id") @SerialName("node_id") val nodeId: String,
        @JsonProperty("published_at") @SerialName("published_at") val publishedAt: String = "",
        @JsonProperty("body") @SerialName("body") val body: String? = null,
        @JsonProperty("draft") @SerialName("draft") val draft: Boolean = false,
        @JsonProperty("assets") @SerialName("assets") val assets: List<Asset> = emptyList(),
    )

    @Serializable
    data class Manifest(
        val applicationId: String,
        val versionCode: Long,
        val versionName: String,
        val apkName: String,
        val apkSha256: String,
        val signingCertificateSha256: String,
    )

    fun trustedDownload(url: String): Boolean = runCatching {
        val uri = URI(url)
        uri.scheme == "https" && uri.host == "github.com" && uri.port == -1 &&
            uri.userInfo == null && uri.rawQuery == null && uri.rawFragment == null &&
            uri.rawPath.startsWith("/$OWNER/$REPOSITORY/releases/download/") &&
            uri.path.split('/').none { it == ".." || it == "." }
    }.getOrDefault(false)

    /** Ignore old temporary-key releases (no manifest), drafts, and foreign assets. */
    fun newestRelease(releases: List<Release>): Release? = releases
        .filter { !it.draft && it.assets.count { a -> a.name == "update.json" && trustedDownload(a.url) } == 1 }
        .maxByOrNull { it.publishedAt }

    fun file(release: Release, manifest: Manifest): GithubFile {
        require(!release.draft && manifest.applicationId == PACKAGE)
        require(manifest.signingCertificateSha256 == CERTIFICATE_SHA256)
        require(manifest.versionCode > 0 && manifest.versionName.isNotBlank())
        require(Regex("[a-f0-9]{64}").matches(manifest.apkSha256))
        require(manifest.apkName.startsWith("e-cloudstream-") && manifest.apkName.endsWith(".apk"))
        val asset = release.assets.single { it.name == manifest.apkName }
        require(trustedDownload(asset.url))
        return GithubFile(
            digest = "sha256:${manifest.apkSha256}", downloadUrl = asset.url,
            displayName = manifest.versionName, changeLog = release.body.orEmpty(),
            tagName = release.tag, nodeId = release.nodeId, versionCode = manifest.versionCode,
        )
    }

    suspend fun getRelease(): GithubFile {
        val headers = mapOf("Accept" to "application/vnd.github+json")
        val releases = app.get(RELEASES_API, headers = headers, cacheTime = 0).parsed<Array<Release>>()
        val release = newestRelease(releases.toList()) ?: error("No signed e-cloudstream update release available")
        val metadata = release.assets.single { it.name == "update.json" && trustedDownload(it.url) }
        val manifest = app.get(metadata.url, cacheTime = 0).parsed<Manifest>()
        return file(release, manifest)
    }
}
