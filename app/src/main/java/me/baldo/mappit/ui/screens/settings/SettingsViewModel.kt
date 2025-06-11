package me.baldo.mappit.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.baldo.mappit.R
import me.baldo.mappit.data.repositories.SettingsRepository

enum class Theme {
    LIGHT, DARK, SYSTEM;

    fun toResourceString(ctx: Context): String {
        return when (this) {
            LIGHT -> ctx.getString(R.string.settings_theme_light)
            DARK -> ctx.getString(R.string.settings_theme_dark)
            SYSTEM -> ctx.getString(R.string.settings_theme_system)
        }
    }
}

data class SettingsState(
    val theme: Theme = Theme.SYSTEM,
    val appLock: Boolean = false
)

interface SettingsActions {
    fun onThemeChanged(theme: Theme)
    fun onAppLockChanged(appLock: Boolean)
}

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    val actions = object : SettingsActions {
        override fun onThemeChanged(theme: Theme) {
            _state.update { it.copy(theme = theme) }
            viewModelScope.launch {
                settingsRepository.setTheme(theme)
            }
        }

        override fun onAppLockChanged(appLock: Boolean) {
            _state.update { it.copy(appLock = appLock) }
            viewModelScope.launch {
                settingsRepository.setAppLock(appLock)
            }
        }
    }

    init {
        runBlocking {
            _state.update {
                it.copy(
                    theme = settingsRepository.theme.first(),
                    appLock = settingsRepository.appLock.first()
                )
            }
        }
    }
}