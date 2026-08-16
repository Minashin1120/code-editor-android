package com.example.update

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubUpdateRepositoryTest {
    @Test
    fun check_usesGithubReleaseWhenNewer() = runTest {
        val repository = GitHubUpdateRepository(
            currentVersionName = "1.1",
            currentVersionCode = 2,
            httpClient = { url ->
                when {
                    url.contains("/releases/latest") ->
                        """{"tag_name":"v1.2","html_url":"https://example.com/v1.2","body":"fix"}"""
                    url.contains("version.json") ->
                        """{"versionName":"1.1","versionCode":2}"""
                    url.contains("/releases") ->
                        """[{"tag_name":"v1.2","html_url":"https://example.com/v1.2"}]"""
                    else -> error("unexpected $url")
                }
            },
        )

        val result = repository.check()
        val available = result as UpdateCheckResult.Available
        assertEquals("1.2", available.update.versionName)
        assertEquals("https://example.com/v1.2", available.update.pageUrl)
    }

    @Test
    fun check_fallsBackToVersionJsonWhenReleaseMissing() = runTest {
        val repository = GitHubUpdateRepository(
            currentVersionName = "1.1",
            currentVersionCode = 2,
            httpClient = { url ->
                if (url.contains("/releases")) {
                    throw HttpStatusException(404)
                }
                """{"versionName":"1.3","versionCode":5,"releaseUrl":"https://example.com/v1.3"}"""
            },
        )

        val result = repository.check()
        val available = result as UpdateCheckResult.Available
        assertEquals("1.3", available.update.versionName)
        assertEquals(5, available.update.versionCode)
    }

    @Test
    fun check_fallsBackToVersionJsonWhenReleaseApiIsForbidden() = runTest {
        val repository = GitHubUpdateRepository(
            currentVersionName = "1.1",
            currentVersionCode = 2,
            httpClient = { url ->
                if (url.contains("api.github.com")) {
                    throw HttpStatusException(403)
                }
                """{"versionName":"1.4","versionCode":6,"releaseUrl":"https://example.com/v1.4"}"""
            },
        )

        val result = repository.check()
        val available = result as UpdateCheckResult.Available
        assertEquals("1.4", available.update.versionName)
        assertEquals(6, available.update.versionCode)
    }

    @Test
    fun check_returnsUpToDateWhenRemoteMatches() = runTest {
        val repository = GitHubUpdateRepository(
            currentVersionName = "1.1",
            currentVersionCode = 2,
            httpClient = {
                """{"tag_name":"v1.1","html_url":"https://example.com/v1.1"}"""
            },
        )

        assertEquals(UpdateCheckResult.UpToDate, repository.check())
    }

    @Test
    fun newest_prefersHigherVersionCode() {
        val older = RemoteUpdate("1.9", 2, "https://example.com/old", null, null)
        val newer = RemoteUpdate("1.2", 5, "https://example.com/new", null, null)
        val selected = GitHubUpdateRepository.newest(listOf(older, newer))
        assertEquals("1.2", selected?.versionName)
        assertEquals(5, selected?.versionCode)
    }

    @Test
    fun publicUrls_pointToThisRepository() {
        assertEquals(
            "https://github.com/Minashin1120/code-editor-android",
            GitHubUpdateRepository.repositoryWebUrl,
        )
        assertEquals(
            "https://github.com/Minashin1120/code-editor-android?tab=license",
            GitHubUpdateRepository.licenseWebUrl,
        )
    }

    @Test
    fun check_returnsErrorOnNetworkFailure() = runTest {
        val repository = GitHubUpdateRepository(
            currentVersionName = "1.1",
            currentVersionCode = 2,
            httpClient = { throw java.io.IOException("offline") },
        )

        val result = repository.check()
        assertTrue(result is UpdateCheckResult.Error)
    }
}
