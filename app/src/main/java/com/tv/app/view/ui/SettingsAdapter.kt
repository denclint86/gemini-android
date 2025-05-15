package com.tv.app.view.ui

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.DiffUtil
import com.tv.app.databinding.LayoutSettingItemBinding
import com.tv.settings.intances.Setting
import com.tv.settings.values.getIndex
import com.tv.settings.values.getOptions
import com.tv.utils.context
import com.tv.utils.setRippleEffect
import com.tv.utils.setTextColorFromAttr
import com.tv.utils.showInputDialog
import com.tv.utils.showSingleChoiceDialog
import com.tv.utils.withLifecycleScope
import com.tv.app.view.EditTextActivity
import com.zephyr.extension.widget.toast
import com.zephyr.vbclass.ui.ViewBindingListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SettingsAdapter(
    private val activityResultLauncher: ActivityResultLauncher<Intent>
) : ViewBindingListAdapter<LayoutSettingItemBinding, com.tv.settings.intances.Setting<*>>(Callback()) {

    private var isInputDialogShown = false
    private var isSelectDialogShown = false

    private fun Context.onClickEvent(setting: com.tv.settings.intances.Setting<*>) {
        when (setting.kind) {
            com.tv.settings.intances.Setting.Kind.READ_ONLY -> {
            }

            com.tv.settings.intances.Setting.Kind.DIALOG_EDIT ->
                openDialogToSet(setting)

            com.tv.settings.intances.Setting.Kind.DIALOG_SELECT ->
                openDialogToSelect(setting)

            com.tv.settings.intances.Setting.Kind.ACTIVITY ->
                startActivityToSet(setting)

            com.tv.settings.intances.Setting.Kind.DIRECT -> {}
        }
    }

    private fun Context.openDialogToSelect(setting: com.tv.settings.intances.Setting<*>) {
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
                        notifySettingOn(setting.name)
                    else
                        result.msg.toast()
                }
            }
        }
    }

    private fun Context.openDialogToSet(setting: com.tv.settings.intances.Setting<*>) {
        if (isInputDialogShown) return
        isInputDialogShown = true

        showInputDialog(
            title = "修改${setting.name}",
            initText = (setting.value(true)).toString()
        ) { str ->
            isInputDialogShown = false
            if (str == null) return@showInputDialog

            this.withLifecycleScope(Dispatchers.IO) {
                val result = setting.set(str)

                withContext(Dispatchers.Main) {
                    if (!result.isSuccess)
                        result.msg.toast()
                    notifySettingOn(setting.name)
                }
            }
        }
    }

    private fun Context.startActivityToSet(setting: com.tv.settings.intances.Setting<*>) {
        val i = Intent(this, EditTextActivity::class.java)
        i.putExtra(com.tv.settings.intances.Setting.NAME_KEY, setting.name)
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

    override fun LayoutSettingItemBinding.onBindViewHolder(data: com.tv.settings.intances.Setting<*>?, position: Int) {
        if (data == null) return

        title.text = data.name
        title.setColorByEnabled(data.isEnabled())

        preview.text = data.value(true).toString()
        if (data.kind == com.tv.settings.intances.Setting.Kind.DIRECT) {
            preview.visibility = View.GONE
        } else {
            preview.visibility = View.VISIBLE
        }

        val isReadOnly = (data.kind == com.tv.settings.intances.Setting.Kind.READ_ONLY)
        root.setRippleEffect(!isReadOnly)

        sw.isChecked = data.isEnabled()
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
            if (data.kind == com.tv.settings.intances.Setting.Kind.DIRECT)
                context.withLifecycleScope(Dispatchers.IO) {
                    data.set { isEnabled = true }
                    withContext(Dispatchers.Main) {
                        notifySettingOn(data.name)
                    }
                }
            else
                context.onClickEvent(data)
        }
    }

    class Callback : DiffUtil.ItemCallback<com.tv.settings.intances.Setting<*>>() {
        override fun areItemsTheSame(oldItem: com.tv.settings.intances.Setting<*>, newItem: com.tv.settings.intances.Setting<*>): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: com.tv.settings.intances.Setting<*>, newItem: com.tv.settings.intances.Setting<*>): Boolean {
            return oldItem == newItem
        }
    }
}