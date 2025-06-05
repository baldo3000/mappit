package me.baldo.mappit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import me.baldo.mappit.ui.MappItNavGraph
import me.baldo.mappit.ui.screens.settings.SettingsViewModel
import me.baldo.mappit.ui.screens.settings.Theme
import me.baldo.mappit.ui.theme.MappItTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settingsVM = koinViewModel<SettingsViewModel>()
            val settingsState by settingsVM.state.collectAsStateWithLifecycle()
            val darkTheme = when (settingsState.theme) {
                Theme.LIGHT -> false
                Theme.DARK -> true
                Theme.SYSTEM -> isSystemInDarkTheme()
            }
            MappItTheme(darkTheme) {
                val navController = rememberNavController()
                MappItNavGraph(navController, settingsState, settingsVM.actions)
            }
        }
    }
}
