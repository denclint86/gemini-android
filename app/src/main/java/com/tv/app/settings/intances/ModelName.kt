package com.tv.app.settings.intances

import com.tv.app.settings.StringSetting
import com.tv.app.settings.values.Default
import com.tv.app.settings.values.Model
import com.tv.app.settings.values.Names

class ModelName : StringSetting() {
    override val name: String
        get() = Names.MODEL_NAME
    override val default: Bean<String>
        get() = Bean(
            value = Default.MODEL_NAME,
            isEnabled = true
        )
    override val kind: Kind
        get() = Kind.DIALOG_SELECT
    override val canSetEnabled: Boolean
        get() = false


    override fun onValidate(bean: Bean<String>): Result {
        val models = Model.entries.toSet()
        val isContained = models.any { it.string == bean.value }

        return if (isContained) {
            Result(true, null)
        } else {
            Result(false, "不支持的模型")
        }
    }
} 