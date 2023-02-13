package com.habitrpg.wearos.habitica.ui.activities

import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.TranslateAnimation
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.databinding.ActivityContinuePhoneBinding
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.wearos.habitica.ui.viewmodels.ContinuePhoneViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@AndroidEntryPoint
class ContinuePhoneActivity : BaseActivity<ActivityContinuePhoneBinding, ContinuePhoneViewModel>() {
    override val viewModel: ContinuePhoneViewModel by viewModels()

    private var secondsToShow = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityContinuePhoneBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        messageClient.addListener(viewModel)

        binding.root.setOnClickListener {
            finish()
        }

        viewModel.onActionCompleted = {
            finish()
        }

        if (!viewModel.keepActive) {
            lifecycleScope.launch {
                delay(secondsToShow.toDuration(DurationUnit.SECONDS))
                finish()
            }
        }

        val alphaAnimation = AlphaAnimation(0f, 1f)
        val translateAnimation = TranslateAnimation((-20f).dpToPx(this), 0f, 0f, 0f)
        val set = AnimationSet(true)
        set.interpolator = AnticipateOvershootInterpolator()
        set.duration = 600
        set.startOffset = 100
        set.fillBefore = true
        set.fillAfter = true
        set.addAnimation(alphaAnimation)
        set.addAnimation(translateAnimation)
        binding.iconView.startAnimation(set)
    }

    override fun onDestroy() {
        messageClient.removeListener(viewModel)
        super.onDestroy()
    }
}
