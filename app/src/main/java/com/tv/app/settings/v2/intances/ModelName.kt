package com.tv.app.settings.v2.intances

import com.tv.app.settings.Model
import com.tv.app.settings.v2.Default
import com.tv.app.settings.v2.Setting2

class ModelName : Setting2<String>() {
    override val name: String
        get() = "模型"
    override val default: Bean<String>
        get() = Bean(
            value = Default.MODEL_NAME,
            enabled = true
        )
    override val kind: Kind
        get() = Kind.USE_DIALOG


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