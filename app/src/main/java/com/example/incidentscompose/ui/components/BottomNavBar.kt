package com.example.incidentscompose.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.incidentscompose.R
import com.example.incidentscompose.navigation.IncidentMapKey
import com.example.incidentscompose.navigation.MyIncidentListKey
import com.example.incidentscompose.navigation.UserManagementKey
import com.example.incidentscompose.navigation.IncidentListKey
import androidx.navigation3.runtime.NavKey

sealed class BottomNavItem(
    val key: NavKey,
    val titleResId: Int,
    val icon: Int,
    val requiredRole: Set<String>
) {
    data object List : BottomNavItem(
        key = IncidentListKey,
        titleResId = R.string.list,
        icon = R.drawable.list,
        requiredRole = setOf("OFFICIAL", "ADMIN")
    )

    data object Map : BottomNavItem(
        key = IncidentMapKey,
        titleResId = R.string.map,
        icon = R.drawable.map,
        requiredRole = setOf("OFFICIAL", "ADMIN")
    )

    data object Users : BottomNavItem(
        key = UserManagementKey,
        titleResId = R.string.users,
        icon = R.drawable.users,
        requiredRole = setOf("ADMIN")
    )

    data object Profile : BottomNavItem(
        key = MyIncidentListKey,
        titleResId = R.string.profile,
        icon = R.drawable.profile,
        requiredRole = setOf("OFFICIAL", "ADMIN")
    )
}

@Composable
fun BottomNavBar(
    currentKey: NavKey,
    userRole: String?,
    onNavigateTo: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    if (userRole == "USER" || userRole == null) return

    val navItems = listOf(
        BottomNavItem.List,
        BottomNavItem.Map,
        BottomNavItem.Users,
        BottomNavItem.Profile
    ).filter { userRole in it.requiredRole }

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        navItems.forEach { item ->
            val isSelected = currentKey::class == item.key::class

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigateTo(item.key) },
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = stringResource(item.titleResId),
                        modifier = Modifier.size(32.dp)
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.titleResId),
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
