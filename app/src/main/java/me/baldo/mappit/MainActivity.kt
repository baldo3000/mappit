package me.baldo.mappit

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.auth.Auth
import me.baldo.mappit.ui.MappItNavGraph
import me.baldo.mappit.ui.screens.settings.SettingsViewModel
import me.baldo.mappit.ui.screens.settings.Theme
import me.baldo.mappit.ui.theme.MappItTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    @Inject
    lateinit var auth: Auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsVM: SettingsViewModel = hiltViewModel()
            val settingsState by settingsVM.state.collectAsStateWithLifecycle()
            val darkTheme = when (settingsState.theme) {
                Theme.LIGHT -> false
                Theme.DARK -> true
                Theme.SYSTEM -> isSystemInDarkTheme()
            }
            MappItTheme(darkTheme) {
                val navController = rememberNavController()
                MappItNavGraph(auth, navController, settingsState, settingsVM.actions)
            }
        }
    }
}
