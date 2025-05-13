package com.tv.app.view.ui

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.tv.app.databinding.LayoutChatButtonBinding
import com.tv.app.utils.keyborad.IKeyboardUtil
import com.tv.app.utils.safeCollapse
import com.tv.app.utils.safeExpand
import com.zephyr.scaling_layout.ScalingLayout
import com.zephyr.scaling_layout.ScalingLayoutListener
import com.zephyr.scaling_layout.State

class ChatButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    companion object {
        private const val EXPAND_ANIM_TIME = 50L
        private const val COLLAPSE_ANIM_TIME = 100L
    }

    private val binding: LayoutChatButtonBinding =
        LayoutChatButtonBinding.inflate(LayoutInflater.from(context), this, true)

    val frameLayout: FrameLayout
        get() = binding.root as FrameLayout
    val editText: EditText
        get() = binding.editText
    val imageSend: ImageView
        get() = binding.imageSend
    val scalingLayout: ScalingLayout
        get() = binding.scalingLayout
    val state: State
        get() = scalingLayout.state

    private var onNewStateListener: ((State) -> Unit)? = null
    private var onSubmitListener: ((String) -> Boolean)? = null

    private var iFocusHandler: IKeyboardUtil.IFocusHandler? = null

    init {
        setScalingLayout()
        setOnClick()
        setOnSubmit()

        binding.layoutActions.visibility = View.INVISIBLE
        binding.tvTint.visibility = View.VISIBLE
    }

    fun setIFocusHandler(i: IKeyboardUtil.IFocusHandler?) {
        iFocusHandler = i
    }

    fun expand(): Boolean {
        val r = scalingLayout.safeExpand()
        if (r)
            iFocusHandler?.focus()
        return r
    }

    fun collapse(): Boolean {
        val r = scalingLayout.safeCollapse()
        if (r)
            iFocusHandler?.loseFocus()
        return r
    }

    fun setText(str: String) {
        binding.tvTint.text = str
    }

    fun setOnNewStateListener(l: ((State) -> Unit)?) {
        onNewStateListener = l
    }

    fun setOnSubmitListener(l: ((String) -> Boolean)?) {
        onSubmitListener = l
    }

    private fun setOnClick() {
        scalingLayout.setOnClickListener {
            expand()
        }
    }

    private fun setOnSubmit() {
        binding.imageSend.setOnClickListener {
            val str = editText.text.toString()
            val result = onSubmitListener?.invoke(str)
            if (result == true) // 被接受
                editText.setText("")
        }
    }

    private fun setScalingLayout() = binding.run {
        scalingLayout.setListener(object : ScalingLayoutListener {
            override fun onCollapsed() {
                tvTint.animate().alpha(1F).setDuration(COLLAPSE_ANIM_TIME).start()
                layoutActions.animate().alpha(0F).setDuration(COLLAPSE_ANIM_TIME)
                    .setListener(object : AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                            tvTint.visibility = View.VISIBLE
                        }

                        override fun onAnimationEnd(animation: Animator) {
                            layoutActions.visibility = View.INVISIBLE
                        }

                        override fun onAnimationCancel(animation: Animator) {}

                        override fun onAnimationRepeat(animation: Animator) {}
                    }).start()

                onNewStateListener?.invoke(State.COLLAPSED)
            }

            override fun onExpanded() {
                tvTint.animate().alpha(0F).setDuration(EXPAND_ANIM_TIME).start()
                layoutActions.animate().alpha(1F).setDuration(EXPAND_ANIM_TIME)
                    .setListener(object : AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                            layoutActions.visibility = View.VISIBLE
                        }

                        override fun onAnimationEnd(animation: Animator) {
                            tvTint.visibility = View.INVISIBLE
                        }

                        override fun onAnimationCancel(animation: Animator) {}

                        override fun onAnimationRepeat(animation: Animator) {}
                    }).start()

                onNewStateListener?.invoke(State.EXPANDED)
            }

            override fun onProgress(progress: Float) {
                onNewStateListener?.invoke(State.PROGRESSING)
            }
        })
    }
}