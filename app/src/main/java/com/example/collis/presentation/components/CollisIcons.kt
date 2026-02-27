package com.example.collis.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import mr.dev.collis.R

@Immutable
object CollisIcons {


    val notificationIcon: ImageVector
        @Composable
        get() = ImageVector.vectorResource(id = R.drawable.ic_notification)

    val homeIcon: ImageVector
        @Composable
        get() = ImageVector.vectorResource(R.drawable.home)
    val scheduleIcon: ImageVector
        @Composable
        get() = ImageVector.vectorResource(R.drawable.schedule)
    val tasksIcon: ImageVector
        @Composable
        get() = ImageVector.vectorResource(R.drawable.tasks)
    val profileIcon: ImageVector
        @Composable
        get() = ImageVector.vectorResource(R.drawable.profile)
    val homeFilledIcon: ImageVector
        @Composable
        get() = ImageVector.vectorResource(R.drawable.home_filled)
    val scheduleFilledIcon: ImageVector
        @Composable
        get() = ImageVector.vectorResource(R.drawable.schedule_filled)
    val tasksFilledIcon: ImageVector
        @Composable
        get() = ImageVector.vectorResource(R.drawable.tasks_filled)
    val profileFilledIcon: ImageVector
        @Composable
        get() = ImageVector.vectorResource(R.drawable.profile_filled)


}




