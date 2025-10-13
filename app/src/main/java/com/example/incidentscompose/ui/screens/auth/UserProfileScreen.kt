package com.example.incidentscompose.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.incidentscompose.data.model.UserResponse
import com.example.incidentscompose.navigation.Destinations
import com.example.incidentscompose.ui.components.IncidentsTextField
import com.example.incidentscompose.ui.components.LoadingOverlay
import com.example.incidentscompose.ui.components.TopNavBar
import com.example.incidentscompose.util.ChangeUserValidationHelper
import com.example.incidentscompose.viewmodel.UserViewModel
import kotlinx.serialization.json.Json
import org.koin.androidx.compose.koinViewModel
import java.net.URLDecoder
import android.widget.Toast
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun UserProfileScreen(
    navController: NavController,
    userJson: String?,
    viewModel: UserViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val initialUser = remember(userJson) {
        userJson?.let { json ->
            try {
                val decodedJson = URLDecoder.decode(json, "UTF-8")
                Json.decodeFromString<UserResponse>(decodedJson)
            } catch (e: Exception) {
                null
            }
        }
    }

    if (initialUser == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    val isBusy by viewModel.isBusy.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var username by remember { mutableStateOf(initialUser.username) }
    var email by remember { mutableStateOf(initialUser.email) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_LONG).show()
            navController.navigate(Destinations.MyIncidentList.route) {
                popUpTo(Destinations.MyIncidentList.route) { inclusive = true }
            }
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.resetUpdateState()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopNavBar(
                    title = "Edit Profile",
                    showBackButton = true,
                    onBackClick = { navController.popBackStack() },
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    textColor = MaterialTheme.colorScheme.onSurface
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Personal Information",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            IncidentsTextField(
                                value = username,
                                onValueChange = { username = it },
                                placeholder = "Username"
                            )

                            IncidentsTextField(
                                value = email,
                                onValueChange = { email = it },
                                placeholder = "Email"
                            )
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Security",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Leave blank to keep current password",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )

                            IncidentsTextField(
                                value = currentPassword,
                                onValueChange = { currentPassword = it },
                                placeholder = "Current password",
                                isPassword = true
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )

                            IncidentsTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                placeholder = "New password",
                                isPassword = true
                            )

                            IncidentsTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                placeholder = "Confirm new password",
                                isPassword = true
                            )
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Button(
                        onClick = {
                            if (ChangeUserValidationHelper.validateUserProfile(
                                    context = context,
                                    username = username,
                                    email = email,
                                    currentPassword = currentPassword,
                                    newPassword = newPassword,
                                    confirmPassword = confirmPassword
                                )) {
                                viewModel.updateProfile(
                                    username = username,
                                    email = email,
                                    newPassword = newPassword.ifBlank { null }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp),
                        enabled = !isBusy,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(25.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 4.dp
                        )
                    ) {
                        Text(
                            text = "Save Changes",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                TextButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    enabled = !isBusy
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        LoadingOverlay(isLoading = isBusy)
    }
}