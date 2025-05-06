package com.tv.app.settings.values

import com.tv.app.settings.Setting

fun Setting<*>.getOptions(): List<String> {
    if (this.kind != Setting.Kind.DIALOG_SELECT) throw IllegalStateException("not kind of 'DIALOG_SELECT'")

    when (this.name) {
        Names.MODEL_NAME -> return Model.entries.map { it.string }
    }

    TODO()
}

fun Setting<*>.getIndex(list: List<String>): Int {
    if (this.kind != Setting.Kind.DIALOG_SELECT) throw IllegalStateException("not kind of 'DIALOG_SELECT'")

    when (this.name) {
        Names.MODEL_NAME -> return list.indexOf(value(true))
    }

    TODO()
}