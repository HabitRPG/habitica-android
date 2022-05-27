package com.habitrpg.android.habitica.ui.fragments.setup

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentWelcomeBinding
import com.habitrpg.android.habitica.extensions.OnChangeTextWatcher
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WelcomeFragment : BaseFragment<FragmentWelcomeBinding>() {

    val nameValidEvents = PublishSubject.create<Boolean>()

    @Inject
    lateinit var userRepository: UserRepository

    override var binding: FragmentWelcomeBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentWelcomeBinding {
        return FragmentWelcomeBinding.inflate(inflater, container, false)
    }

    private val displayNameVerificationEvents = PublishSubject.create<String>()
    private val usernameVerificationEvents = PublishSubject.create<String>()

    private val checkmarkIcon: Drawable by lazy {
        context?.let {
            BitmapDrawable(resources, HabiticaIconsHelper.imageOfCheckmark(ContextCompat.getColor(it, R.color.green_50), 1f))
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
                displayNameVerificationEvents.onNext(p0.toString())
            }
        )
        binding?.usernameEditText?.addTextChangedListener(
            OnChangeTextWatcher { p0, _, _, _ ->
                usernameVerificationEvents.onNext(p0.toString())
            }
        )

        compositeSubscription.add(
            displayNameVerificationEvents.toFlowable(BackpressureStrategy.DROP)
                .map { it.length in 1..30 }
                .subscribeWithErrorHandler {
                    if (it) {
                        binding?.displayNameEditText?.setCompoundDrawablesWithIntrinsicBounds(null, null, checkmarkIcon, null)
                        binding?.issuesTextView?.visibility = View.GONE
                    } else {
                        binding?.displayNameEditText?.setCompoundDrawablesWithIntrinsicBounds(null, null, alertIcon, null)
                        binding?.issuesTextView?.visibility = View.VISIBLE
                        binding?.issuesTextView?.text = context?.getString(R.string.display_name_length_error)
                    }
                }
        )
        compositeSubscription.add(
            usernameVerificationEvents.toFlowable(BackpressureStrategy.DROP)
                .filter { it.length in 1..30 }
                .throttleLast(1, TimeUnit.SECONDS)
                .flatMap { userRepository.verifyUsername(it) }
                .subscribeWithErrorHandler {
                    if (it.isUsable) {
                        binding?.usernameEditText?.setCompoundDrawablesWithIntrinsicBounds(null, null, checkmarkIcon, null)
                        binding?.issuesTextView?.visibility = View.GONE
                    } else {
                        binding?.usernameEditText?.setCompoundDrawablesWithIntrinsicBounds(null, null, alertIcon, null)
                        binding?.issuesTextView?.visibility = View.VISIBLE
                        binding?.issuesTextView?.text = it.issues.joinToString("\n")
                    }
                    nameValidEvents.onNext(it.isUsable)
                }
        )

        compositeSubscription.add(
            userRepository.getUserFlowable().firstElement().subscribe {
                binding?.displayNameEditText?.setText(it.profile?.name)
                displayNameVerificationEvents.onNext(it.profile?.name ?: "")
                binding?.usernameEditText?.setText(it.username)
                usernameVerificationEvents.onNext(it.username ?: "")
            }
        )
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }
}
