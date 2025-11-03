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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.incidentscompose.R
import com.example.incidentscompose.navigation.Destinations
import com.example.incidentscompose.ui.components.IncidentsTextField
import com.example.incidentscompose.ui.components.LoadingOverlay
import com.example.incidentscompose.viewmodel.RegisterState
import com.example.incidentscompose.viewmodel.RegisterViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = koinViewModel()
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
                    context.getString(R.string.registration_successful_you_can_now_log_in),
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
                    Text(
                        text = stringResource(R.string.create_account),
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
                        placeholder = stringResource(R.string.username),
                        isError = registerState is RegisterState.Error
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    IncidentsTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (registerState is RegisterState.Error) {
                                viewModel.clearRegisterState()
                            }
                        },
                        placeholder = stringResource(R.string.email),
                        isError = registerState is RegisterState.Error
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    IncidentsTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (registerState is RegisterState.Error) {
                                viewModel.clearRegisterState()
                            }
                        },
                        placeholder = stringResource(R.string.password),
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
                        placeholder = stringResource(R.string.confirm_password),
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
                        Text(stringResource(R.string.register))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = stringResource(R.string.already_have_an_account_login_here),
                        fontSize = 14.sp,
                        color = Color(0xFF0D47A1),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(Destinations.Login.route) {
                                    popUpTo(Destinations.Register.route) { inclusive = true }
                                }
                            }
                    )
                }
            }
        }

        LoadingOverlay(isLoading = isBusy)
    }
}