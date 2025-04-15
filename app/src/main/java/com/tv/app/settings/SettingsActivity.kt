package com.tv.app.settings

import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.tv.app.databinding.ActivitySettingsBinding
import com.tv.app.setViewInsets
import com.tv.app.showInputDialog
import com.zephyr.extension.widget.toast
import com.zephyr.vbclass.ViewBindingActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class SettingsActivity : ViewBindingActivity<ActivitySettingsBinding>() {
    override fun ActivitySettingsBinding.initBinding() {
        enableEdgeToEdge()
        setInserts()

        runBlocking {
            et.setText(SettingsRepository.getSystemPrompt())
        }
    }

    private fun setInserts() = binding.run {
        et.setViewInsets { insets ->
            leftMargin = insets.left
            rightMargin = insets.right
            topMargin = insets.top
            bottomMargin = insets.bottom
        }
    }

    override fun onBackPressed() {
        runBlocking {
            val text = binding.et.text.toString().trim()
            val oriText = SettingsRepository.getSystemPrompt().trim()

            if (text == oriText) {
                super.onBackPressed()
            } else {
                showInputDialog("设置", "保存吗?") { save ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (save) {
                            SettingsRepository.setSystemPrompt(text)
                            "已保存".toast()
                        }
                        withContext(Dispatchers.Main) {
                            super.onBackPressed()
                        }
                    }
                }
            }
        }
    }
}