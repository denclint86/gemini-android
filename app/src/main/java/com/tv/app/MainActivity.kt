package com.tv.app

import androidx.activity.enableEdgeToEdge
import com.tv.app.databinding.ActivityMainBinding
import com.zephyr.vbclass.ViewBindingActivity

class MainActivity : ViewBindingActivity<ActivityMainBinding>() {
    override fun ActivityMainBinding.initBinding() {
        enableEdgeToEdge()
    }
}