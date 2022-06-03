package com.habitrpg.wearos.habitica.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.viewbinding.ViewBinding

open class BaseActivity<B: ViewBinding> : ComponentActivity() {
    protected lateinit var binding: B

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}