package me.baldo.mappit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import me.baldo.mappit.ui.MappItNavGraph
import me.baldo.mappit.ui.theme.MappItTheme

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MappItTheme {
                val navController = rememberNavController()
                MappItNavGraph(navController)
            }
        }
    }
}
