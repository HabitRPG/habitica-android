package com.habitrpg.android.habitica.ui.fragments

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.Unbinder
import com.habitrpg.android.habitica.HabiticaApplication
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.TutorialRepository
import com.habitrpg.android.habitica.events.DisplayTutorialEvent
import com.habitrpg.android.habitica.helpers.AmplitudeManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.EventBusException
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.subscriptions.CompositeSubscription
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class BaseFragment : DialogFragment() {

    @Inject
    lateinit var tutorialRepository: TutorialRepository

    var tutorialStepIdentifier: String? = null
    var tutorialText: String? = null
    var unbinder: Unbinder? = null
    protected var tutorialCanBeDeferred = true
    var tutorialTexts: MutableList<String> = ArrayList()

    protected var compositeSubscription: CompositeSubscription = CompositeSubscription()

    open val displayedClassName: String?
        get() = this.javaClass.simpleName

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        showTutorialIfNeeded()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        injectFragment(HabiticaBaseApplication.getComponent())
        this.showsDialog = false
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        compositeSubscription = CompositeSubscription()

        // Receive Events
        try {
            EventBus.getDefault().register(this)
        } catch (ignored: EventBusException) {

        }

        return null
    }

    abstract fun injectFragment(component: AppComponent)

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (unbinder == null) {
            unbinder = ButterKnife.bind(this, view)
        }
    }

    override fun onResume() {
        super.onResume()
        showTutorialIfNeeded()
    }

    private fun showTutorialIfNeeded() {
        if (userVisibleHint && view != null) {
            if (this.tutorialStepIdentifier != null) {
                tutorialRepository.getTutorialStep(this.tutorialStepIdentifier!!).first()
                        .delay(1, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(Action1 { step ->
                            if (step != null && step.isValid && step.isManaged && step.shouldDisplay()) {
                                val event = DisplayTutorialEvent()
                                event.step = step
                                if (tutorialText != null) {
                                    event.tutorialText = tutorialText
                                } else {
                                    event.tutorialTexts = tutorialTexts
                                }
                                event.canBeDeferred = tutorialCanBeDeferred
                                EventBus.getDefault().post(event)
                            }
                        }, RxErrorHandler.handleEmptyError())
            }

            val displayedClassName = this.displayedClassName

            if (displayedClassName != null) {
                val additionalData = HashMap<String, Any>()
                additionalData.put("page", displayedClassName)
                AmplitudeManager.sendEvent("navigate", AmplitudeManager.EVENT_CATEGORY_NAVIGATION, AmplitudeManager.EVENT_HITTYPE_PAGEVIEW, additionalData)
            }
        }
    }

    override fun onDestroyView() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        if (unbinder != null) {
            unbinder!!.unbind()
            unbinder = null
        }
        if (!compositeSubscription.isUnsubscribed) {
            compositeSubscription.unsubscribe()
        }

        super.onDestroyView()
        val refWatcher = HabiticaApplication.getInstance(context).refWatcher
        refWatcher.watch(this)
    }

    override fun onDestroy() {
        tutorialRepository.close()
        super.onDestroy()
    }

    open fun addToBackStack(): Boolean = true
}
