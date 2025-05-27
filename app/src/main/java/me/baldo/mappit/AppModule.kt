package me.baldo.mappit

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import me.baldo.mappit.data.repositories.AuthenticationRepository
import me.baldo.mappit.data.repositories.PinRepository
import me.baldo.mappit.ui.screens.signin.SignInViewModel
import me.baldo.mappit.ui.screens.signup.SignUpViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
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

    viewModel { SignUpViewModel(get()) }
    viewModel { SignInViewModel(get()) }
}