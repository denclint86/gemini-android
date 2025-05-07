package com.tv.app.view.ui

import android.view.View
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import com.tv.app.databinding.LayoutChatItemBinding
import com.tv.app.utils.Role
import com.tv.app.utils.setBackgroundColorFromAttr
import com.tv.app.utils.setTextColorFromAttr
import com.tv.app.viewmodel.chat.mvi.bean.ChatMessage
import com.zephyr.global_values.globalContext
import com.zephyr.vbclass.ui.ViewBindingListAdapter
import io.noties.markwon.Markwon
import io.noties.markwon.PrecomputedTextSetterCompat
import java.util.concurrent.Executors

class ChatAdapter : ViewBindingListAdapter<LayoutChatItemBinding, ChatMessage>(Callback()) {
    companion object {
        private const val HIDE_ENABLED = false
    }

    private val markwon: Markwon by lazy {
        Markwon.builder(globalContext!!)
//            .usePlugin(HtmlPlugin.create { plugin ->
//                plugin.addHandler(
//                    ThinkTagHandler(10)
//                )
//            })
            .textSetter(PrecomputedTextSetterCompat.create(Executors.newCachedThreadPool()))
            .build()
    }

    override fun LayoutChatItemBinding.onBindViewHolder(data: ChatMessage?, position: Int) {
        root.visibility = when {
            currentList.lastIndex < position -> {
                tv.text = "\n\n\n"
                View.INVISIBLE
            }

            (HIDE_ENABLED && position == 0) -> View.GONE
            else -> View.VISIBLE
        }

        if (data == null) return

        val style = roleStyles[data.role] ?: return

        // 缓存当前颜色，检查是否需要更新
        val currentBgColor = tv.backgroundTintList?.defaultColor ?: -1
        val currentTextColor = tv.currentTextColor
        val newBgColor = resolveColorFromAttr(style.backgroundAttr, root.context)
        val newTextColor = resolveColorFromAttr(style.textColorAttr, root.context)

        if (currentBgColor != newBgColor) {
            tv.setBackgroundColorFromAttr(style.backgroundAttr)
        }
        if (currentTextColor != newTextColor) {
            tv.setTextColorFromAttr(style.textColorAttr)
        }

        if (data.role != Role.FUNC) {
            markwon.setMarkdown(tv, data.text)
        } else {
            tv.text = data.text
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 2
    }

    // 提取颜色解析逻辑，避免重复解析
    private fun resolveColorFromAttr(
        @AttrRes attrResId: Int,
        context: android.content.Context
    ): Int {
        val typedValue = android.util.TypedValue()
        context.theme.resolveAttribute(attrResId, typedValue, true)
        return if (typedValue.resourceId != 0) {
            ContextCompat.getColor(context, typedValue.resourceId)
        } else if (typedValue.type >= android.util.TypedValue.TYPE_FIRST_COLOR_INT &&
            typedValue.type <= android.util.TypedValue.TYPE_LAST_COLOR_INT
        ) {
            typedValue.data
        } else {
            0 // 或者抛出异常，视需求而定
        }
    }

    class Callback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.text == newItem.text && oldItem.role == newItem.role
        }
    }
}
