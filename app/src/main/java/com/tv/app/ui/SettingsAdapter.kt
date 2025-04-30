package com.tv.app.ui

import androidx.recyclerview.widget.DiffUtil
import com.tv.app.databinding.LayoutSettingItemBinding
import com.zephyr.vbclass.ui.ViewBindingListAdapter

abstract class Setting<T : Any> {
    abstract val name: String
    abstract val preview: T

    abstract fun onValidate(v: Any): Boolean

    fun clone(
        newName: String? = null,
        newValue: T? = null
    ): Setting<T> {
        return object : Setting<T>() {
            override val name: String = newName ?: this@Setting.name
            override val preview: T = newValue ?: this@Setting.preview
            override fun onValidate(v: Any): Boolean = this@Setting.onValidate(v)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other is Setting<*>)
            return preview == other.preview
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + preview.hashCode()
        return result
    }
}

class SettingsAdapter(val onClick: (Setting<*>) -> Unit) :
    ViewBindingListAdapter<LayoutSettingItemBinding, Setting<*>>(Callback()) {

    override fun LayoutSettingItemBinding.onBindViewHolder(data: Setting<*>?, position: Int) {
        if (data == null) return
        title.text = data.name
        preview.text = data.preview.toString()

        root.setOnClickListener {
            onClick(data)
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