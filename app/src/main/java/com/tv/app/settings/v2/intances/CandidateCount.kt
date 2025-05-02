package com.tv.app.settings.v2.intances

import com.tv.app.settings.v2.Default
import com.tv.app.settings.v2.Setting2

class CandidateCount : Setting2<Int>() {
    override val name: String
        get() = "candidate_count"
    override val default: Bean<Int>
        get() = Bean(
            value = Default.CANDIDATE_COUNT,
            enabled = true
        )
    override val kind: Kind
        get() = Kind.DIALOG_EDIT


    override fun onValidate(bean: Bean<Int>): Result {
        return if (bean.value in 1..8) {
            Result(true, null)
        } else {
            Result(false, "候选数量必须在 1 至 4 之间")
        }
    }
} 