package com.example.collis.core.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.collis.presentation.components.CollisIcons

/**
 * Student Bottom Navigation Bar — custom implementation with full colour control.
 *
 * Replaces Material3 NavigationBar (which forces its own container colour)
 * with a Row-based layout inside a Surface whose background is fully
 * controllable via the theme.
 *
 * TABS:
 * 1. Home — Dashboard
 * 2. Schedule — Weekly timetable
 * 3. Tasks — Task Manager
 * 4. Profile — User settings
 */
@Composable
fun StudentBottomNavigation(
    navController: NavController,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem(
            title = "Home",
            selectedIcon = CollisIcons.homeFilledIcon,
            unselectedIcon = CollisIcons.homeIcon,
            route = Screen.StudentHome.route
        ),
        BottomNavItem(
            title = "Schedule",
            selectedIcon = CollisIcons.scheduleFilledIcon,
            unselectedIcon = CollisIcons.scheduleIcon,
            route = Screen.StudentSubjects.route
        ),
        BottomNavItem(
            title = "Tasks",
            selectedIcon = CollisIcons.tasksFilledIcon,
            unselectedIcon = CollisIcons.tasksIcon,
            route = Screen.StudentTasks.route
        ),
        BottomNavItem(
            title = "Profile",
            selectedIcon = CollisIcons.profileFilledIcon,
            unselectedIcon = CollisIcons.profileIcon,
            route = Screen.StudentProfile.route
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
            .navigationBarsPadding(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                BottomNavItemView(
                    selected = selected,
                    item = item,
                    onClick = { onNavigate(item.route) }
                )
            }
        }
    }
}

/**
 * Individual bottom-nav item with animated selection indicator.
 */
@Composable
private fun BottomNavItemView(
    selected: Boolean,
    item: BottomNavItem,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.9f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "nav_scale"
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "nav_tint"
    )
    val bgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer
        else Color.Transparent,
        label = "nav_bg"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.title,
            modifier = Modifier.size(24.dp),
            tint = iconTint
        )
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = iconTint,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}