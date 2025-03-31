package com.tv.app.ui

import android.graphics.Color
import androidx.recyclerview.widget.DiffUtil
import com.tv.app.chat.Role
import com.tv.app.chat.mvi.bean.ChatMessage
import com.tv.app.databinding.ItemChatBinding
import com.zephyr.extension.screen.getStatusBarHeight
import com.zephyr.extension.widget.setMargins
import com.zephyr.vbclass.ui.ViewBindingListAdapter

class ChatAdapter : ViewBindingListAdapter<ItemChatBinding, ChatMessage>(Callback()) {
    companion object {
        private const val HIDE_ENABLED = true
    }


    override fun ItemChatBinding.onBindViewHolder(data: ChatMessage?, position: Int) {
        if (data == null) return

        val marginTop = if (position == 0)
            root.context.getStatusBarHeight()
        else
            0

        root.setMargins(top = marginTop)

        val color = when (data.role) {
            Role.USER -> "#E7FBE6"
            Role.MODEL -> "#EECAD5"
            Role.SYSTEM -> "#FF8A8A"
            Role.FUNC -> "#D1E9F6"
        }

        cv.setCardBackgroundColor(Color.parseColor(color))

        tv.text =
            if (HIDE_ENABLED && data.text.length > 200 && (data.role == Role.SYSTEM || data.role == Role.FUNC))
                data.text.take(200) + "......"
            else
                data.text
    }

    class Callback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}