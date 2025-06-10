package me.baldo.mappit.ui

import android.util.Log
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.status.SessionSource
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.serialization.Serializable
import me.baldo.mappit.R
import me.baldo.mappit.ui.composables.HomeOverlay
import me.baldo.mappit.ui.composables.MenuOverlay
import me.baldo.mappit.ui.screens.addpin.AddPinScreen
import me.baldo.mappit.ui.screens.addpin.AddPinViewModel
import me.baldo.mappit.ui.screens.bookmarks.BookmarksScreen
import me.baldo.mappit.ui.screens.bookmarks.BookmarksViewModel
import me.baldo.mappit.ui.screens.home.HomeScreen
import me.baldo.mappit.ui.screens.home.HomeViewModel
import me.baldo.mappit.ui.screens.pininfo.PinInfoScreen
import me.baldo.mappit.ui.screens.pininfo.PinInfoViewModel
import me.baldo.mappit.ui.screens.profile.ProfileScreen
import me.baldo.mappit.ui.screens.profile.ProfileViewModel
import me.baldo.mappit.ui.screens.profileSetup.ProfileSetupScreen
import me.baldo.mappit.ui.screens.profileSetup.ProfileSetupViewModel
import me.baldo.mappit.ui.screens.settings.SettingsActions
import me.baldo.mappit.ui.screens.settings.SettingsScreen
import me.baldo.mappit.ui.screens.settings.SettingsState
import me.baldo.mappit.ui.screens.signin.SignInScreen
import me.baldo.mappit.ui.screens.signin.SignInViewModel
import me.baldo.mappit.ui.screens.signup.SignUpScreen
import me.baldo.mappit.ui.screens.signup.SignUpViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

private const val TAG = "NavGraph"

sealed interface MappItRoute {
    @Serializable
    data object Dummy : MappItRoute

    @Serializable
    data object SignUp : MappItRoute

    @Serializable
    data object SignIn : MappItRoute

    @Serializable
    data object Home : MappItRoute

    @Serializable
    data object Settings : MappItRoute

    @Serializable
    data object AddPin : MappItRoute

    @Serializable
    data object Profile : MappItRoute

    @Serializable
    data object Discovery : MappItRoute

    @Serializable
    data object Bookmarks : MappItRoute

    @Serializable
    data class ProfileSetup(val userId: String) : MappItRoute

    @Serializable
    data class PinInfo(val pinId: String) : MappItRoute
}

