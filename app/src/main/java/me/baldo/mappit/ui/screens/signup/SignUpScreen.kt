package me.baldo.mappit.ui.screens.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import me.baldo.mappit.data.repositories.SignUpResult

@OptIn(AuthUiExperimental::class)
@Composable
fun SignUpScreen(
    signUpState: SignUpState,
    signUpActions: SignUpActions,
    goToSignIn: () -> Unit
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
                text = stringResource(R.string.auth_sign_up),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            OutlinedEmailField(
                modifier = Modifier.width(280.dp),
                value = signUpState.email,
                onValueChange = signUpActions::onUpdateEmail,
                label = { Text(stringResource(R.string.auth_email)) },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            OutlinedPasswordField(
                modifier = Modifier.width(280.dp),
                value = signUpState.password,
                onValueChange = signUpActions::onUpdatePassword,
                label = { Text(stringResource(R.string.auth_password)) },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(
                    onGo = {
                        keyboardController?.hide()
                        signUpActions.signUp()
                    }
                ),
                rules = rememberPasswordRuleList(
                    PasswordRule.minLength(6),
                    PasswordRule.containsSpecialCharacter(),
                    PasswordRule.containsDigit(),
                    PasswordRule.containsLowercase(),
                    PasswordRule.containsUppercase()
                )
            )
            when (signUpState.signUpResult) {
                SignUpResult.Error -> {
                    Text(
                        text = stringResource(R.string.auth_sign_up_error),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                SignUpResult.UserExisting -> {
                    Text(
                        text = stringResource(R.string.auth_sign_up_user_exists),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                SignUpResult.Success -> {}
            }
            Spacer(Modifier.height(4.dp))
            TextButton(goToSignIn) {
                Text(
                    text = stringResource(R.string.auth_sign_in_go),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = signUpActions::signUp,
                enabled = authState.validForm && !signUpState.isSigningUp,
                shapes = ButtonDefaults.shapes()
            ) { Text(stringResource(if (signUpState.isSigningUp) R.string.auth_signing_up else R.string.auth_sign_up)) }
        }
    }
}