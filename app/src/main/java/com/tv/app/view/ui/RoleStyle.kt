package com.tv.app.view.ui

import com.tv.app.utils.Role

data class RoleStyle(
    val backgroundAttr: Int,
    val textColorAttr: Int
)

val roleStyles = mapOf(
    Role.USER to RoleStyle(
        com.google.android.material.R.attr.colorPrimary,
        com.google.android.material.R.attr.colorOnPrimary
    ),
    Role.MODEL to RoleStyle(
        com.google.android.material.R.attr.colorSurface,
        com.google.android.material.R.attr.colorPrimary
    ),
    Role.SYSTEM to RoleStyle(
        com.google.android.material.R.attr.colorPrimaryContainer,
        com.google.android.material.R.attr.colorOnPrimaryContainer
    ),
    Role.FUNC to RoleStyle(
        com.google.android.material.R.attr.colorOnPrimarySurface,
        com.google.android.material.R.attr.colorPrimarySurface
    )
)