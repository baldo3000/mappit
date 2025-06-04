package me.baldo.mappit

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import me.baldo.mappit.data.model.Pin
import me.baldo.mappit.data.repositories.AuthenticationRepository
import me.baldo.mappit.data.repositories.CameraRepository
import me.baldo.mappit.data.repositories.PinRepository
import me.baldo.mappit.ui.screens.addpin.AddPinViewModel
import me.baldo.mappit.ui.screens.home.HomeViewModel
import me.baldo.mappit.ui.screens.pininfo.PinInfoViewModel
import me.baldo.mappit.ui.screens.signin.SignInViewModel
import me.baldo.mappit.ui.screens.signup.SignUpViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

private val Context.dataStore by preferencesDataStore("map")

val appModule = module {
    single { get<Context>().dataStore }
    single {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
            install(Realtime)
            install(Auth) {
                flowType = FlowType.PKCE
                scheme = "app"
                host = "supabase.com"
            }
            install(ComposeAuth)
            install(Storage)
        }
    }
    single { get<SupabaseClient>().postgrest }
    single { get<SupabaseClient>().auth }
    single { get<SupabaseClient>().composeAuth }
    single { get<SupabaseClient>().storage }

    single { PinRepository(get()) }
    single { AuthenticationRepository(get()) }
    single { CameraRepository(get()) }

    viewModel { SignUpViewModel(get()) }
    viewModel { SignInViewModel(get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { AddPinViewModel(get(), get()) }
    viewModel { (pinId: Long) -> PinInfoViewModel(pinId, get()) }
}