package com.example.incidentscompose.ui.screens.auth

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.incidentscompose.R
import com.example.incidentscompose.navigation.Destinations
import com.example.incidentscompose.ui.components.LoadingOverlay
import com.example.incidentscompose.viewmodel.AutoLoginState
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

    // Track if we're currently in login process
    val isLoggingIn = remember { derivedStateOf {
        loginState is LoginState.Loading || (isBusy && loginState !is LoginState.Error)
    } }

    LaunchedEffect(Unit) {
        viewModel.checkAutoLogin()
    }

    LaunchedEffect(autoLoginState) {
        when (autoLoginState) {
            is AutoLoginState.TokenFound -> {
                navController.navigate(Destinations.MyIncidentList.route) {
                    popUpTo("login") { inclusive = true }
                }
            }
            else -> {}
        }
    }

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Success -> {
                navController.navigate(Destinations.MyIncidentList.route) {
                    popUpTo("login") { inclusive = true }
                }
            }
            else -> {}
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
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(200.dp)
                    .padding(vertical = 16.dp)
            )

            Surface(
                modifier = Modifier.width(360.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(30.dp)
                ) {
                    TextField(
                        value = username,
                        onValueChange = {
                            username = it
                            if (loginState is LoginState.Error) {
                                viewModel.clearLoginState()
                            }
                        },
                        placeholder = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        isError = loginState is LoginState.Error
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    TextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (loginState is LoginState.Error) {
                                viewModel.clearLoginState()
                            }
                        },
                        placeholder = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        isError = loginState is LoginState.Error
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (loginState is LoginState.Error) {
                        Text(
                            text = (loginState as LoginState.Error).message,
                            color = Color.Red,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    Button(
                        onClick = {
                            viewModel.login(username, password)
                        },
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoggingIn.value && username.isNotBlank() && password.isNotBlank()
                    ) {
                        Text("Login")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { /* TODO: anonymous report */ },
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoggingIn.value
                    ) {
                        Text("Report anonymously")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Don't have an account? Register here",
                        fontSize = 14.sp,
                        color = Color(0xFF0D47A1),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!isLoggingIn.value) {
                                    navController.navigate(Destinations.Register.route)
                                }
                            }
                    )
                }
            }
        }

        LoadingOverlay(
            isLoading = autoLoginState is AutoLoginState.Checking || isLoggingIn.value
        )
    }
}