package com.habitrpg.wearos.habitica.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.databinding.ActivityFaintBinding
import com.habitrpg.common.habitica.helpers.Animations
import com.habitrpg.wearos.habitica.ui.viewmodels.FaintViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FaintActivity : BaseActivity<ActivityFaintBinding, FaintViewModel>() {
    override val viewModel: FaintViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityFaintBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        binding.continueButton.setOnClickListener {
            binding.continueButton.isEnabled = false
            startAnimatingProgress()
            lifecycleScope.launch(
                CoroutineExceptionHandler { _, _ ->
                    stopAnimatingProgress()
                    binding.continueButton.isEnabled = true
                },
            ) {
                viewModel.revive()
                finish()
            }
        }

        binding.iconView.startAnimation(Animations.bobbingAnimation(4f))
        binding.hpBar.setPercentageValues(0f, 50f)
    }

    override fun onResume() {
        super.onResume()
        binding.hpBar.animateProgress(50f, 2000)
    }
}
