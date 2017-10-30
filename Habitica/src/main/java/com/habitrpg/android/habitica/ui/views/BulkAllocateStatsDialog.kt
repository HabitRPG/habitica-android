package com.habitrpg.android.habitica.ui.views

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import kotlinx.android.synthetic.main.dialog_bulk_allocate.*
import rx.Subscription
import rx.functions.Action1
import javax.inject.Inject

class BulkAllocateStatsDialog(context: Context?, component: AppComponent) : AlertDialog(context) {

    @Inject
    lateinit var userRepository: UserRepository

    var subscription: Subscription? = null

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
        component.inject(this)

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_bulk_allocate, null)

        setView(view)
        this.setButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE, context?.getString(R.string.done)) { _, _ ->
            this.dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscription = userRepository.user.subscribe(Action1 {
            user = it
        }, RxErrorHandler.handleEmptyError())

        strengthSliderView.allocateAction = {
            updateTitle()
        }
        intelligenceSliderView.allocateAction = {
            updateTitle()
        }
        constitutionSliderView.allocateAction = {
            updateTitle()
        }
        perceptionSliderView.allocateAction = {
            updateTitle()
        }
    }

    override fun dismiss() {
        subscription?.unsubscribe()
        super.dismiss()
    }

    private fun updateTitle() {
        allocatedTitle.text = allocatedPoints.toString() + "/" + pointsToAllocate.toString()
        if (allocatedPoints > 0) {
            titleView.setBackgroundColor(ContextCompat.getColor(context, R.color.brand_400))
        } else {
            titleView.setBackgroundColor(ContextCompat.getColor(context, R.color.gray_400))
        }
    }
}