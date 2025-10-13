package com.example.incidentscompose.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.incidentscompose.R
import com.example.incidentscompose.navigation.Destinations

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: Int,
    val requiredRole: Set<String>
) {
    data object List : BottomNavItem(
        route = Destinations.IncidentList.route,
        title = "List",
        icon = R.drawable.list,
        requiredRole = setOf("OFFICIAL", "ADMIN")
    )

    data object Map : BottomNavItem(
        route = Destinations.IncidentMap.route,
        title = "Map",
        icon = R.drawable.map,
        requiredRole = setOf("OFFICIAL", "ADMIN")
    )

    data object Users : BottomNavItem(
        route = Destinations.UserManagement.route,
        title = "Users",
        icon = R.drawable.users,
        requiredRole = setOf("ADMIN")
    )

    data object Profile : BottomNavItem(
        route = Destinations.MyIncidentList.route,
        title = "Profile",
        icon = R.drawable.profile,
        requiredRole = setOf("OFFICIAL", "ADMIN")
    )
}

@Composable
fun BottomNavBar(
    currentRoute: String,
    userRole: String?,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (userRole == "USER" || userRole == null) {
        return
    }

    val navItems = listOf(
        BottomNavItem.List,
        BottomNavItem.Map,
        BottomNavItem.Users,
        BottomNavItem.Profile
    ).filter { item ->
        userRole in item.requiredRole
    }

    NavigationBar(
        modifier = modifier.fillMaxWidth().navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        navItems.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(item.route) },
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.title,
                        modifier = Modifier.size(32.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color(0xFF6B7280),
                    unselectedTextColor = Color(0xFF6B7280),
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ),
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }
}
