package com.habitrpg.android.habitica.ui.views.stats

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.dialog_bulk_allocate.*
import javax.inject.Inject

class BulkAllocateStatsDialog(context: Context, component: AppComponent?) : AlertDialog(context) {

    @Inject
    lateinit var userRepository: UserRepository

    var subscription: Disposable? = null

    private var allocatedPoints: Int
        get() {
        var value = 0
        value += strengthSliderView.currentValue
        value += intelligenceSliderView.currentValue
        value += constitutionSliderView.currentValue
        value += perceptionSliderView.currentValue
        return value
    }
    set(value) {
        return
    }

    private var pointsToAllocate = 0
    set(value) {
        field = value
        updateTitle()
        strengthSliderView.maxValue = pointsToAllocate
        intelligenceSliderView.maxValue = pointsToAllocate
        constitutionSliderView.maxValue = pointsToAllocate
        perceptionSliderView.maxValue = pointsToAllocate
    }

    private var user: User? = null
    set(value) {
        field = value
        pointsToAllocate = user?.stats?.points ?: 0
        strengthSliderView.previousValue = user?.stats?.str ?: 0
        intelligenceSliderView.previousValue = user?.stats?._int ?: 0
        constitutionSliderView.previousValue = user?.stats?.con ?: 0
        perceptionSliderView.previousValue = user?.stats?.per ?: 0
    }

    init {
        component?.inject(this)

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_bulk_allocate, null)

        setView(view)
        this.setButton(BUTTON_POSITIVE, context.getString(R.string.save)) { _, _ ->
            saveChanges()
        }
        this.setButton(BUTTON_NEUTRAL, context.getString(R.string.action_cancel)) { _, _ ->
            this.dismiss()
        }
    }

    private fun saveChanges() {
        @Suppress("DEPRECATION")
        val progressDialog = ProgressDialog.show(context, context.getString(R.string.allocating_points), null, true)
        userRepository.bulkAllocatePoints(user, strengthSliderView.currentValue, intelligenceSliderView.currentValue, constitutionSliderView.currentValue, perceptionSliderView.currentValue)
                .subscribe({
                    progressDialog.dismiss()
                    this.dismiss()
                }, {
                    RxErrorHandler.reportError(it)
                    progressDialog.dismiss()
                    this.dismiss()
                })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscription = userRepository.getUser().subscribe(Consumer {
            user = it
        }, RxErrorHandler.handleEmptyError())

        strengthSliderView.allocateAction = {
            checkRedistribution(strengthSliderView)
            updateTitle()
        }
        intelligenceSliderView.allocateAction = {
            checkRedistribution(intelligenceSliderView)
            updateTitle()
        }
        constitutionSliderView.allocateAction = {
            checkRedistribution(constitutionSliderView)
            updateTitle()
        }
        perceptionSliderView.allocateAction = {
            checkRedistribution(perceptionSliderView)
            updateTitle()
        }
    }

    private fun checkRedistribution(excludedSlider: StatsSliderView) {
        val diff = allocatedPoints - pointsToAllocate
        if (diff > 0) {
            var highestSlider: StatsSliderView? = null
            if (excludedSlider != strengthSliderView) {
                highestSlider = getSliderWithHigherValue(highestSlider, strengthSliderView)
            }
            if (excludedSlider != intelligenceSliderView) {
                highestSlider = getSliderWithHigherValue(highestSlider, intelligenceSliderView)
            }
            if (excludedSlider != constitutionSliderView) {
                highestSlider = getSliderWithHigherValue(highestSlider, constitutionSliderView)
            }
            if (excludedSlider != perceptionSliderView) {
                highestSlider = getSliderWithHigherValue(highestSlider, perceptionSliderView)
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

    private fun updateTitle() {
        allocatedTitle.text = "$allocatedPoints/$pointsToAllocate"
        if (allocatedPoints > 0) {
            titleView.setBackgroundColor(ContextCompat.getColor(context, R.color.brand_400))
        } else {
            titleView.setBackgroundColor(ContextCompat.getColor(context, R.color.gray_400))
        }

        getButton(BUTTON_POSITIVE).isEnabled = allocatedPoints > 0
    }
}