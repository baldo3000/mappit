package me.baldo.mappit.ui.screens.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.baldo.mappit.data.repositories.AuthenticationRepository
import me.baldo.mappit.data.repositories.SignUpResult
import javax.inject.Inject

data class SignUpState(
    val email: String,
    val password: String,
    val signUpResult: SignUpResult = SignUpResult.Success,
    val isSigningUp: Boolean = false
)

interface SignUpActions {
    fun onUpdateEmail(email: String)
    fun onUpdatePassword(password: String)

    fun signUp()
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SignUpState("", ""))
    val state = _state.asStateFlow()

    val actions = object : SignUpActions {
        override fun onUpdateEmail(email: String) {
            _state.update { it.copy(email = email) }
        }

        override fun onUpdatePassword(password: String) {
            _state.update { it.copy(password = password) }
        }

        override fun signUp() {
            viewModelScope.launch {
                _state.update { it.copy(isSigningUp = true) }
                val email = _state.value.email
                val password = _state.value.password
                val result = authenticationRepository.signUp(email, password)
                _state.update { it.copy(signUpResult = result, isSigningUp = false) }
            }
        }
    }
}