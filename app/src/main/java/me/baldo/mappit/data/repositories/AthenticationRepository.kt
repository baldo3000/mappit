package me.baldo.mappit.data.repositories

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo

sealed interface SignInResult {
    data object Success : SignInResult
    data object InvalidCredentials : SignInResult
    data object Error : SignInResult
}

sealed interface SignUpResult {
    data object Success : SignUpResult
    data object UserExisting : SignUpResult
    data object Error : SignUpResult
}

class AuthenticationRepository(
    private val auth: Auth
) {
    val user: UserInfo?
        get() = (auth.sessionStatus.value as? SessionStatus.Authenticated)?.session?.user

    suspend fun getUser(): UserInfo {
        return auth.retrieveUserForCurrentSession(true)
    }

    suspend fun signOut(): Boolean {
        return try {
            auth.signOut()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun signIn(email: String, password: String): SignInResult {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            SignInResult.Success
        } catch (_: AuthRestException) {
            SignInResult.InvalidCredentials
        } catch (_: Exception) {
            SignInResult.Error
        }
    }

    suspend fun signUp(email: String, password: String): SignUpResult {
        return try {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            SignUpResult.Success
        } catch (_: AuthRestException) {
            SignUpResult.UserExisting
        } catch (e: Exception) {
            throw e
            SignUpResult.Error
        }
    }

    suspend fun signInWithGoogle(): Boolean {
        return try {
            auth.signInWith(Google)
            true
        } catch (e: Exception) {
            false
        }
    }
}