package com.example.update

internal object UpdateJson {
    fun parseGithubRelease(json: String): RemoteUpdate? {
        val tagName = stringField(json, "tag_name") ?: return null
        val versionName = AppVersion.normalize(tagName)
        if (versionName.isEmpty()) return null
        val htmlUrl = stringField(json, "html_url")
            ?: "https://github.com/${GitHubUpdateRepository.OWNER}/${GitHubUpdateRepository.REPO}/releases/latest"
        val notes = stringField(json, "body")
        val apkUrl = firstApkAssetUrl(json)
        return RemoteUpdate(
            versionName = versionName,
            versionCode = intField(json, "versionCode"),
            pageUrl = htmlUrl,
            downloadUrl = apkUrl,
            notes = notes?.trim()?.ifEmpty { null },
        )
    }

    fun parseGithubReleases(json: String): List<RemoteUpdate> {
        val trimmed = json.trim()
        if (trimmed.startsWith("[")) {
            return jsonObjects(trimmed).mapNotNull(::parseGithubRelease)
        }
        return listOfNotNull(parseGithubRelease(trimmed))
    }

    fun parseVersionManifest(json: String): RemoteUpdate? {
        val versionName = stringField(json, "versionName")?.let(AppVersion::normalize)
        if (versionName.isNullOrEmpty()) return null
        val pageUrl = stringField(json, "releaseUrl")
            ?: stringField(json, "downloadUrl")
            ?: "https://github.com/${GitHubUpdateRepository.OWNER}/${GitHubUpdateRepository.REPO}/releases/latest"
        return RemoteUpdate(
            versionName = versionName,
            versionCode = intField(json, "versionCode"),
            pageUrl = pageUrl,
            downloadUrl = stringField(json, "downloadUrl"),
            notes = stringField(json, "notes")?.trim()?.ifEmpty { null },
        )
    }

    private fun firstApkAssetUrl(json: String): String? {
        val assetsStart = json.indexOf("\"assets\"")
        if (assetsStart < 0) return null
        val assetsBlock = json.substring(assetsStart)
        val matcher = Regex(
            "\"browser_download_url\"\\s*:\\s*\"([^\"]+\\.apk)\"",
            RegexOption.IGNORE_CASE,
        ).find(assetsBlock)
        return matcher?.groupValues?.getOrNull(1)
    }

    private fun stringField(json: String, key: String): String? {
        val match = Regex("\"${Regex.escape(key)}\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"")
            .find(json)
            ?: return null
        return unescapeJson(match.groupValues[1])
    }

    private fun intField(json: String, key: String): Int? {
        val match = Regex("\"${Regex.escape(key)}\"\\s*:\\s*(-?\\d+)").find(json)
        return match?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    private fun unescapeJson(value: String): String {
        return value
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
    }

    private fun jsonObjects(arrayJson: String): List<String> {
        val objects = mutableListOf<String>()
        var depth = 0
        var start = -1
        var inString = false
        var escaped = false
        for (index in arrayJson.indices) {
            val char = arrayJson[index]
            if (inString) {
                escaped = if (escaped) {
                    false
                } else if (char == '\\') {
                    true
                } else {
                    if (char == '"') inString = false
                    false
                }
                continue
            }
            when (char) {
                '"' -> inString = true
                '{' -> {
                    if (depth == 0) start = index
                    depth++
                }
                '}' -> {
                    if (depth == 0) continue
                    depth--
                    if (depth == 0 && start >= 0) {
                        objects += arrayJson.substring(start, index + 1)
                        start = -1
                    }
                }
            }
        }
        return objects
    }
}
