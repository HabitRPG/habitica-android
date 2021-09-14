package com.habitrpg.android.habitica.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.TutorialRepository
import com.habitrpg.android.habitica.helpers.AmplitudeManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.ui.activities.MainActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.functions.Consumer
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.EventBusException
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class BaseDialogFragment<VB : ViewBinding> : DialogFragment() {

    var isModal: Boolean = false
    abstract var binding: VB?

    @Inject
    lateinit var tutorialRepository: TutorialRepository

    var tutorialStepIdentifier: String? = null
    var tutorialText: String? = null
    protected var tutorialCanBeDeferred = true
    var tutorialTexts: MutableList<String> = ArrayList()

    protected var compositeSubscription: CompositeDisposable = CompositeDisposable()

    open val displayedClassName: String?
        get() = this.javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        HabiticaBaseApplication.userComponent?.let {
            injectFragment(it)
        }
        super.onCreate(savedInstanceState)
    }

    abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        compositeSubscription = CompositeDisposable()

        // Receive Events
        try {
            EventBus.getDefault().register(this)
        } catch (ignored: EventBusException) {
        }

        val additionalData = HashMap<String, Any>()
        additionalData["page"] = this.javaClass.simpleName
        AmplitudeManager.sendEvent("navigate", AmplitudeManager.EVENT_CATEGORY_NAVIGATION, AmplitudeManager.EVENT_HITTYPE_PAGEVIEW, additionalData)

        binding = createBinding(inflater, container)
        return binding?.root
    }

    abstract fun injectFragment(component: UserComponent)

    override fun onResume() {
        super.onResume()
        showTutorialIfNeeded()
    }

    private fun showTutorialIfNeeded() {
        if (view != null) {
            if (this.tutorialStepIdentifier != null) {
                compositeSubscription.add(
                    tutorialRepository.getTutorialStep(this.tutorialStepIdentifier ?: "").firstElement()
                        .delay(1, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            Consumer { step ->
                                if (step != null && step.isValid && step.isManaged && step.shouldDisplay()) {
                                    val mainActivity = activity as? MainActivity ?: return@Consumer
                                    if (tutorialText != null) {
                                        mainActivity.displayTutorialStep(step, tutorialText ?: "", tutorialCanBeDeferred)
                                    } else {
                                        mainActivity.displayTutorialStep(step, tutorialTexts, tutorialCanBeDeferred)
                                    }
                                }
                            },
                            RxErrorHandler.handleEmptyError()
                        )
                )
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        if (!compositeSubscription.isDisposed) {
            compositeSubscription.dispose()
        }

        super.onDestroyView()
    }

    override fun onDestroy() {
        try {
            tutorialRepository.close()
        } catch (exception: UninitializedPropertyAccessException) { /* no-on */ }
        super.onDestroy()
    }

    open fun addToBackStack(): Boolean = true
}
