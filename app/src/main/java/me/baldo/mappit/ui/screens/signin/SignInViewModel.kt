package me.baldo.mappit.ui.screens.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.baldo.mappit.data.repositories.AuthenticationRepository
import me.baldo.mappit.data.repositories.SignInResult
import javax.inject.Inject

data class SignInState(
    val email: String,
    val password: String,
    val signInResult: SignInResult = SignInResult.Success,
    val isSigningIn: Boolean = false
)

interface SignInActions {
    fun onUpdateEmail(email: String)
    fun onUpdatePassword(password: String)

    fun signIn()
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SignInState("", ""))
    val state = _state.asStateFlow()

    val actions = object : SignInActions {
        override fun onUpdateEmail(email: String) {
            _state.update { it.copy(email = email) }
        }

        override fun onUpdatePassword(password: String) {
            _state.update { it.copy(password = password) }
        }

        override fun signIn() {
            viewModelScope.launch {
                _state.update { it.copy(isSigningIn = true) }
                val email = _state.value.email
                val password = _state.value.password
                val result = authenticationRepository.signIn(email, password)
                _state.update { it.copy(signInResult = result, isSigningIn = false) }
            }
        }
    }
}