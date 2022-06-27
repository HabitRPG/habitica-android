package com.habitrpg.wearos.habitica.ui.activities

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View.*
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ActivityStatsBinding
import com.habitrpg.common.habitica.views.HabiticaIconsHelper
import com.habitrpg.wearos.habitica.models.user.Stats
import com.habitrpg.wearos.habitica.models.user.User
import com.habitrpg.wearos.habitica.ui.viewmodels.StatsViewModel
import com.habitrpg.wearos.habitica.ui.views.StatValue
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class StatsActivity : BaseActivity<ActivityStatsBinding, StatsViewModel>() {
    override val viewModel: StatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityStatsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setViews()
        viewModel.user.observe(this) {
            loadViews(it)
        }
    }

    private fun setViews() {
        binding.statsImageview.setColorFilter(ContextCompat.getColor(this, R.color.watch_purple_200))
        binding.statsImageview.visibility = VISIBLE
        loadingManager.startLoading()
        setBarViews()
        setStatViews()
    }

    private fun loadViews(user: User) {
        binding.statsImageview.visibility = GONE
        loadingManager.endLoading()
        updateStats(user)
    }

    private fun setBarViews() {
        binding.hpBar.setPercentageValues(1f, 100f)
        binding.hpBar.animateProgress()

        binding.expBar.setPercentageValues(1f, 100f)
        binding.expBar.animateProgress()

        binding.mpBar.setPercentageValues(1f, 100f)
        binding.mpBar.animateProgress()

    }

    private fun setStatViews() {
        binding.hpStatValue.visibility = INVISIBLE
        binding.expStatValue.visibility = INVISIBLE
        binding.mpStatValue.visibility = INVISIBLE

        binding.hpStatValue.setStatValueResources(HabiticaIconsHelper.imageOfHeartLarge(), R.color.hp_bar_color)
        binding.expStatValue.setStatValueResources(HabiticaIconsHelper.imageOfExperience(), R.color.exp_bar_color)
        binding.mpStatValue.setStatValueResources(HabiticaIconsHelper.imageOfMagic(), R.color.mpColor)
    }

    private fun updateStats(user: User) {
        val stats = user.stats
        stats?.let { updateBarViews(it) }
        stats?.let { updateStatViews(it) }
    }

    private fun updateBarViews(stats: Stats) {
        binding.hpBar.visibility = VISIBLE
        binding.expBar.visibility = VISIBLE
        binding.mpBar.visibility = VISIBLE

        binding.hpBar.setPercentageValues(stats.hp?.toFloat() ?: 0f, stats.maxHealth?.toFloat() ?: 0f)
        binding.hpBar.animateProgress()

        binding.expBar.setPercentageValues(stats.exp?.toFloat() ?: 0f, stats.toNextLevel?.toFloat() ?: 0f)
        binding.expBar.animateProgress()

        if ((stats.lvl ?: 0) < 10) {
            binding.mpBar.visibility = GONE
        } else {
            binding.mpBar.setPercentageValues(stats.mp?.toFloat() ?: 0f, stats.maxMP?.toFloat() ?: 0f)
            binding.mpBar.animateProgress()
        }
    }

    private fun updateStatViews(stats: Stats) {
        binding.hpStatValue.visibility = VISIBLE
        binding.expStatValue.visibility = VISIBLE
        binding.mpStatValue.visibility = VISIBLE

        binding.hpStatValue.setStatValues(stats.maxHealth ?: 0, stats.hp?.toInt() ?: 0)
        binding.expStatValue.setStatValues(stats.toNextLevel ?: 0, stats.exp?.toInt() ?: 0)
        if ((stats.lvl ?: 0) < 10) {
            binding.mpStatValue.visibility = GONE
        } else {
            binding.mpStatValue.setStatValues(stats.maxMP ?: 0, stats.mp?.toInt() ?: 0)
        }
    }

}


