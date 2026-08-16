package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.update.GitHubUpdateRepository
import com.example.update.OkHttpUpdateClient
import com.example.update.RemoteUpdate
import com.example.update.UpdateCheckResult
import com.example.update.UpdatePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppUpdateUiState(
    val currentVersionName: String = BuildConfig.VERSION_NAME,
    val availableUpdate: RemoteUpdate? = null,
    val showDialog: Boolean = false,
    val isChecking: Boolean = false,
    val statusMessage: String? = null,
)

class AppUpdateViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = UpdatePreferences(application)
    private val repository = GitHubUpdateRepository(
        currentVersionName = BuildConfig.VERSION_NAME,
        currentVersionCode = BuildConfig.VERSION_CODE,
        httpClient = OkHttpUpdateClient(),
    )

    private val _uiState = MutableStateFlow(AppUpdateUiState())
    val uiState: StateFlow<AppUpdateUiState> = _uiState.asStateFlow()

    fun checkForUpdates(manual: Boolean) {
        if (_uiState.value.isChecking) return
        if (!manual && !preferences.shouldAutoCheck()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isChecking = true, statusMessage = if (manual) "更新を確認しています…" else null) }
            val result = repository.check()
            preferences.markChecked()
            when (result) {
                is UpdateCheckResult.Available -> {
                    val skipped = preferences.skippedVersion()
                    val shouldPrompt = manual || skipped != result.update.versionName
                    _uiState.update {
                        it.copy(
                            isChecking = false,
                            availableUpdate = result.update,
                            showDialog = shouldPrompt,
                            statusMessage = if (manual && !shouldPrompt) {
                                "新しいバージョン ${result.update.versionName} があります"
                            } else {
                                null
                            },
                        )
                    }
                }
                UpdateCheckResult.UpToDate -> {
                    _uiState.update {
                        it.copy(
                            isChecking = false,
                            availableUpdate = null,
                            showDialog = false,
                            statusMessage = if (manual) {
                                "お使いのバージョン（${BuildConfig.VERSION_NAME}）は最新です"
                            } else {
                                null
                            },
                        )
                    }
                }
                is UpdateCheckResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isChecking = false,
                            statusMessage = if (manual) result.message else null,
                        )
                    }
                }
            }
        }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(showDialog = false) }
    }

    fun skipAvailableVersion() {
        val version = _uiState.value.availableUpdate?.versionName
        if (version != null) {
            preferences.skipVersion(version)
        }
        _uiState.update { it.copy(showDialog = false) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
