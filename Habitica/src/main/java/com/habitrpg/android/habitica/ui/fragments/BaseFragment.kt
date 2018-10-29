package com.habitrpg.android.habitica.ui.fragments

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.TutorialRepository
import com.habitrpg.android.habitica.events.DisplayTutorialEvent
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.EventBusException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class BaseFragment : DialogFragment() {

    @Inject
    lateinit var tutorialRepository: TutorialRepository

    var tutorialStepIdentifier: String? = null
    var tutorialText: String? = null
    protected var tutorialCanBeDeferred = true
    var tutorialTexts: MutableList<String> = ArrayList()

    protected var compositeSubscription: CompositeDisposable = CompositeDisposable()

    open val displayedClassName: String?
        get() = this.javaClass.simpleName

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        showTutorialIfNeeded()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        HabiticaBaseApplication.component.notNull {
            injectFragment(it)
        }
        this.showsDialog = false
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        compositeSubscription = CompositeDisposable()

        // Receive Events
        try {
            EventBus.getDefault().register(this)
        } catch (ignored: EventBusException) {

        }

        return null
    }

    abstract fun injectFragment(component: AppComponent)

    override fun onResume() {
        super.onResume()
        showTutorialIfNeeded()
    }

    private fun showTutorialIfNeeded() {
        if (userVisibleHint && view != null) {
            if (this.tutorialStepIdentifier != null) {
                compositeSubscription.add(tutorialRepository.getTutorialStep(this.tutorialStepIdentifier ?: "").firstElement()
                        .delay(1, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(Consumer { step ->
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
                        }, RxErrorHandler.handleEmptyError()))
            }
        }
    }

    override fun onDestroyView() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        if (!compositeSubscription.isDisposed) {
            compositeSubscription.dispose()
        }

        super.onDestroyView()
        context.notNull {
            val refWatcher = HabiticaBaseApplication.getInstance(it).refWatcher
            refWatcher?.watch(this)
        }
    }

    override fun onDestroy() {
        try {
            tutorialRepository.close()
        } catch (exception: UninitializedPropertyAccessException) {
        }
        super.onDestroy()
    }

    open fun addToBackStack(): Boolean = true
}
