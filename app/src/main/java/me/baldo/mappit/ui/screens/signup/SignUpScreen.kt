package me.baldo.mappit.ui.screens.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.compose.auth.ui.AuthForm
import io.github.jan.supabase.compose.auth.ui.LocalAuthState
import io.github.jan.supabase.compose.auth.ui.annotations.AuthUiExperimental
import io.github.jan.supabase.compose.auth.ui.email.OutlinedEmailField
import io.github.jan.supabase.compose.auth.ui.password.OutlinedPasswordField
import io.github.jan.supabase.compose.auth.ui.password.PasswordRule
import io.github.jan.supabase.compose.auth.ui.password.rememberPasswordRuleList
import me.baldo.mappit.R

@OptIn(AuthUiExperimental::class, ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    signUpState: SignUpState,
    signUpActions: SignUpActions,
    goToSignIn: () -> Unit
) {
    AuthForm() {
        val authState = LocalAuthState.current

        Column(
            modifier = Modifier.fillMaxSize(),
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
                value = signUpState.email,
                onValueChange = signUpActions::onUpdateEmail,
                label = { Text(stringResource(R.string.auth_email)) }
            )
            OutlinedPasswordField(
                value = signUpState.password,
                onValueChange = signUpActions::onUpdatePassword,
                label = { Text(stringResource(R.string.auth_password)) },
                rules = rememberPasswordRuleList(
                    PasswordRule.minLength(6),
                    PasswordRule.containsSpecialCharacter(),
                    PasswordRule.containsDigit(),
                    PasswordRule.containsLowercase(),
                    PasswordRule.containsUppercase()
                )
            )
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
                enabled = authState.validForm
            ) { Text(stringResource(R.string.auth_sign_up)) }
        }
    }
}