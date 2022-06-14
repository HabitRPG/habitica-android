package com.habitrpg.wearos.habitica.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.core.view.children
import androidx.viewbinding.ViewBinding
import androidx.wear.activity.ConfirmationActivity
import com.habitrpg.android.habitica.databinding.ActivityWrapperBinding
import com.habitrpg.wearos.habitica.ui.viewmodels.BaseViewModel
import com.habitrpg.wearos.habitica.ui.views.IndeterminateProgressView

abstract class BaseActivity<B: ViewBinding, VM: BaseViewModel> : ComponentActivity() {
    private lateinit var wrapperBinding: ActivityWrapperBinding
    protected lateinit var binding: B
    abstract val viewModel: VM

    private var progressView: IndeterminateProgressView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        wrapperBinding = ActivityWrapperBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        wrapperBinding.root.addView(binding.root)
        setContentView(wrapperBinding.root)

        viewModel.errorValues.observe(this) {
            val intent = Intent(this, ConfirmationActivity::class.java).apply {
                putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION)
                putExtra(ConfirmationActivity.EXTRA_MESSAGE, it.title)
                putExtra(ConfirmationActivity.EXTRA_ANIMATION_DURATION_MILLIS, 3000)
            }
            startActivity(intent)
        }
    }

    fun startAnimatingProgress() {
        if (progressView == null) {
            progressView = IndeterminateProgressView(this)
            wrapperBinding.root.addView(progressView)
            progressView?.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            progressView?.startAnimation()
        }
    }

    fun stopAnimatingProgress() {
        if (progressView != null) {
            wrapperBinding.root.removeView(progressView)
        } else {
            wrapperBinding.root.children.forEach {
                if (it is IndeterminateProgressView) {
                    wrapperBinding.root.removeView(it)
                }
            }
        }
    }
}