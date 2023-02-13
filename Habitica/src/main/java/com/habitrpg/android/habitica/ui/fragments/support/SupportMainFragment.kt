package com.habitrpg.android.habitica.ui.fragments.support

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.FAQRepository
import com.habitrpg.android.habitica.databinding.FragmentSupportMainBinding
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class SupportMainFragment : BaseMainFragment<FragmentSupportMainBinding>() {
    override var binding: FragmentSupportMainBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSupportMainBinding {
        return FragmentSupportMainBinding.inflate(inflater, container, false)
    }

    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String
    @Inject
    lateinit var faqRepository: FAQRepository
    @Inject
    lateinit var appConfigManager: AppConfigManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        hidesToolbar = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.usingHabiticaWrapper?.setOnClickListener {
            MainNavigationController.navigate(R.id.FAQOverviewFragment)
        }
        binding?.bugsFixesWrapper?.setOnClickListener {
            MainNavigationController.navigate(R.id.bugFixFragment)
        }
        binding?.suggestionsFeedbackWrapper?.setOnClickListener {
            if (appConfigManager.feedbackURL().isNotBlank()) {
                val uriUrl = appConfigManager.feedbackURL().toUri()
                val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
                startActivity(launchBrowser)
            }
        }

        binding?.resetTutorialButton?.setOnClickListener {
            lifecycleScope.launch(ExceptionHandler.coroutine()) {
                userRepository.resetTutorial()
                activity?.showSnackbar(null, null, getString(R.string.tutorial_reset_confirmation), displayType = HabiticaSnackbar.SnackbarDisplayType.SUCCESS)
            }
        }
    }

    override fun onDestroy() {
        faqRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }
}
