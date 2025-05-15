package com.tv.settings.intances

import com.tv.settings.values.Default
import com.tv.settings.values.Names

class CandidateCount : IntSetting() {
    override val name: String
        get() = Names.CANDIDATE_COUNT
    override val default: Bean<Int>
        get() = Bean(
            value = Default.CANDIDATE_COUNT,
            isEnabled = true
        )
    override val kind: Kind
        get() = Kind.DIALOG_EDIT
    override val canSetEnabled: Boolean
        get() = true


    override fun onValidate(bean: Bean<Int>): Result {
        return if (bean.value in 1..8) {
            Result(true, null)
        } else {
            Result(false, "候选数量必须在 1 至 8 之间")
        }
    }
} 