package com.example.incidentscompose.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.incidentscompose.navigation.Destinations
import com.example.incidentscompose.ui.components.IncidentsTextField
import com.example.incidentscompose.ui.components.LoadingOverlay
import com.example.incidentscompose.viewmodel.RegisterState
import com.example.incidentscompose.viewmodel.RegisterViewModel
import org.koin.compose.koinInject

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = koinInject()
) {
    val isBusy by viewModel.isBusy.collectAsState()
    val registerState by viewModel.registerState.collectAsState()
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    LaunchedEffect(registerState) {
        when (registerState) {
            is RegisterState.Success -> {
                Toast.makeText(
                    context,
                    "Registration successful! You can now log in.",
                    Toast.LENGTH_LONG
                ).show()

                navController.navigate(Destinations.Login.route) {
                    popUpTo("register") { inclusive = true }
                }
            }
            else -> {
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF6EE7FF)))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.width(360.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(30.dp)
                ) {
                    // Title
                    Text(
                        text = "Create Account",
                        fontSize = 24.sp,
                        color = Color(0xFF0D47A1),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    IncidentsTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            if (registerState is RegisterState.Error) {
                                viewModel.clearRegisterState()
                            }
                        },
                        placeholder = "Username",
                        isError = registerState is RegisterState.Error
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Email field
                    IncidentsTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (registerState is RegisterState.Error) {
                                viewModel.clearRegisterState()
                            }
                        },
                        placeholder = "Email",
                        isError = registerState is RegisterState.Error
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Password field
                    IncidentsTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (registerState is RegisterState.Error) {
                                viewModel.clearRegisterState()
                            }
                        },
                        placeholder = "Password",
                        isPassword = true,
                        isError = registerState is RegisterState.Error
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    IncidentsTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (registerState is RegisterState.Error) {
                                viewModel.clearRegisterState()
                            }
                        },
                        placeholder = "Confirm Password",
                        isPassword = true,
                        isError = registerState is RegisterState.Error
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    when (registerState) {
                        is RegisterState.Error -> {
                            Text(
                                text = (registerState as RegisterState.Error).message,
                                color = Color.Red,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                        else -> {
                            // Show nothing for other states
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.register(username, password, email, confirmPassword)
                        },
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isBusy && username.isNotBlank() && password.isNotBlank() &&
                                email.isNotBlank() && confirmPassword.isNotBlank()
                    ) {
                        Text("Register")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Already have an account? Login here",
                        fontSize = 14.sp,
                        color = Color(0xFF0D47A1),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(Destinations.Login.route) {
                                    popUpTo("register") { inclusive = true }
                                }
                            }
                    )
                }
            }
        }

        LoadingOverlay(isLoading = isBusy)
    }
}