@Composable
fun MappItNavGraph(
    navController: NavHostController,
    settingsState: SettingsState,
    settingsActions: SettingsActions
) {
    val auth = koinInject<Auth>()

    LaunchedEffect(Unit) {
        auth.sessionStatus.collect {
            when (it) {
                is SessionStatus.Authenticated -> {
                    when (it.source) {
                        SessionSource.External -> {}
                        is SessionSource.SignIn -> {
                            navController.navigate(MappItRoute.Home) { popUpTo(0) }
                        }

                        is SessionSource.SignUp -> {
                            val userId = it.session.user?.id?.toString()
                                ?: error("User registered but profile missing")
                            navController.navigate(MappItRoute.ProfileSetup(userId)) { popUpTo(0) }
                        }

                        SessionSource.Storage -> {
                            Log.i(TAG, "Session restored from storage")
                            if (navController.currentBackStackEntry?.destination?.route == MappItRoute.Dummy::class.qualifiedName) {
                                navController.navigate(MappItRoute.Home) { popUpTo(0) }
                            }
                        }

                        SessionSource.Unknown -> {
                            Log.i(TAG, "Session source is unknown")
                        }

                        is SessionSource.UserChanged -> {
                            Log.i(TAG, "User changed")
                        }

                        is SessionSource.UserIdentitiesChanged -> {
                            Log.i(TAG, "User identities changed")
                        }

                        SessionSource.AnonymousSignIn -> {
                            Log.i(TAG, "Anonymous sign in")
                        }

                        is SessionSource.Refresh -> {
                            Log.i(TAG, "Session refreshed")
                            if (navController.currentBackStackEntry?.destination?.route == MappItRoute.Dummy::class.qualifiedName) {
                                navController.navigate(MappItRoute.Home) { popUpTo(0) }
                            }
                        }
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
                        navController.navigate(MappItRoute.SignIn) { popUpTo(0) }
                    } else {
                        Log.i(TAG, "User not signed in")
                        navController.navigate(MappItRoute.SignIn) { popUpTo(0) }
                    }
                }
            }
        }
    }

    val homeVM = koinViewModel<HomeViewModel>()
    val homeState by homeVM.state.collectAsStateWithLifecycle()

    NavHost(
        startDestination = MappItRoute.Dummy,
        navController = navController,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None }
    ) {
        composable<MappItRoute.Home> {
            homeVM.actions.updatePins()
            HomeOverlay(BottomBarTab.Home, navController) { innerPadding ->
                HomeScreen(
                    homeState,
                    homeVM.actions,
                    onAddPin = { navController.navigate(MappItRoute.AddPin) },
                    onPinInfo = { navController.navigate(MappItRoute.PinInfo(it.toString())) },
                    theme = settingsState.theme,
                    Modifier.padding(innerPadding)
                )
            }
        }

        composable<MappItRoute.Discovery> {
            HomeOverlay(BottomBarTab.Discovery, navController) {

            }
        }

        composable<MappItRoute.Bookmarks> {
            val user = (auth.sessionStatus.value as? SessionStatus.Authenticated)?.session?.user

            if (user != null) {
                HomeOverlay(BottomBarTab.Bookmarks, navController) {
                    val bookmarksVM = koinViewModel<BookmarksViewModel>(
                        parameters = { parametersOf(Uuid.parse(user.id)) }
                    )
                    val bookmarksState by bookmarksVM.state.collectAsStateWithLifecycle()

                    BookmarksScreen(
                        state = bookmarksState,
                        actions = bookmarksVM.actions,
                        onPinClick = { navController.navigate(MappItRoute.PinInfo(it.id.toString())) },
                        onProfileClick = {},
                        modifier = Modifier.padding(it)
                    )
                }
            } else {
                TODO()
            }
        }

        composable<MappItRoute.Dummy> {
            Box(Modifier.background(MaterialTheme.colorScheme.surfaceContainer))
        }

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

        composable<MappItRoute.Settings> {
            MenuOverlay(stringResource(R.string.screen_settings), navController) {
                SettingsScreen(
                    settingsState = settingsState,
                    settingsActions = settingsActions,
                    modifier = Modifier.padding(it)
                )
            }
        }

        composable<MappItRoute.AddPin> {
            val addPinViewModel = koinViewModel<AddPinViewModel>()
            val addPinState by addPinViewModel.state.collectAsStateWithLifecycle()

            MenuOverlay(stringResource(R.string.screen_add_pin), navController) {
                AddPinScreen(
                    addPinState = addPinState,
                    addPinActions = addPinViewModel.actions,
                    onPinAdd = { navController.navigateUp() },
                    modifier = Modifier.padding(it)
                )
            }
        }

        composable<MappItRoute.PinInfo> { backStackEntry ->
            val pinId = Uuid.parse(backStackEntry.toRoute<MappItRoute.PinInfo>().pinId)
            val pinInfoVM = koinViewModel<PinInfoViewModel>(parameters = { parametersOf(pinId) })
            val pinInfoState by pinInfoVM.state.collectAsStateWithLifecycle()

            MenuOverlay(stringResource(R.string.screen_pin_info), navController) {
                PinInfoScreen(pinInfoState, pinInfoVM.actions, Modifier.padding(it))
            }
        }

        composable<MappItRoute.Profile> {
            val profileVm = koinViewModel<ProfileViewModel>()
            val profileState by profileVm.state.collectAsStateWithLifecycle()

            HomeOverlay(BottomBarTab.Profile, navController) {
                ProfileScreen(profileState, profileVm.actions, Modifier.padding(it))
            }
        }

        composable<MappItRoute.ProfileSetup> { backStackEntry ->
            val userId = Uuid.parse(backStackEntry.toRoute<MappItRoute.ProfileSetup>().userId)
            val profileSetupVM =
                koinViewModel<ProfileSetupViewModel>(parameters = { parametersOf(userId) })
            val profileSetupState by profileSetupVM.state.collectAsStateWithLifecycle()

            ProfileSetupScreen(profileSetupState, profileSetupVM.actions) {
                navController.navigate(MappItRoute.Home) { popUpTo(0) }
            }
        }
    }
}
