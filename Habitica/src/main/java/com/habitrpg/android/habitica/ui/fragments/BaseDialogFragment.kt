package com.habitrpg.android.habitica.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.TutorialRepository
import com.habitrpg.android.habitica.helpers.AmplitudeManager
import com.habitrpg.android.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.ui.activities.MainActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.functions.Consumer
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class BaseDialogFragment<VB : ViewBinding> : BottomSheetDialogFragment() {

    var isModal: Boolean = false
    abstract var binding: VB?

    @Inject
    lateinit var tutorialRepository: TutorialRepository

    var tutorialStepIdentifier: String? = null
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        compositeSubscription = CompositeDisposable()

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
                                if (step.isValid && step.isManaged && step.shouldDisplay) {
                                    val mainActivity = activity as? MainActivity ?: return@Consumer
                                    mainActivity.displayTutorialStep(step, tutorialTexts, tutorialCanBeDeferred)
                                }
                            },
                            ExceptionHandler.rx()
                        )
                )
            }
        }
    }

    override fun onDestroyView() {
        binding = null
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
