package edu.rmas.artstreet.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import edu.rmas.artstreet.screens.components.UploadIcon
import edu.rmas.artstreet.screens.components.CallToActionText
import edu.rmas.artstreet.view_models.AuthVM

@Composable
fun SignUpScreen(authVM: AuthVM?, navController: NavController?) {
    val signUpFlow = authVM?.signUpFlow?.collectAsState()

    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val fullName = remember { mutableStateOf("") }
    val phoneNumber = remember { mutableStateOf("") }
    val profileImage = remember { mutableStateOf(Uri.EMPTY) }

    val isEmailError = remember { mutableStateOf(false) }
    val emailErrorText = remember { mutableStateOf("") }

    val isPasswordError = remember { mutableStateOf(false) }
    val passwordErrorText = remember { mutableStateOf("") }

    val isImageError = remember { mutableStateOf(false) }
    val isFullNameError = remember { mutableStateOf(false) }
    val isPhoneNumberError = remember { mutableStateOf(false) }

    val isError = remember { mutableStateOf(false) }
    val errorText = remember { mutableStateOf("") }

    val buttonIsEnabled = remember { mutableStateOf(true) }
    val isLoading = remember { mutableStateOf(false) }

    DashedLineBackground {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 34.dp, end = 24.dp, top = 24.dp, bottom = 14.dp)
        )
        {
            Spacer(modifier = Modifier.height(32.dp))
            Header(header_text = "Step into ArtStreet now!")

            Spacer(modifier = Modifier.height(8.dp))
            Secondary(secondary_text = "Create your account to explore and share stunning art sights...")

            Spacer(modifier = Modifier.height(34.dp))
            InputFieldLabel(label = "Profile Picture")

            Spacer(modifier = Modifier.height(2.dp))
            UploadIcon(profileImage, isImageError)

            Spacer(modifier = Modifier.height(16.dp))
            InputFieldLabel(label = "Full Name")

            Spacer(modifier = Modifier.height(2.dp))
            InputField(
                hint = "John Doe",
                value = fullName,
                isEmail = false,
                isError = isFullNameError,
                errorText = emailErrorText
            )

            Spacer(modifier = Modifier.height(16.dp))
            InputFieldLabel(label = "Email Address")

            Spacer(modifier = Modifier.height(2.dp))
            InputField(
                hint = "example@domain.com",
                value = email,
                isEmail = true,
                isError = isEmailError,
                errorText = emailErrorText
            )

            Spacer(modifier = Modifier.height(16.dp))
            InputFieldLabel(label = "Phone Number")

            Spacer(modifier = Modifier.height(2.dp))
            InputField(
                hint = "+1234567890",
                value = phoneNumber,
                isEmail = false,
                isError = isPhoneNumberError,
                errorText = emailErrorText
            )

            Spacer(modifier = Modifier.height(16.dp))
            InputFieldLabel(label = "Password")

            Spacer(modifier = Modifier.height(2.dp))
            PasswordInputField(
                inputValue = password,
                hint = "Never share it with anyone!",
                isError = isPasswordError,
                errorText = passwordErrorText
            )

            Spacer(modifier = Modifier.height(24.dp))
            SignUpInButton(
                onClick = {
                    isImageError.value = false
                    isEmailError.value = false
                    isPasswordError.value = false
                    isFullNameError.value = false
                    isPhoneNumberError.value = false
                    isError.value = false
                    isLoading.value = true

                    if (profileImage.value == Uri.EMPTY) {
                        isImageError.value = true
                        isLoading.value = false
                    } else if (fullName.value.isEmpty()) {
                        isFullNameError.value = true
                        isLoading.value = false
                    } else if (email.value.isEmpty()) {
                        isEmailError.value = true
                        isLoading.value = false
                    } else if (phoneNumber.value.isEmpty()) {
                        isPhoneNumberError.value = true
                        isLoading.value = false
                    } else if (password.value.isEmpty()) {
                        isPasswordError.value = true
                        isLoading.value = false
                    } else {
                        authVM?.signUp(
                            profileImage = profileImage.value,
                            fullName = fullName.value,
                            email = email.value,
                            phoneNumber = phoneNumber.value,
                            password = password.value
                        )
                    }
                },
                text = "Sign Up",
                icon = Icons.AutoMirrored.Filled.Login,
                isEnabled = buttonIsEnabled,
                isLoading = isLoading,
            )

            Spacer(modifier = Modifier.height(17.dp))
            CallToActionText(
                promptText = "Already have an account? ",
                ctaLinkText = "Sign In",
                onClick = {
                    navController?.navigate(Routes.signInScreen)
                }
            )

            Spacer(modifier = Modifier.height(101.dp))
            CopyrightText(
                year = 2024,
                owner = "18859",
                textColor = Color.White,
                modifier = Modifier
                    .align(Alignment.End)
                    .align(Alignment.CenterHorizontally)
            )
        }

        signUpFlow?.value?.let {
            when (it) {
                is Resource.Failure -> {
                    isLoading.value = false
                    Log.e("[ERROR]", it.exception.message.toString())

                    when (it.exception.message.toString()) {
                        ExceptionLogs.emptyFields -> {
                            isEmailError.value = true
                            isPasswordError.value = true
                        }

                        ExceptionLogs.badlyFormattedEmail -> {
                            isEmailError.value = true
                            emailErrorText.value = "The email address is improperly formatted."
                        }

                        ExceptionLogs.invalidCredentials -> {
                            isError.value = true
                            errorText.value = "The provided authentication credentials are incorrect or expired."
                        }

                        ExceptionLogs.passwordTooShort -> {
                            isPasswordError.value = true
                            passwordErrorText.value = "Password must be at least 6 characters long."
                        }

                        ExceptionLogs.emailAlreadyInUse -> {
                            isError.value = true
                            errorText.value = "This email address is already associated with another account."
                        }

                        else -> { }
                    }
                }
                is Resource.Success -> {
                    isLoading.value = false
                    LaunchedEffect(Unit) {
                        navController?.navigate(Routes.testScreen) {
                            popUpTo(Routes.testScreen) {
                                inclusive = true
                            }
                        }
                    }
                }
                is Resource.loading -> { }

                null -> Log.d("SignUpScreen", "SignUp flow doesn't exist!")
            }
        }
    }
}

