package com.tv.app.settings

import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.RecyclerView
import com.tv.app.databinding.ActivitySettingsBinding
import com.tv.app.ui.SettingsAdapter
import com.zephyr.extension.ui.PreloadLayoutManager
import com.zephyr.extension.widget.addLineDecoration
import com.zephyr.extension.widget.toast
import com.zephyr.vbclass.ViewBindingActivity
import kotlinx.coroutines.runBlocking

class SettingsActivity : ViewBindingActivity<ActivitySettingsBinding>() {
    override fun ActivitySettingsBinding.initBinding() {
        enableEdgeToEdge()
        setInserts()

        runBlocking {
//            et.setText(SettingsRepository.getSystemPrompt())
        }

        val settingsAdapter = SettingsAdapter { setting ->
            setting.name.toast()
        }
        val preloadLayoutManager =
            PreloadLayoutManager(this@SettingsActivity, RecyclerView.VERTICAL)

        rv.adapter = settingsAdapter
        rv.layoutManager = preloadLayoutManager
        rv.addLineDecoration(this@SettingsActivity, RecyclerView.VERTICAL)

        settingsAdapter.submitList(
            listOf()
        )
    }

    private fun setInserts() = binding.run {
//        et.setViewInsets { insets ->
//            leftMargin = insets.left
//            rightMargin = insets.right
//            topMargin = insets.top
//            bottomMargin = insets.bottom
//        }
    }

//    override fun onBackPressed() {
//        runBlocking {
//            val text = binding.et.text.toString().trim()
//            val oriText = SettingsRepository.getSystemPrompt().trim()
//
//            if (text == oriText) {
//                super.onBackPressed()
//            } else {
//                showInputDialog("设置", "保存吗?") { save ->
//                    lifecycleScope.launch(Dispatchers.IO) {
//                        if (save) {
//                            SettingsRepository.setSystemPrompt(text)
//                            "已保存".toast()
//                        }
//                        withContext(Dispatchers.Main) {
//                            super.onBackPressed()
//                        }
//                    }
//                }
//            }
//        }
//    }
}