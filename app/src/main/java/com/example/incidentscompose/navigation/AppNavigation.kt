package com.example.incidentscompose.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.incidentscompose.ui.screens.auth.LoginScreen
import com.example.incidentscompose.ui.screens.auth.RegisterScreen
import com.example.incidentscompose.ui.screens.auth.UserProfileScreen
import com.example.incidentscompose.ui.screens.incidents.MyIncidentDetailScreen
import com.example.incidentscompose.ui.screens.incidents.MyIncidentListScreen
import com.example.incidentscompose.ui.screens.management.IncidentDetailScreen
import com.example.incidentscompose.ui.screens.management.IncidentListScreen
import com.example.incidentscompose.ui.screens.management.IncidentMapScreen
import com.example.incidentscompose.ui.screens.management.UserManagementScreen
import com.example.incidentscompose.ui.screens.incidents.ReportIncidentScreen

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(LoginKey)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<LoginKey> {
                LoginScreen(
                    onNavigateToIncidentList = {
                        backStack.removeAll { true }
                        backStack.add(MyIncidentListKey)
                    },
                    onNavigateToReport = { backStack.add(ReportIncidentKey) },
                    onNavigateToRegister = { backStack.add(RegisterKey) }
                )
            }

            entry<RegisterKey> {
                RegisterScreen(
                    onNavigateToLogin = {
                        backStack.removeLastOrNull() }
                )
            }

            entry<MyIncidentListKey> {
                TODO("to be implemented")
            }

            entry<MyIncidentDetailKey> {
                TODO("to be implemented")
            }

            entry<ReportIncidentKey> {
                TODO("to be implemented")
            }

            entry<IncidentListKey> {
                TODO("to be implemented")
            }

            entry<IncidentMapKey> {
                TODO("to be implemented")
            }

            entry<UserManagementKey> {
                TODO("to be implemented")
            }

            entry<UserProfileKey>{
                TODO("to be implemented")
                // data class UserProfileKey(val userJson: String) : NavKey
            }

            entry<IncidentDetailKey> {
                TODO("to be implemented")
                // data class IncidentDetailKey(val incidentId: Long) : NavKey
            }
        }
    )
}

