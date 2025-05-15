package com.tv.app.view.ui

import com.tv.utils.Role

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
        com.google.android.material.R.attr.colorOnError,
        com.google.android.material.R.attr.colorError
    ),
    Role.FUNC to RoleStyle(
        com.google.android.material.R.attr.colorError,
        com.google.android.material.R.attr.colorOnError
    )
)