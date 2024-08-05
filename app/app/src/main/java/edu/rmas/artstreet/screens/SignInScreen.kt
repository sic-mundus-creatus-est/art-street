package edu.rmas.artstreet.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.rmas.artstreet.app_navigation.Routes
import edu.rmas.artstreet.data.repositories.Resource
import edu.rmas.artstreet.logs.ExceptionLogs
import edu.rmas.artstreet.screens.components.SignUpInButton
import edu.rmas.artstreet.screens.components.PasswordInputField
import edu.rmas.artstreet.screens.components.InputField
import edu.rmas.artstreet.screens.components.CopyrightText
import edu.rmas.artstreet.screens.components.DashedLineBackground
import edu.rmas.artstreet.screens.components.Secondary
import edu.rmas.artstreet.screens.components.Header
import edu.rmas.artstreet.screens.components.InputFieldLabel
import edu.rmas.artstreet.screens.components.CallToActionText
import edu.rmas.artstreet.screens.components.customErrorContainer
import edu.rmas.artstreet.view_models.AuthVM

@Composable
fun SignInScreen ( authVM: AuthVM?, navController: NavController)
{
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    val isEmailError = remember { mutableStateOf(false) }
    val emailErrorText = remember { mutableStateOf("") }

    val isPasswordError = remember { mutableStateOf(false) }
    val passwordErrorText = remember { mutableStateOf("") }

    val isError = remember { mutableStateOf(false) }
    val errorText = remember { mutableStateOf("") }

    val buttonIsEnabled = remember { mutableStateOf(true) }
    val isLoading = remember { mutableStateOf(false) }

    val signInFlow = authVM?.signInFlow?.collectAsState()

    DashedLineBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 34.dp, end = 24.dp, top = 44.dp, bottom = 14.dp)
        )
        {
            Header(header_text = "Welcome Back to ArtStreet!")

            Spacer(modifier = Modifier.height(7.dp))
            Secondary(secondary_text = "Ready to continue exploring streets full of art?")

            Spacer(modifier = Modifier.height(47.dp))
            InputFieldLabel(label = "Enter your E-mail")

            Spacer(modifier = Modifier.height(2.dp))
            InputField(
                hint = "example@domain.com",
                value = email,
                isEmail = true,
                isError = isEmailError,
                errorText = emailErrorText
            )

            Spacer(modifier = Modifier.height(20.dp))
            InputFieldLabel(label = "Enter your Password")

            Spacer(modifier = Modifier.height(2.dp))
            PasswordInputField(
                inputValue = password,
                hint = "Never share it with anyone!",
                isError = isPasswordError,
                errorText = passwordErrorText
            )

            if (isError.value) customErrorContainer(errorText = "Oops! Invalid credentials, please try again.")
            Spacer(modifier = Modifier.height(24.dp))
            SignUpInButton(
                onClick = {
                    isEmailError.value = false
                    isPasswordError.value = false
                    isError.value = false
                    isLoading.value = true
                    authVM?.signIn(email.value, password.value)
                },
                text = "Sign In",
                icon = Icons.AutoMirrored.Filled.Login,
                isEnabled = buttonIsEnabled,
                isLoading = isLoading,
            )

            Spacer(modifier = Modifier.height(17.dp))
            CallToActionText(
                promptText = "New to ArtStreet? ",
                ctaLinkText = "Sign Up",
                onClick = {
                    navController.navigate(Routes.signUpScreen)
                })

            Spacer(modifier = Modifier.weight(1f)) // takes all space that's left
            CopyrightText(
                year = 2024,
                owner = "18859",
                textColor = Color.White,
                modifier = Modifier
                    .align(Alignment.End)
                    .align(Alignment.CenterHorizontally)
            )
        }

        signInFlow?.value.let {
            when (it) {
                is Resource.Failure -> {
                    isLoading.value = false
                    Log.d("[ERROR]", it.exception.message.toString())
                    when (it.exception.message.toString()) {
                        ExceptionLogs.emptyFields -> {
                            isEmailError.value = true
                            isPasswordError.value = true
                        }

                        ExceptionLogs.badlyFormattedEmail -> {
                            isEmailError.value = true
                            emailErrorText.value = "Invalid e-mail address."
                        }

                        ExceptionLogs.invalidCredentials -> {
                            isError.value = true
                            errorText.value = "Invalid credentials."
                        }

                        else -> {}
                    }

                }

                is Resource.Success -> {
                    isLoading.value = false
                    LaunchedEffect(Unit) {
                        navController.navigate(Routes.testScreen) {
                            popUpTo(Routes.testScreen) {
                                inclusive = true
                            }
                        }
                    }
                }

                is Resource.loading -> {}
                null -> {}
            }
        }
    }
}
