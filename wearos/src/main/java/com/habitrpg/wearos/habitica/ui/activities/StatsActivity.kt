package com.habitrpg.wearos.habitica.ui.activities

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.viewModels
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ActivityStatsBinding
import com.habitrpg.android.habitica.extensions.observeOnce
import com.habitrpg.common.habitica.views.HabiticaIconsHelper
import com.habitrpg.wearos.habitica.extensions.waitForLayout
import com.habitrpg.wearos.habitica.models.Stats
import com.habitrpg.wearos.habitica.models.User
import com.habitrpg.wearos.habitica.ui.viewmodels.StatsViewModel
import com.habitrpg.wearos.habitica.ui.views.StatValue
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatsActivity : BaseActivity<ActivityStatsBinding, StatsViewModel>() {
    override val viewModel: StatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityStatsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        viewModel.user.observe(this) {
            updateStats(it)
        }
    }

    private fun updateStats(user: User) {
        binding.root.waitForLayout {
            val height: Int = binding.root.measuredHeight
            val stats = user.stats
            stats?.let { updateBarViews(it, height) }
            stats?.let { updateStatViews(it) }
        }
    }

    private fun updateBarViews(stats: Stats, height: Int) {
        binding.hpBar.ovalSize = ((height / 2) - 10)
        binding.hpBar.setBarColor(R.color.hp_bar_color)
        binding.hpBar.setPercentageValues(stats.hp?.toInt() ?: 0, stats.maxHealth ?: 0)
        binding.hpBar.animateProgress()

        binding.expBar.ovalSize = ((height / 2) - 28)
        binding.expBar.setBarColor(R.color.exp_bar_color)
        binding.expBar.setPercentageValues(stats.exp?.toInt() ?: 0, stats.toNextLevel ?: 0)
        binding.expBar.animateProgress()

        if (stats.lvl ?: 0 < 10) {
            binding.mpBar.visibility = View.GONE
        } else {
            binding.mpBar.ovalSize = ((height / 2) - 46)
            binding.mpBar.setBarColor(R.color.mpColor)
            binding.mpBar.setPercentageValues(stats.mp?.toInt() ?: 0, stats.maxMP ?: 0)
            binding.mpBar.animateProgress()
        }


    }

    private fun updateStatViews(stats: Stats) {
        binding.hpStatValue.setStatValue(stats.maxHealth ?: 0, stats.hp?.toInt() ?: 0, HabiticaIconsHelper.imageOfHeartLightBg(), R.color.hp_bar_color)
        binding.expStatValue.setStatValue(stats.toNextLevel ?: 0, stats.exp?.toInt() ?: 0, HabiticaIconsHelper.imageOfExperience(), R.color.exp_bar_color)
        if (stats.lvl ?: 0 < 10) {
            binding.mpStatValue.visibility = View.GONE
        } else {
            binding.mpStatValue.setStatValue(stats.maxMP ?: 0, stats.mp?.toInt() ?: 0, HabiticaIconsHelper.imageOfMagic(), R.color.mpColor)
        }
    }
}


