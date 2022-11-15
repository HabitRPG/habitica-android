package com.habitrpg.android.habitica.ui.activities

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.databinding.ActivityVerifyUsernameBinding
import com.habitrpg.android.habitica.extensions.OnChangeTextWatcher
import com.habitrpg.android.habitica.extensions.runDelayed
import com.habitrpg.android.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
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

    override fun getContentView(layoutResId: Int?): View {
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
                    }
            ) { displayNameUsable, usernameUsable -> displayNameUsable && usernameUsable.isUsable }
                .subscribe(
                    {
                        binding.confirmUsernameButton.isEnabled = it
                    },
                    ExceptionHandler.rx()
                )
        )

        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            val user = userRepository.getUser().firstOrNull()
            binding.displayNameEditText.setText(user?.profile?.name)
            displayNameVerificationEvents.onNext(user?.profile?.name ?: "")
            binding.usernameEditText.setText(user?.authentication?.localAuthentication?.username)
            usernameVerificationEvents.onNext(user?.username ?: "")
        }
    }

    private fun confirmNames() {
        binding.confirmUsernameButton.isClickable = false
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.updateUser("profile.name", binding.displayNameEditText.text.toString())
            userRepository.updateLoginName(binding.usernameEditText.text.toString())
            showConfirmationAndFinish()
            binding.confirmUsernameButton.isClickable = true
        }
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
