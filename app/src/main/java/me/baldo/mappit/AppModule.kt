package me.baldo.mappit

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("map")

/*
* Provides a Hilt module for singleton dependencies
*/
@Module
@InstallIn(SingletonComponent::class)
internal object HiltSingletonModule {
    @Provides
    @Singleton
    fun providesPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.dataStore

    @Provides
    @Singleton
    fun providesSupabaseClient(

    ): SupabaseClient = createSupabaseClient(
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

    @Provides
    @Singleton
    fun providesPostgrest(
        client: SupabaseClient
    ): Postgrest = client.postgrest

    @Provides
    @Singleton
    fun providesAuth(
        client: SupabaseClient
    ): Auth = client.auth

    @Provides
    @Singleton
    fun providesComposeAuth(
        client: SupabaseClient
    ): ComposeAuth = client.composeAuth

    @Provides
    @Singleton
    fun providesStorage(
        client: SupabaseClient
    ): Storage = client.storage
}