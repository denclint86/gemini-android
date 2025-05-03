package com.tv.app.ui

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.DiffUtil
import com.tv.app.databinding.LayoutSettingItemBinding
import com.tv.app.setRippleEffect
import com.tv.app.setTextColorFromAttr
import com.tv.app.settings.EditTextActivity
import com.tv.app.settings.Setting
import com.tv.app.settings.values.getIndex
import com.tv.app.settings.values.getOptions
import com.tv.app.showInputDialog
import com.tv.app.showSingleChoiceDialog
import com.tv.app.withLifecycleScope
import com.zephyr.extension.widget.toast
import com.zephyr.vbclass.ui.ViewBindingListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsAdapter(
    private val activityResultLauncher: ActivityResultLauncher<Intent>
) : ViewBindingListAdapter<LayoutSettingItemBinding, Setting<*>>(Callback()) {

    private var isInputDialogShown = false
    private var isSelectDialogShown = false

    private fun Context.onClickEvent(setting: Setting<*>, position: Int) {
        when (setting.kind) {
            Setting.Kind.READ_ONLY -> {
            }

            Setting.Kind.DIALOG_EDIT ->
                openDialogToSet(setting, position)

            Setting.Kind.DIALOG_SELECT ->
                openDialogToSelect(setting, position)

            Setting.Kind.ACTIVITY ->
                startActivityToSet(setting)

            Setting.Kind.DIRECT -> {}
        }
    }

    private fun Context.openDialogToSelect(setting: Setting<*>, position: Int) {
        if (isSelectDialogShown) return
        isSelectDialogShown = true

        val options = setting.getOptions()
        val index = setting.getIndex(options)

        showSingleChoiceDialog(
            title = "选择${setting.name}",
            items = options,
            selectedIndex = index
        ) { newValue ->
            isSelectDialogShown = false
            if (newValue == null) return@showSingleChoiceDialog

            this.withLifecycleScope(Dispatchers.IO) {
                val result = setting.set(newValue)

                withContext(Dispatchers.Main) {
                    if (result.isSuccess)
                        notifyItemChanged(position)
                    else
                        result.msg.toast()
                }
            }
        }
    }

    private fun Context.openDialogToSet(setting: Setting<*>, position: Int) {
        if (isInputDialogShown) return
        isInputDialogShown = true

        showInputDialog(
            title = "修改${setting.name}",
            initText = (setting.value ?: "").toString()
        ) { str ->
            isInputDialogShown = false
            if (str == null) return@showInputDialog

            this.withLifecycleScope(Dispatchers.IO) {
                val result = setting.set(str)

                withContext(Dispatchers.Main) {
                    if (result.isSuccess)
                        notifyItemChanged(position)
                    else
                        result.msg.toast()
                }
            }
        }
    }

    private fun Context.startActivityToSet(setting: Setting<*>) {
        val i = Intent(this, EditTextActivity::class.java)
        i.putExtra(Setting.NAME_KEY, setting.name)
        activityResultLauncher.launch(i)
    }

    suspend fun notifySettingOn(name: String) {
        val index = withContext(Dispatchers.IO) {
            currentList.indexOfFirst { it.name == name }
        }
        withContext(Dispatchers.Main) {
            notifyItemChanged(index)
        }
    }

    private fun TextView.setColorByEnabled(isEnabled: Boolean) {
        val attr = if (isEnabled)
            com.google.android.material.R.attr.colorOnSurface
        else
            com.google.android.material.R.attr.colorOnSurfaceVariant
        setTextColorFromAttr(attr)
    }

    override fun LayoutSettingItemBinding.onBindViewHolder(data: Setting<*>?, position: Int) {
        if (data == null) return
        val context = root.context

        title.text = data.name
        title.setColorByEnabled(data.isEnabled)

        preview.text = data.preview.value.toString()
        if (data.kind == Setting.Kind.DIRECT) {
            preview.visibility = View.GONE
        } else {
            preview.visibility = View.VISIBLE
        }

        val isReadOnly = (data.kind == Setting.Kind.READ_ONLY)
        root.setRippleEffect(!isReadOnly)
        sw.isChecked = data.preview.isEnabled
        sw.visibility = if (!data.canSetEnabled) View.INVISIBLE else View.VISIBLE
        sw.setOnCheckedChangeListener { _, isChecked ->
            context.withLifecycleScope(Dispatchers.IO) {
                data.set { isEnabled = isChecked }
                withContext(Dispatchers.Main) {
                    title.setColorByEnabled(isChecked)
                }
            }
        }

        root.setOnClickListener {
            if (data.kind == Setting.Kind.DIRECT)
                sw.isChecked = !sw.isChecked
            else
                context.onClickEvent(data, position)
        }
    }

    class Callback : DiffUtil.ItemCallback<Setting<*>>() {
        override fun areItemsTheSame(oldItem: Setting<*>, newItem: Setting<*>): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Setting<*>, newItem: Setting<*>): Boolean {
            return oldItem == newItem
        }
    }
}