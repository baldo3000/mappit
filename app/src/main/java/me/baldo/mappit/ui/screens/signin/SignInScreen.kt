package me.baldo.mappit.ui.screens.signin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.compose.auth.ui.AuthForm
import io.github.jan.supabase.compose.auth.ui.LocalAuthState
import io.github.jan.supabase.compose.auth.ui.annotations.AuthUiExperimental
import io.github.jan.supabase.compose.auth.ui.email.OutlinedEmailField
import io.github.jan.supabase.compose.auth.ui.password.OutlinedPasswordField
import io.github.jan.supabase.compose.auth.ui.password.PasswordRule
import io.github.jan.supabase.compose.auth.ui.password.rememberPasswordRuleList
import me.baldo.mappit.R
import me.baldo.mappit.data.repositories.SignInResult

@OptIn(AuthUiExperimental::class)
@Composable
fun SignInScreen(
    signInState: SignInState,
    signInActions: SignInActions,
    goToSignUp: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    AuthForm {
        val authState = LocalAuthState.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.auth_sign_in),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            OutlinedEmailField(
                value = signInState.email,
                onValueChange = signInActions::onUpdateEmail,
                label = { Text(stringResource(R.string.auth_email)) },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            OutlinedPasswordField(
                value = signInState.password,
                onValueChange = signInActions::onUpdatePassword,
                label = { Text(stringResource(R.string.auth_password)) },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(
                    onGo = {
                        keyboardController?.hide()
                        signInActions.signIn()
                    }
                ),
                rules = rememberPasswordRuleList(
                    PasswordRule.minLength(1)
                )
            )
            when (signInState.signInResult) {
                SignInResult.Error -> {
                    Text(
                        text = stringResource(R.string.auth_sign_in_error),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                SignInResult.InvalidCredentials -> {
                    Text(
                        text = stringResource(R.string.auth_sign_in_invalid_credentials),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                SignInResult.Success -> {}
            }
            Spacer(Modifier.height(4.dp))
            TextButton(goToSignUp) {
                Text(
                    text = stringResource(R.string.auth_sign_up_go),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = signInActions::signIn,
                enabled = authState.validForm && !signInState.isSigningIn,
                shapes = ButtonDefaults.shapes()
            ) { Text(stringResource(if (signInState.isSigningIn) R.string.auth_signing_in else R.string.auth_sign_in)) }
        }
    }
}