package com.tv.app.view

import android.app.Activity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.tv.app.databinding.ActivitySettingsBinding
import com.tv.app.chat.models.ChatManager
import com.tv.settings.SettingManager
import com.tv.settings.intances.Setting
import com.tv.utils.setBackAffair
import com.tv.utils.setViewInsets
import com.tv.app.view.ui.SettingsAdapter
import com.zephyr.extension.ui.PreloadLayoutManager
import com.zephyr.extension.widget.addLineDecoration
import com.zephyr.vbclass.ViewBindingActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : ViewBindingActivity<ActivitySettingsBinding>() {
    private lateinit var settingsAdapter: SettingsAdapter

    override fun ActivitySettingsBinding.initBinding() {
        enableEdgeToEdge()
        setSupportActionBar(toolBar)
        setInserts()

        toolBar.setBackAffair()

        val activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val name = result.data?.getStringExtra(com.tv.settings.intances.Setting.RESULT_NAME_KEY)
                        ?: return@registerForActivityResult
                    lifecycleScope.launch {
                        settingsAdapter.notifySettingOn(name)
                    }
                }
            }

        settingsAdapter = SettingsAdapter(activityResultLauncher)

        val preloadLayoutManager =
            PreloadLayoutManager(this@SettingsActivity, RecyclerView.VERTICAL)

        rv.adapter = settingsAdapter
        rv.layoutManager = preloadLayoutManager
        rv.addLineDecoration(this@SettingsActivity, RecyclerView.VERTICAL)

        settingsAdapter.submitList(
            com.tv.settings.SettingManager.settingMap.values.toList()
        )
    }

    private fun setInserts() = binding.run {
        toolBar.setViewInsets { insets ->
            topMargin = insets.top
        }
    }

    override fun onPause() {
        lifecycleScope.launch(Dispatchers.IO) {
            ChatManager.recreateModel() // 更新对话配置
        }
        super.onPause()
    }
}