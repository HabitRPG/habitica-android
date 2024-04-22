package com.habitrpg.android.habitica.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.habitrpg.android.habitica.data.TutorialRepository
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.common.habitica.helpers.launchCatching
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import kotlin.time.DurationUnit
import kotlin.time.toDuration

abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    var isModal: Boolean = false
    abstract var binding: VB?

    @Inject
    lateinit var tutorialRepository: TutorialRepository

    var tutorialStepIdentifier: String? = null
    protected var tutorialCanBeDeferred = true
    var tutorialTexts: List<String> = ArrayList()

    var shouldInitializeComponent = true

    open val displayedClassName: String?
        get() = this.javaClass.simpleName

    abstract fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = createBinding(inflater, container)
        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        showTutorialIfNeeded()
    }

    private fun showTutorialIfNeeded() {
        tutorialStepIdentifier?.let { identifier ->
            lifecycleScope.launchCatching {
                val step = tutorialRepository.getTutorialStep(identifier).firstOrNull()
                delay(1.toDuration(DurationUnit.SECONDS))
                if (step?.isValid == true && step.isManaged && step.shouldDisplay) {
                    val mainActivity = activity as? MainActivity ?: return@launchCatching
                    mainActivity.displayTutorialStep(
                        step,
                        tutorialTexts,
                        tutorialCanBeDeferred,
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        try {
            tutorialRepository.close()
        } catch (exception: UninitializedPropertyAccessException) {
            // no-on
        }
        super.onDestroy()
    }

    open fun addToBackStack(): Boolean = true
}
