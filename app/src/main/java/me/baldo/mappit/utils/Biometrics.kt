package me.baldo.mappit.utils

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

interface BiometricsHelper {
    fun isBiometricAvailable(): Boolean
    fun authenticate()
}

@Composable
fun rememberBiometricsHelper(
    dialogTitle: String,
    onAuthenticationSuccess: () -> Unit,
): BiometricsHelper {
    val context = LocalContext.current
    val biometricManager = remember { BiometricManager.from(context) }
    val isBiometricAvailable = remember {
        biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
    }
    val executor = remember { ContextCompat.getMainExecutor(context) }
    val biometricPrompt = remember {
        BiometricPrompt(
            context as FragmentActivity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e("TAG", "Authentication error: $errorCode - $errString")
                }

                @RequiresApi(Build.VERSION_CODES.R)
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.i("TAG", "Authentication succeeded")
                    onAuthenticationSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.e("TAG", "Authentication failed")
                }
            }
        )
    }
    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .setTitle(dialogTitle)
            .build()
    }

    return object : BiometricsHelper {
        override fun isBiometricAvailable(): Boolean {
            return isBiometricAvailable == BiometricManager.BIOMETRIC_SUCCESS
        }

        override fun authenticate() {
            if (isBiometricAvailable()) {
                biometricPrompt.authenticate(promptInfo)
            }
        }
    }
}