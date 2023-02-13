package com.habitrpg.wearos.habitica.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.databinding.ActivityConfirmationBinding
import com.habitrpg.wearos.habitica.ui.viewmodels.ConfirmactionActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@AndroidEntryPoint
class ConfirmationActivity :
    BaseActivity<ActivityConfirmationBinding, ConfirmactionActivityViewModel>() {
    override val viewModel: ConfirmactionActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityConfirmationBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        binding.root.setOnClickListener {
            finish()
        }

        lifecycleScope.launch {
            delay(4.toDuration(DurationUnit.SECONDS))
            finish()
        }

        binding.textView.setCompoundDrawablesWithIntrinsicBounds(0, viewModel.icon, 0, 0)
        binding.textView.text = viewModel.text
    }
}
