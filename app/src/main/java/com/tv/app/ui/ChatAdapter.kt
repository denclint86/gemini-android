package com.tv.app.ui

import android.graphics.Color
import androidx.recyclerview.widget.DiffUtil
import com.tv.app.chat.mvi.bean.ChatMessage
import com.tv.app.databinding.ItemChatBinding
import com.tv.app.gemini.Role
import com.zephyr.vbclass.ui.ViewBindingListAdapter

class ChatAdapter : ViewBindingListAdapter<ItemChatBinding, ChatMessage>(Callback()) {

    override fun ItemChatBinding.onBindViewHolder(data: ChatMessage?, position: Int) {
        if (data == null) return

        val color = when (data.role) {
            Role.USER -> Color.GREEN
            Role.MODEL -> Color.YELLOW
            Role.SYSTEM -> Color.RED
            Role.FUNC -> Color.BLUE
        }

        root.setBackgroundColor(color)

        tv.text = data.text
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