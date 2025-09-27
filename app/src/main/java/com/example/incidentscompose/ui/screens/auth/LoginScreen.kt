package com.example.incidentscompose.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
TODO: meerdere onderdelen in losse components opsplitsen:
 textvakje
 knop?

 logo fixen
 */



@Composable
fun LoginScreen(
    isBusy: Boolean = false,
    validationError: String? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF42A5F5), // Secondary
                        Color(0xFF6EE7FF)  // Light gradient
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "INCIDENTS",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 20.dp)
            )


            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier
                    .width(360.dp)
                    .padding(0.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(30.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    TextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    )

                    TextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    )

                    if (!validationError.isNullOrEmpty()) {
                        Text(
                            text = validationError,
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = { /* TODO: login */ },
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Login",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { /* TODO: anonymous */ },
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Report anonymously",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Don't have an account? Register here",
                        fontSize = 14.sp,
                        color = Color(0xFF0D47A1), // Primary
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable { /* TODO: register */ }
                    )
                }
            }
        }

        if (isBusy) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White
                )
            }
        }
    }
}

