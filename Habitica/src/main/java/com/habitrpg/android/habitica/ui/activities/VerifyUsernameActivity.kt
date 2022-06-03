package com.habitrpg.android.habitica.ui.activities

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.databinding.ActivityVerifyUsernameBinding
import com.habitrpg.android.habitica.extensions.OnChangeTextWatcher
import com.habitrpg.android.habitica.extensions.runDelayed
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.common.habitica.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class VerifyUsernameActivity : BaseActivity() {
    private lateinit var binding: ActivityVerifyUsernameBinding

    private val displayNameVerificationEvents = PublishSubject.create<String>()
    private val usernameVerificationEvents = PublishSubject.create<String>()

    private val checkmarkIcon: Drawable by lazy {
        BitmapDrawable(resources, HabiticaIconsHelper.imageOfCheckmark(ContextCompat.getColor(this, R.color.text_green), 1f))
    }
    private val alertIcon: Drawable by lazy {
        BitmapDrawable(resources, HabiticaIconsHelper.imageOfAlertIcon())
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_verify_username
    }

    override fun getContentView(): View {
        binding = ActivityVerifyUsernameBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.wikiTextView.movementMethod = LinkMovementMethod.getInstance()
        binding.footerTextView.movementMethod = LinkMovementMethod.getInstance()

        binding.confirmUsernameButton.setOnClickListener { confirmNames() }

        binding.displayNameEditText.addTextChangedListener(
            OnChangeTextWatcher { p0, _, _, _ ->
                displayNameVerificationEvents.onNext(p0.toString())
            }
        )
        binding.usernameEditText.addTextChangedListener(
            OnChangeTextWatcher { p0, _, _, _ ->
                usernameVerificationEvents.onNext(p0.toString())
            }
        )

        compositeSubscription.add(
            Flowable.combineLatest(
                displayNameVerificationEvents.toFlowable(BackpressureStrategy.DROP)
                    .map { it.length in 1..30 }
                    .doOnNext {
                        if (it) {
                            binding.displayNameEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, checkmarkIcon, null)
                            binding.issuesTextView.visibility = View.GONE
                        } else {
                            binding.displayNameEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, alertIcon, null)
                            binding.issuesTextView.visibility = View.VISIBLE
                            binding.issuesTextView.text = getString(R.string.display_name_length_error)
                        }
                    },
                usernameVerificationEvents.toFlowable(BackpressureStrategy.DROP)
                    .throttleLast(1, TimeUnit.SECONDS)
                    .flatMap { userRepository.verifyUsername(binding.usernameEditText.text.toString()) }
                    .doOnNext {
                        if (it.isUsable) {
                            binding.usernameEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, checkmarkIcon, null)
                            binding.issuesTextView.visibility = View.GONE
                        } else {
                            binding.usernameEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, alertIcon, null)
                            binding.issuesTextView.visibility = View.VISIBLE
                            binding.issuesTextView.text = it.issues.joinToString("\n")
                        }
                    },
                { displayNameUsable, usernameUsable -> displayNameUsable && usernameUsable.isUsable }
            )
                .subscribe(
                    {
                        binding.confirmUsernameButton.isEnabled = it
                    },
                    RxErrorHandler.handleEmptyError()
                )
        )

        compositeSubscription.add(
            userRepository.getUserFlowable().firstElement().subscribe {
                binding.displayNameEditText.setText(it.profile?.name)
                displayNameVerificationEvents.onNext(it.profile?.name ?: "")
                binding.usernameEditText.setText(it.authentication?.localAuthentication?.username)
                usernameVerificationEvents.onNext(it.username ?: "")
            }
        )
    }

    private fun confirmNames() {
        binding.confirmUsernameButton.isClickable = false
        compositeSubscription.add(
            userRepository.updateUser("profile.name", binding.displayNameEditText.text.toString())
                .flatMap { userRepository.updateLoginName(binding.usernameEditText.text.toString()).toFlowable() }
                .doOnComplete { showConfirmationAndFinish() }
                .doOnEach { binding.confirmUsernameButton.isClickable = true }
                .subscribe({ }, RxErrorHandler.handleEmptyError())
        )
    }

    private fun showConfirmationAndFinish() {
        HabiticaSnackbar.showSnackbar(binding.snackbarView, getString(R.string.username_confirmed), HabiticaSnackbar.SnackbarDisplayType.SUCCESS)
        runDelayed(3, TimeUnit.SECONDS) {
            finish()
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}
