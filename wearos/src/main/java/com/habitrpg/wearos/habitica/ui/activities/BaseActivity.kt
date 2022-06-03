package com.habitrpg.wearos.habitica.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.viewbinding.ViewBinding
import androidx.wear.activity.ConfirmationActivity
import com.habitrpg.wearos.habitica.ui.viewmodels.BaseViewModel

abstract class BaseActivity<B: ViewBinding, VM: BaseViewModel> : ComponentActivity() {
    protected lateinit var binding: B
    abstract val viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewModel.errorValues.observe(this) {
            val intent = Intent(this, ConfirmationActivity::class.java).apply {
                putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION)
                putExtra(ConfirmationActivity.EXTRA_MESSAGE, it.title)
                putExtra(ConfirmationActivity.EXTRA_ANIMATION_DURATION_MILLIS, 3000)
            }
            startActivity(intent)
        }
    }
}