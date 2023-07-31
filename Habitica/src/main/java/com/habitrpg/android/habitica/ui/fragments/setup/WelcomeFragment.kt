package com.habitrpg.android.habitica.ui.fragments.setup

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentWelcomeBinding
import com.habitrpg.android.habitica.extensions.OnChangeTextWatcher
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WelcomeFragment : BaseFragment<FragmentWelcomeBinding>() {

    var onNameValid: ((Boolean?) -> Unit)? = null

    @Inject
    lateinit var userRepository: UserRepository

    override var binding: FragmentWelcomeBinding? = null

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentWelcomeBinding {
        return FragmentWelcomeBinding.inflate(inflater, container, false)
    }

    private val displayNameVerificationEvents = MutableStateFlow<String?>(null)
    private val usernameVerificationEvents = MutableStateFlow<String?>(null)

    private val checkmarkIcon: Drawable by lazy {
        context?.let {
            BitmapDrawable(
                resources,
                HabiticaIconsHelper.imageOfCheckmark(
                    ContextCompat.getColor(it, R.color.green_50),
                    1f
                )
            )
        } ?: VectorDrawable()
    }
    private val alertIcon: Drawable by lazy {
        BitmapDrawable(resources, HabiticaIconsHelper.imageOfAlertIcon())
    }
    val username: String
        get() = binding?.usernameEditText?.text?.toString() ?: ""
    val displayName: String
        get() = binding?.displayNameEditText?.text?.toString() ?: ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.speechBubble?.animateText(context?.getString(R.string.welcome_text) ?: "")

        super.onCreate(savedInstanceState)

        binding?.displayNameEditText?.addTextChangedListener(
            OnChangeTextWatcher { p0, _, _, _ ->
                displayNameVerificationEvents.value = p0.toString()
            }
        )
        binding?.usernameEditText?.addTextChangedListener(
            OnChangeTextWatcher { p0, _, _, _ ->
                usernameVerificationEvents.value = p0.toString()
            }
        )

        lifecycleScope.launchCatching {
            displayNameVerificationEvents
                .map { it?.length in 1..30 }
                .collect {
                    if (it) {
                        binding?.displayNameEditText?.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            null,
                            checkmarkIcon,
                            null
                        )
                        binding?.issuesTextView?.visibility = View.GONE
                    } else {
                        binding?.displayNameEditText?.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            null,
                            alertIcon,
                            null
                        )
                        binding?.issuesTextView?.visibility = View.VISIBLE
                        binding?.issuesTextView?.text =
                            context?.getString(R.string.display_name_length_error)
                    }
                }
        }
        lifecycleScope.launchCatching {
            usernameVerificationEvents
                .filter { it?.length in 1..30 }
                .filterNotNull()
                .map { userRepository.verifyUsername(it) }
                .collect {
                    if (it?.isUsable == true) {
                        binding?.usernameEditText?.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            null,
                            checkmarkIcon,
                            null
                        )
                        binding?.issuesTextView?.visibility = View.GONE
                    } else {
                        binding?.usernameEditText?.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            null,
                            alertIcon,
                            null
                        )
                        binding?.issuesTextView?.visibility = View.VISIBLE
                        binding?.issuesTextView?.text = it?.issues?.joinToString("\n")
                    }
                    onNameValid?.invoke(it?.isUsable)
                }
        }

        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            val user = userRepository.getUser().firstOrNull()
            binding?.displayNameEditText?.setText(user?.profile?.name)
            displayNameVerificationEvents.value = user?.profile?.name ?: ""
            binding?.usernameEditText?.setText(user?.authentication?.localAuthentication?.username)
            usernameVerificationEvents.value = user?.username ?: ""
        }
    }
}
