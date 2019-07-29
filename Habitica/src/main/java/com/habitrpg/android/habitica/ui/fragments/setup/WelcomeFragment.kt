package com.habitrpg.android.habitica.ui.fragments.setup

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.OnChangeTextWatcher
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
import com.habitrpg.android.habitica.helpers.AmplitudeManager
import com.habitrpg.android.habitica.ui.SpeechBubbleView
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import io.reactivex.BackpressureStrategy
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WelcomeFragment : BaseFragment() {

    val nameValidEvents = PublishSubject.create<Boolean>()

    @Inject
    lateinit var userRepository: UserRepository

    private val speechBubbleView: SpeechBubbleView? by bindView(R.id.speech_bubble)
    private val displayNameEditText: EditText by bindView(R.id.display_name_edit_text)
    private val usernameEditText: EditText by bindView(R.id.username_edit_text)
    private val issuesTextView: TextView by bindView(R.id.issues_text_view)

    private val displayNameVerificationEvents = PublishSubject.create<String>()
    private val usernameVerificationEvents = PublishSubject.create<String>()

    private val checkmarkIcon: Drawable by lazy {
        BitmapDrawable(resources, HabiticaIconsHelper.imageOfCheckmark(ContextCompat.getColor(context!!, R.color.green_50), 1f))
    }
    private val alertIcon: Drawable by lazy {
        BitmapDrawable(resources, HabiticaIconsHelper.imageOfAlertIcon())
    }
    val username: String
    get() = usernameEditText.text.toString()
    val displayName: String
    get() = displayNameEditText.text.toString()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return container?.inflate(R.layout.fragment_welcome)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetViews()

        speechBubbleView?.animateText(context?.getString(R.string.welcome_text) ?: "")

        super.onCreate(savedInstanceState)

        displayNameEditText.addTextChangedListener(OnChangeTextWatcher { p0, _, _, _ ->
                displayNameVerificationEvents.onNext(p0.toString())
        })
        usernameEditText.addTextChangedListener(OnChangeTextWatcher { p0, _, _, _ ->
                usernameVerificationEvents.onNext(p0.toString())
        })

        compositeSubscription.add(displayNameVerificationEvents.toFlowable(BackpressureStrategy.DROP)
                .map { it.length in 1..30 }
                .subscribeWithErrorHandler(Consumer {
                    if (it) {
                        displayNameEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, checkmarkIcon, null)
                        issuesTextView.visibility = View.GONE
                    } else {
                        displayNameEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, alertIcon, null)
                        issuesTextView.visibility = View.VISIBLE
                        issuesTextView.text = context?.getString(R.string.display_name_length_error)
                    }
                }))
        compositeSubscription.add(usernameVerificationEvents.toFlowable(BackpressureStrategy.DROP)
                .filter { it.length in 1..30 }
                .throttleLast(1, TimeUnit.SECONDS)
                .flatMap { userRepository.verifyUsername(it) }
                .subscribeWithErrorHandler(Consumer {
                    if (it.isUsable) {
                        usernameEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, checkmarkIcon, null)
                        issuesTextView.visibility = View.GONE
                    } else {
                        usernameEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, alertIcon, null)
                        issuesTextView.visibility = View.VISIBLE
                        issuesTextView.text = it.issues.joinToString("\n")
                    }
                    nameValidEvents.onNext(it.isUsable)
                }))

        compositeSubscription.add(userRepository.getUser().firstElement().subscribe {
            displayNameEditText.setText(it.profile?.name)
            displayNameVerificationEvents.onNext(it.profile?.name ?: "")
            usernameEditText.setText(it.username)
            usernameVerificationEvents.onNext(it.username ?: "")
        })
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }
}
