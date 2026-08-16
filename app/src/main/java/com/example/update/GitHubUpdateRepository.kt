package com.example.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class HttpStatusException(val code: Int) : IOException("HTTP $code")

fun interface UpdateHttpClient {
    suspend fun get(url: String): String
}

class OkHttpUpdateClient(
    private val client: OkHttpClient = defaultClient(),
) : UpdateHttpClient {
    override suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Cache-Control", "no-cache")
            .apply {
                if (url.contains("api.github.com")) {
                    val accept = if (url.contains("/contents/")) {
                        "application/vnd.github.raw"
                    } else {
                        "application/vnd.github+json"
                    }
                    header("Accept", accept)
                    header("X-GitHub-Api-Version", "2022-11-28")
                } else {
                    header("Accept", "application/json")
                }
            }
            .build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw HttpStatusException(response.code)
            body
        }
    }

    companion object {
        private const val USER_AGENT = "HTMLEditor-UpdateCheck"

        fun defaultClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .protocols(listOf(Protocol.HTTP_1_1))
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()
        }
    }
}

class GitHubUpdateRepository(
    private val currentVersionName: String,
    private val currentVersionCode: Int,
    private val httpClient: UpdateHttpClient,
) {
    suspend fun check(): UpdateCheckResult {
        val remotes = mutableListOf<RemoteUpdate>()
        var lastStatus: HttpStatusException? = null
        var sawNetworkError = false
        var sawOtherError = false
        var otherMessage: String? = null

        for ((url, parser) in sources) {
            try {
                remotes += parser(httpClient.get(url))
            } catch (e: HttpStatusException) {
                lastStatus = e
            } catch (_: IOException) {
                sawNetworkError = true
            } catch (e: Exception) {
                sawOtherError = true
                otherMessage = e.localizedMessage
            }
        }

        val remote = newest(remotes)
        if (remote != null) {
            return if (AppVersion.isRemoteNewer(
                    remoteName = remote.versionName,
                    remoteCode = remote.versionCode,
                    localName = currentVersionName,
                    localCode = currentVersionCode,
                )
            ) {
                UpdateCheckResult.Available(remote)
            } else {
                UpdateCheckResult.UpToDate
            }
        }

        return when {
            lastStatus != null -> UpdateCheckResult.Error("サーバーに接続できませんでした (${lastStatus.code})")
            sawNetworkError -> UpdateCheckResult.Error("ネットワークに接続できませんでした")
            sawOtherError -> UpdateCheckResult.Error(otherMessage ?: "更新確認に失敗しました")
            else -> UpdateCheckResult.Error("公開中のバージョン情報を取得できませんでした")
        }
    }

    private val sources: List<Pair<String, (String) -> List<RemoteUpdate>>>
        get() = listOf(
            versionManifestUrl to { body -> listOfNotNull(UpdateJson.parseVersionManifest(body)) },
            versionManifestApiUrl to { body -> listOfNotNull(UpdateJson.parseVersionManifest(body)) },
            latestReleaseUrl to { body -> listOfNotNull(UpdateJson.parseGithubRelease(body)) },
            releasesUrl to { body -> UpdateJson.parseGithubReleases(body) },
        )

    companion object {
        const val OWNER = "Minashin1120"
        const val REPO = "code-editor-android"

        val repositoryWebUrl: String
            get() = "https://github.com/$OWNER/$REPO"

        val licenseWebUrl: String
            get() = "https://github.com/$OWNER/$REPO?tab=license"

        val latestReleaseUrl: String
            get() = "https://api.github.com/repos/$OWNER/$REPO/releases/latest"

        val releasesUrl: String
            get() = "https://api.github.com/repos/$OWNER/$REPO/releases?per_page=10"

        val versionManifestUrl: String
            get() = "https://raw.githubusercontent.com/$OWNER/$REPO/main/version.json"

        val versionManifestApiUrl: String
            get() = "https://api.github.com/repos/$OWNER/$REPO/contents/version.json?ref=main"

        internal fun newest(updates: List<RemoteUpdate>): RemoteUpdate? {
            return updates.maxWithOrNull { left, right -> compareUpdates(left, right) }
        }

        private fun compareUpdates(left: RemoteUpdate, right: RemoteUpdate): Int {
            val leftCode = left.versionCode?.takeIf { it > 0 }
            val rightCode = right.versionCode?.takeIf { it > 0 }
            if (leftCode != null && rightCode != null) return leftCode.compareTo(rightCode)
            return AppVersion.compareNames(left.versionName, right.versionName)
        }
    }
}
