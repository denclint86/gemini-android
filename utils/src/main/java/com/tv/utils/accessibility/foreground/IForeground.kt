package com.tv.utils.accessibility.foreground

interface IForeground {
    val currentApp: Pair<String, String>?

    fun update(packageName: String?)

    fun clear()

    fun addOnChangeListener(callback: ChangeCallback)

    fun removeOnChangeListener(callback: ChangeCallback)


    fun interface ChangeCallback {
        fun onChange(app: Pair<String, String>?)
    }
}