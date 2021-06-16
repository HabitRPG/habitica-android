package com.habitrpg.android.habitica.ui.views.stats

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.DialogBulkAllocateBinding
import com.habitrpg.android.habitica.extensions.getThemeColor
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import io.reactivex.rxjava3.disposables.Disposable
import javax.inject.Inject

class BulkAllocateStatsDialog(context: Context, component: UserComponent?) : AlertDialog(context) {
    private val binding = DialogBulkAllocateBinding.inflate(context.layoutInflater)
    @Inject
    lateinit var userRepository: UserRepository

    var subscription: Disposable? = null

    private val allocatedPoints: Int
        get() {
        var value = 0
        value += binding.strengthSliderView.currentValue
        value += binding.intelligenceSliderView.currentValue
        value += binding.constitutionSliderView.currentValue
        value += binding.perceptionSliderView.currentValue
        return value
    }

    private var pointsToAllocate = 0
    set(value) {
        field = value
        updateTitle()
        binding.strengthSliderView.maxValue = pointsToAllocate
        binding.intelligenceSliderView.maxValue = pointsToAllocate
        binding.constitutionSliderView.maxValue = pointsToAllocate
        binding.perceptionSliderView.maxValue = pointsToAllocate
    }

    init {
        component?.inject(this)

        setView(binding.root)
        this.setButton(BUTTON_POSITIVE, context.getString(R.string.save)) { _, _ ->
            saveChanges()
        }
        this.setButton(BUTTON_NEUTRAL, context.getString(R.string.action_cancel)) { _, _ ->
            this.dismiss()
        }
    }

    private fun saveChanges() {
        getButton(BUTTON_POSITIVE).isEnabled = false
        userRepository.bulkAllocatePoints(binding.strengthSliderView.currentValue,
                binding.intelligenceSliderView.currentValue,
                binding.constitutionSliderView.currentValue,
                binding.perceptionSliderView.currentValue)
                .subscribe({
                    this.dismiss()
                }, {
                    RxErrorHandler.reportError(it)
                    this.dismiss()
                })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscription = userRepository.getUser().subscribe({
            pointsToAllocate = it.stats?.points ?: 0
            binding.strengthSliderView.previousValue = it.stats?.strength ?: 0
            binding.intelligenceSliderView.previousValue = it.stats?.intelligence ?: 0
            binding.constitutionSliderView.previousValue = it.stats?.constitution ?: 0
            binding.perceptionSliderView.previousValue = it.stats?.per ?: 0
        }, RxErrorHandler.handleEmptyError())

        binding.strengthSliderView.allocateAction = {
            checkRedistribution(binding.strengthSliderView)
            updateTitle()
        }
        binding.intelligenceSliderView.allocateAction = {
            checkRedistribution(binding.intelligenceSliderView)
            updateTitle()
        }
        binding.constitutionSliderView.allocateAction = {
            checkRedistribution(binding.constitutionSliderView)
            updateTitle()
        }
        binding.perceptionSliderView.allocateAction = {
            checkRedistribution(binding.perceptionSliderView)
            updateTitle()
        }
    }

    private fun checkRedistribution(excludedSlider: StatsSliderView) {
        val diff = allocatedPoints - pointsToAllocate
        if (diff > 0) {
            var highestSlider: StatsSliderView? = null
            if (excludedSlider != binding.strengthSliderView) {
                highestSlider = getSliderWithHigherValue(highestSlider, binding.strengthSliderView)
            }
            if (excludedSlider != binding.intelligenceSliderView) {
                highestSlider = getSliderWithHigherValue(highestSlider, binding.intelligenceSliderView)
            }
            if (excludedSlider != binding.constitutionSliderView) {
                highestSlider = getSliderWithHigherValue(highestSlider, binding.constitutionSliderView)
            }
            if (excludedSlider != binding.perceptionSliderView) {
                highestSlider = getSliderWithHigherValue(highestSlider, binding.perceptionSliderView)
            }
            if (highestSlider != null) {
                highestSlider.currentValue -= diff
            }
        }
    }

    private fun getSliderWithHigherValue(firstSlider: StatsSliderView?, secondSlider: StatsSliderView?): StatsSliderView? {
        return if (firstSlider?.currentValue ?: 0 > secondSlider?.currentValue ?: 0) {
            firstSlider
        } else {
            secondSlider
        }
    }

    override fun dismiss() {
        subscription?.dispose()
        super.dismiss()
    }

    @SuppressLint("SetTextI18n")
    private fun updateTitle() {
        binding.allocatedTitle.text = "$allocatedPoints/$pointsToAllocate"
        if (allocatedPoints > 0) {
            binding.titleView.setBackgroundColor(context.getThemeColor(R.attr.colorAccent))
        } else {
            binding.titleView.setBackgroundColor(ContextCompat.getColor(context, R.color.disabled_background))
        }

        getButton(BUTTON_POSITIVE).isEnabled = allocatedPoints > 0
    }
}