package com.example.update

data class RemoteUpdate(
    val versionName: String,
    val versionCode: Int?,
    val pageUrl: String,
    val downloadUrl: String?,
    val notes: String?,
)

sealed class UpdateCheckResult {
    data class Available(val update: RemoteUpdate) : UpdateCheckResult()
    data object UpToDate : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}

object AppVersion {
    fun normalize(raw: String): String {
        return raw.trim().removePrefix("v").removePrefix("V")
    }

    fun compareNames(left: String, right: String): Int {
        val leftParts = numericParts(left)
        val rightParts = numericParts(right)
        val size = maxOf(leftParts.size, rightParts.size)
        for (index in 0 until size) {
            val l = leftParts.getOrElse(index) { 0 }
            val r = rightParts.getOrElse(index) { 0 }
            if (l != r) return l.compareTo(r)
        }
        return 0
    }

    fun isRemoteNewer(
        remoteName: String,
        remoteCode: Int?,
        localName: String,
        localCode: Int,
    ): Boolean {
        if (remoteCode != null && remoteCode > 0) {
            return remoteCode > localCode
        }
        return compareNames(remoteName, localName) > 0
    }

    private fun numericParts(raw: String): List<Int> {
        return normalize(raw)
            .split(Regex("[^0-9]+"))
            .filter { it.isNotEmpty() }
            .mapNotNull { it.toIntOrNull() }
    }
}
