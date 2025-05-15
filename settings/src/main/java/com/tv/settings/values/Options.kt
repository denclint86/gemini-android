package com.tv.settings.values

import com.tv.settings.beans.Language
import com.tv.settings.beans.Voice
import com.tv.settings.intances.Setting

fun Setting<*>.getOptions(): List<String> {
    if (this.kind != Setting.Kind.DIALOG_SELECT) throw IllegalStateException("not kind of 'DIALOG_SELECT'")

    when (this.name) {
        Names.MODEL_NAME -> return Model.entries.map { it.string }
        Names.LIVE_VOICE_NAME -> return Voice.entries.map { it.string }
        Names.LIVE_LANGUAGE -> return Language.entries.map { it.string }
    }

    TODO()
}

fun Setting<*>.getIndex(list: List<String>): Int {
    if (this.kind != Setting.Kind.DIALOG_SELECT) throw IllegalStateException("not kind of 'DIALOG_SELECT'")

    when (this.name) {
        Names.MODEL_NAME -> return list.indexOf(value(true))
        Names.LIVE_VOICE_NAME -> return list.indexOf(value(true))
        Names.LIVE_LANGUAGE -> return list.indexOf(value(true))
    }

    TODO()
}