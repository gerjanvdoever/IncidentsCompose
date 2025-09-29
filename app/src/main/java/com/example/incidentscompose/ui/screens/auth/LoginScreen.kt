package com.example.incidentscompose.ui.screens.auth

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.incidentscompose.navigation.Destinations
import com.example.incidentscompose.viewmodel.LoginState
import com.example.incidentscompose.viewmodel.LoginViewModel
import org.koin.compose.koinInject

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = koinInject()
) {
    val isBusy by viewModel.isBusy.collectAsState()
    val loginState by viewModel.loginState.collectAsState()
    val autoLoginState by viewModel.autoLoginState.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Auto-login check
    LaunchedEffect(Unit) {
        viewModel.checkAutoLogin()
    }

    // Handle auto-login result
    LaunchedEffect(autoLoginState) {
        when (autoLoginState) {
            is com.example.incidentscompose.viewmodel.AutoLoginState.TokenFound -> {
                navController.navigate(Destinations.MyIncidentList.route) {
                    popUpTo("login") { inclusive = true }
                }
            }
            else -> {
                // Do nothing, stay on login screen
            }
        }
    }

    // Handle login result
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> {
                navController.navigate(Destinations.MyIncidentList.route) {
                    popUpTo("login") { inclusive = true }
                }
            }
            else -> {
                // Other states handled in the UI
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

                    TextField(
                        value = username,
                        onValueChange = {
                            username = it
                            // Clear error when user starts typing
                            if (loginState is LoginState.Error) {
                                viewModel.clearLoginState()
                            }
                        },
                        placeholder = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        isError = loginState is LoginState.Error
                    )

                    TextField(
                        value = password,
                        onValueChange = {
                            password = it
                            // Clear error when user starts typing
                            if (loginState is LoginState.Error) {
                                viewModel.clearLoginState()
                            }
                        },
                        placeholder = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        isError = loginState is LoginState.Error
                    )

                    when (loginState) {
                        is LoginState.Error -> {
                            Text(
                                text = (loginState as LoginState.Error).message,
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
                            viewModel.login(username, password)
                        },
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isBusy && username.isNotBlank() && password.isNotBlank()
                    ) {
                        if (isBusy) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Login")
                        }
                    }

                    Button(
                        onClick = { /* TODO: anonymous report */ },
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isBusy
                    ) {
                        Text("Report anonymously")
                    }

                    Text(
                        text = "Don't have an account? Register here",
                        fontSize = 14.sp,
                        color = Color(0xFF0D47A1),
                        modifier = Modifier.clickable { /* TODO: navigate to register */ }
                    )
                }
            }
        }

        // Loading overlay for auto-login check
        if (autoLoginState is com.example.incidentscompose.viewmodel.AutoLoginState.Checking) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Checking saved login...", color = Color.White)
                }
            }
        }
    }
}