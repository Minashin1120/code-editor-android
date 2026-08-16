package com.example.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
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
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", USER_AGENT)
            .header("X-GitHub-Api-Version", "2022-11-28")
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
        return try {
            val remote = fetchLatestRelease() ?: fetchVersionManifest()
                ?: return UpdateCheckResult.Error("公開中のバージョン情報を取得できませんでした")
            if (AppVersion.isRemoteNewer(
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
        } catch (e: HttpStatusException) {
            UpdateCheckResult.Error("サーバーに接続できませんでした (${e.code})")
        } catch (e: IOException) {
            UpdateCheckResult.Error("ネットワークに接続できませんでした")
        } catch (e: Exception) {
            UpdateCheckResult.Error(e.localizedMessage ?: "更新確認に失敗しました")
        }
    }

    private suspend fun fetchLatestRelease(): RemoteUpdate? {
        return try {
            val json = httpClient.get(latestReleaseUrl)
            UpdateJson.parseGithubRelease(json)
        } catch (e: HttpStatusException) {
            if (e.code == 404) null else throw e
        }
    }

    private suspend fun fetchVersionManifest(): RemoteUpdate? {
        return try {
            val json = httpClient.get(versionManifestUrl)
            UpdateJson.parseVersionManifest(json)
        } catch (e: HttpStatusException) {
            if (e.code == 404) null else throw e
        }
    }

    companion object {
        const val OWNER = "Minashin1120"
        const val REPO = "code-editor-android"

        val latestReleaseUrl: String
            get() = "https://api.github.com/repos/$OWNER/$REPO/releases/latest"

        val versionManifestUrl: String
            get() = "https://raw.githubusercontent.com/$OWNER/$REPO/main/version.json"
    }
}
