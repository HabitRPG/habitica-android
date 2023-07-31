package com.habitrpg.wearos.habitica.ui.activities

import android.os.Bundle
import android.view.Gravity
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ActivityLevelupBinding
import com.habitrpg.common.habitica.helpers.Animations
import com.habitrpg.wearos.habitica.ui.viewmodels.LevelupViewModel
import com.plattysoft.leonids.ParticleSystem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LevelupActivity : BaseActivity<ActivityLevelupBinding, LevelupViewModel>() {
    override val viewModel: LevelupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLevelupBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        binding.continueButton.setOnClickListener {
            binding.continueButton.isEnabled = false
            startAnimatingProgress()
            lifecycleScope.launch(
                CoroutineExceptionHandler { _, _ ->
                    stopAnimatingProgress()
                    binding.continueButton.isEnabled = true
                }
            ) {
                finish()
            }
        }

        viewModel.level.observe(this) {
            binding.titleView.text = getString(R.string.user_level_long, it)
        }

        binding.iconView.startAnimation(Animations.bobbingAnimation(4f))
        binding.expBar.setPercentageValues(100f, 100f)
        val container = binding.confettiAnchor
        container.postDelayed(
            {
                createParticles(container, R.drawable.confetti_blue)
                createParticles(container, R.drawable.confetti_red)
                createParticles(container, R.drawable.confetti_yellow)
                createParticles(container, R.drawable.confetti_purple)
            },
            500
        )
    }

    private fun createParticles(container: FrameLayout, resource: Int) {
        ParticleSystem(
            container,
            20,
            ContextCompat.getDrawable(this, resource),
            6000
        )
            .setRotationSpeed(144f)
            .setScaleRange(1.0f, 1.6f)
            .setSpeedByComponentsRange(-0.15f, 0.15f, 0.15f, 0.45f)
            .setFadeOut(200, AccelerateInterpolator())
            .emitWithGravity(binding.confettiAnchor, Gravity.TOP, 10, 2000)
    }

    override fun onResume() {
        super.onResume()
        binding.expBar.animateProgress(0f, 2000)
    }
}
