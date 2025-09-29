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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.incidentscompose.navigation.Destinations
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
                    modifier = Modifier.padding(30.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Create Account",
                        fontSize = 24.sp,
                        color = Color(0xFF0D47A1),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    TextField(
                        value = username,
                        onValueChange = {
                            username = it
                            // Clear error when user starts typing
                            if (registerState is RegisterState.Error) {
                                viewModel.clearRegisterState()
                            }
                        },
                        placeholder = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        isError = registerState is RegisterState.Error
                    )

                    TextField(
                        value = email,
                        onValueChange = {
                            email = it
                            // Clear error when user starts typing
                            if (registerState is RegisterState.Error) {
                                viewModel.clearRegisterState()
                            }
                        },
                        placeholder = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        isError = registerState is RegisterState.Error
                    )

                    TextField(
                        value = password,
                        onValueChange = {
                            password = it
                            // Clear error when user starts typing
                            if (registerState is RegisterState.Error) {
                                viewModel.clearRegisterState()
                            }
                        },
                        placeholder = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        isError = registerState is RegisterState.Error
                    )

                    TextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (registerState is RegisterState.Error) {
                                viewModel.clearRegisterState()
                            }
                        },
                        placeholder = { Text("Confirm Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        isError = registerState is RegisterState.Error
                    )

                    when (registerState) {
                        is RegisterState.Error -> {
                            Text(
                                text = (registerState as RegisterState.Error).message,
                                color = Color.Red,
                                fontSize = 14.sp
                            )
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
                        if (isBusy) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Register")
                        }
                    }

                    Text(
                        text = "Already have an account? Login here",
                        fontSize = 14.sp,
                        color = Color(0xFF0D47A1),
                        modifier = Modifier.clickable {
                            navController.navigate(Destinations.Login.route) {
                                popUpTo("register") { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}