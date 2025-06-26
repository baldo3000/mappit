package me.baldo.mappit.data.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map
import me.baldo.mappit.ui.screens.settings.Theme
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
        private val APP_LOCK_KEY = booleanPreferencesKey("app_lock")
    }

    val theme =
        dataStore.data.map { it[THEME_KEY] ?: Theme.SYSTEM.toString() }.map { Theme.valueOf(it) }

    suspend fun setTheme(theme: Theme) =
        dataStore.edit { it[THEME_KEY] = theme.toString() }

    val appLock =
        dataStore.data.map { it[APP_LOCK_KEY] == true }

    suspend fun setAppLock(lockApp: Boolean) =
        dataStore.edit { it[APP_LOCK_KEY] = lockApp }
}