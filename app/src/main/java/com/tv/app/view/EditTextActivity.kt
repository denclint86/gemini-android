package com.tv.app.view

import android.app.Activity
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import com.tv.app.R
import com.tv.app.databinding.ActivityEditTextBinding
import com.tv.app.model.SettingManager
import com.tv.app.settings.intances.Setting
import com.tv.app.utils.setBackAffair
import com.tv.app.utils.setViewInsets
import com.tv.app.utils.withLifecycleScope
import com.zephyr.extension.widget.toast
import com.zephyr.net.toJson
import com.zephyr.vbclass.ViewBindingActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EditTextActivity : ViewBindingActivity<ActivityEditTextBinding>() {
    private var setting: Setting<*>? = null

    override fun ActivityEditTextBinding.initBinding() {
        enableEdgeToEdge()
        setSupportActionBar(toolBar)
        setInserts()
        toolBar.setBackAffair()

        val name =
            intent.getStringExtra(Setting.NAME_KEY)


        name?.run {
            supportActionBar?.title = "修改$name"

            setting = SettingManager.settingMap[name]
            setting?.let {
                et.setText(it.value()!!.toString()) // 显示当前值
            }
        } ?: run { // 进入关于模式
            supportActionBar?.title = "关于"
            val builder = StringBuilder()
            SettingManager.settingMap.forEach { (k, v) ->
                builder.append("$k: ")
                builder.append(v.bean.toJson())
                builder.append("\n")
            }
            et.setText(builder.toString())
        }

        val resultIntent = Intent().putExtra(Setting.RESULT_NAME_KEY, name)
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