package com.example.update

import android.content.Context

class UpdatePreferences(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun lastCheckEpochMillis(): Long = prefs.getLong(KEY_LAST_CHECK, 0L)

    fun markChecked(nowMillis: Long = System.currentTimeMillis()) {
        prefs.edit().putLong(KEY_LAST_CHECK, nowMillis).apply()
    }

    fun skippedVersion(): String? = prefs.getString(KEY_SKIPPED_VERSION, null)

    fun skipVersion(versionName: String) {
        prefs.edit().putString(KEY_SKIPPED_VERSION, AppVersion.normalize(versionName)).apply()
    }

    fun shouldAutoCheck(nowMillis: Long = System.currentTimeMillis()): Boolean {
        return nowMillis - lastCheckEpochMillis() >= AUTO_CHECK_INTERVAL_MS
    }

    companion object {
        private const val PREFS_NAME = "app_update_prefs"
        private const val KEY_LAST_CHECK = "last_check_epoch"
        private const val KEY_SKIPPED_VERSION = "skipped_version"
        const val AUTO_CHECK_INTERVAL_MS = 12L * 60L * 60L * 1000L
    }
}
