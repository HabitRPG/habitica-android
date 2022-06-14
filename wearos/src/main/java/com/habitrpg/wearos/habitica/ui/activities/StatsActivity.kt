package com.habitrpg.wearos.habitica.ui.activities

import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.activity.viewModels
import com.habitrpg.android.habitica.databinding.ActivityStatsBinding
import com.habitrpg.wearos.habitica.ui.viewmodels.StatsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatsActivity : BaseActivity<ActivityStatsBinding, StatsViewModel>() {
    override val viewModel: StatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityStatsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        val vto: ViewTreeObserver = binding.progress.viewTreeObserver;
        vto.addOnGlobalLayoutListener {
            val height: Int = binding.root.measuredHeight
            binding.progress.setCircularProgressView((height / 2) - 10)
            binding.progress.animateProgress()
        }
    }

    override fun onStart() {
        super.onStart()
//        val width: Int = binding.root.measuredWidth
//        val height: Int = binding.root.measuredHeight
        binding.progress.animateProgress()

    }

}


