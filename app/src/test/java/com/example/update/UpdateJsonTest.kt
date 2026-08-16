package com.example.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UpdateJsonTest {
    @Test
    fun parseGithubRelease_readsTagNotesAndApk() {
        val json = """
            {
              "tag_name": "v1.2",
              "html_url": "https://github.com/Minashin1120/code-editor-android/releases/tag/v1.2",
              "body": "検索を改善",
              "assets": [
                {
                  "name": "app-debug.apk",
                  "browser_download_url": "https://example.com/app-debug.apk"
                }
              ]
            }
        """.trimIndent()

        val update = UpdateJson.parseGithubRelease(json)!!
        assertEquals("1.2", update.versionName)
        assertEquals("https://github.com/Minashin1120/code-editor-android/releases/tag/v1.2", update.pageUrl)
        assertEquals("https://example.com/app-debug.apk", update.downloadUrl)
        assertEquals("検索を改善", update.notes)
    }

    @Test
    fun parseVersionManifest_readsFields() {
        val json = """
            {
              "versionName": "1.3",
              "versionCode": 4,
              "releaseUrl": "https://github.com/Minashin1120/code-editor-android/releases/tag/v1.3",
              "notes": "起動を高速化"
            }
        """.trimIndent()

        val update = UpdateJson.parseVersionManifest(json)!!
        assertEquals("1.3", update.versionName)
        assertEquals(4, update.versionCode)
        assertEquals("起動を高速化", update.notes)
    }

    @Test
    fun parseGithubRelease_returnsNullWhenTagMissing() {
        assertNull(UpdateJson.parseGithubRelease("""{"html_url":"https://example.com"}"""))
    }
}
