package me.baldo.mappit.ui

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.baldo.mappit.ui.screens.signin.SignInScreen
import me.baldo.mappit.ui.screens.signin.SignInViewModel
import me.baldo.mappit.ui.screens.signup.SignUpScreen
import me.baldo.mappit.ui.screens.signup.SignUpViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private const val TAG = "NavGraph"

sealed interface MappItRoute {
    @Serializable
    data object SignUp : MappItRoute

    @Serializable
    data object SignIn : MappItRoute

    @Serializable
    data object HomeTest : MappItRoute
}

@Composable
fun MappItNavGraph(navController: NavHostController) {
    val auth = koinInject<Auth>()

    LaunchedEffect(Unit) {
        auth.sessionStatus.collect {
            when (it) {
                is SessionStatus.Authenticated -> {
                    Log.i(TAG, "User has authenticated: ${it.session.user?.email}")
                    when (it.source) {
                        SessionSource.External -> TODO()
                        is SessionSource.Refresh -> TODO()
                        is SessionSource.SignIn -> {navController.navigate(MappItRoute.HomeTest) }
                        is SessionSource.SignUp -> {navController.navigate(MappItRoute.HomeTest) }
                        SessionSource.Storage -> {navController.navigate(MappItRoute.HomeTest) }
                        SessionSource.Unknown -> TODO()
                        is SessionSource.UserChanged -> TODO()
                        is SessionSource.UserIdentitiesChanged -> TODO()
                        SessionSource.AnonymousSignIn -> TODO()
                    }
                }

                SessionStatus.Initializing -> Log.i(TAG, "Session is initializing")
                is SessionStatus.RefreshFailure -> Log.i(
                    TAG,
                    "Refresh failure: ${it.cause}"
                ) // Either a network error or a internal server error
                is SessionStatus.NotAuthenticated -> {
                    if (it.isSignOut) {
                        Log.i(TAG, "User signed out")
                        navController.navigate(MappItRoute.SignIn)
                    } else {
                        Log.i(TAG, "User not signed in")
                    }
                }
            }
        }
    }
    val scope = rememberCoroutineScope()

    NavHost(
        startDestination = MappItRoute.SignIn,
        navController = navController
    ) {
        composable<MappItRoute.SignUp> {
            val signUpVM = koinViewModel<SignUpViewModel>()
            val signUpState by signUpVM.state.collectAsStateWithLifecycle()
            SignUpScreen(
                signUpState = signUpState,
                signUpActions = signUpVM.actions,
                goToSignIn = { navController.navigate(MappItRoute.SignIn) }
            )
        }

        composable<MappItRoute.SignIn> {
            val signInVM = koinViewModel<SignInViewModel>()
            val signInState by signInVM.state.collectAsStateWithLifecycle()
            SignInScreen(
                signInState = signInState,
                signInActions = signInVM.actions,
                goToSignUp = { navController.navigate(MappItRoute.SignUp) }
            )
        }

        composable<MappItRoute.HomeTest> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Button({ scope.launch { auth.signOut() } }) { Text("Sign Out") }
            }
        }
    }
}