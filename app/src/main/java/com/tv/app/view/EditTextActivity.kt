package com.tv.app.view

import android.app.Activity
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import com.tv.app.R
import com.tv.app.databinding.ActivityEditTextBinding
import com.tv.settings.SettingManager
import com.tv.settings.intances.Setting
import com.tv.utils.setBackAffair
import com.tv.utils.setViewInsets
import com.tv.utils.withLifecycleScope
import com.zephyr.extension.widget.toast
import com.zephyr.vbclass.ViewBindingActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EditTextActivity : ViewBindingActivity<ActivityEditTextBinding>() {
    private var setting: com.tv.settings.intances.Setting<*>? = null

    override fun ActivityEditTextBinding.initBinding() {
        enableEdgeToEdge()
        setSupportActionBar(toolBar)
        setInserts()
        toolBar.setBackAffair()

        val name =
            intent.getStringExtra(com.tv.settings.intances.Setting.NAME_KEY)


        name?.run {
            supportActionBar?.title = "修改$name"

            setting = com.tv.settings.SettingManager.settingMap[name]
            setting?.let {
                et.setText(it.value()!!.toString()) // 显示当前值
            }
        } ?: finish()

        val resultIntent = Intent().putExtra(com.tv.settings.intances.Setting.RESULT_NAME_KEY, name)
        setResult(Activity.RESULT_OK, resultIntent)
    }

    private fun setInserts() = binding.run {
        toolBar.setViewInsets { insets ->
            topMargin = insets.top
        }

        et.setViewInsets { inserts ->
            bottomMargin = inserts.bottom
        }
    }

    private fun save() {
        val text = binding.et.text.toString().trim()

        this.withLifecycleScope(Dispatchers.IO) {
            val result = setting?.set(text) ?: return@withLifecycleScope

            withContext(Dispatchers.Main) {
                if (result.isSuccess)
                    "已保存".toast()
                else
                    result.msg.toast()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu);
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.toolbar_save ->
                save()

            else -> throw IllegalStateException("")
        }
        return super.onOptionsItemSelected(item);
    }
}