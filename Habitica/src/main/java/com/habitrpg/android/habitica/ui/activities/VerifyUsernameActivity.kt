package com.habitrpg.android.habitica.ui.activities

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.runDelayed
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class VerifyUsernameActivity: BaseActivity() {
    @Inject
    lateinit var userRepository: UserRepository

    private val displayNameEditText: EditText by bindView(R.id.display_name_edit_text)
    private val usernameEditText: EditText by bindView(R.id.username_edit_text)
    private val confirmUsernameButton: Button by bindView(R.id.confirm_username_button)
    private val issuesTextView: TextView by bindView(R.id.issues_text_view)
    private val snackbarView: ViewGroup by bindView(R.id.snackbar_view)

    private val verificationEvents = PublishSubject.create<String>()

    private val checkmarkIcon: Drawable by lazy {
        BitmapDrawable(resources, HabiticaIconsHelper.imageOfCheckmark(ContextCompat.getColor(this, R.color.green_50), 1f))
    }
    private val alertIcon: Drawable by lazy {
        BitmapDrawable(resources, HabiticaIconsHelper.imageOfAlertIcon())
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_verify_username
    }

    override fun injectActivity(component: AppComponent?) {
        component?.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        compositeSubscription.add(userRepository.getUser().firstElement().subscribe {
            displayNameEditText.setText(it.profile?.name)
            usernameEditText.setText(it.authentication?.localAuthentication?.username)
        })

        confirmUsernameButton.setOnClickListener { confirmNames() }

        usernameEditText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                verificationEvents.onNext(p0.toString())
            }

        })
        compositeSubscription.add(verificationEvents.toFlowable(BackpressureStrategy.DROP)
                .throttleLast(1, TimeUnit.SECONDS)
                .flatMap { userRepository.verifyUsername(usernameEditText.text.toString()) }
                .subscribe {
                    if (it.isUsable) {
                        usernameEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, checkmarkIcon, null)
                        issuesTextView.visibility = View.GONE
                    } else {
                        usernameEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, alertIcon, null)
                        issuesTextView.visibility = View.VISIBLE
                        issuesTextView.text = it.issues.joinToString("\n")
                    }
                    confirmUsernameButton.isEnabled = it.isUsable
                })
    }

    private fun confirmNames() {
        confirmUsernameButton.isEnabled = false
        compositeSubscription.add(userRepository.updateUser(null, "profile.name", displayNameEditText.text.toString())
                .flatMap { userRepository.updateLoginName(usernameEditText.text.toString()).toFlowable() }
                .doOnComplete { showConfirmationAndFinish() }
                .doOnEach { confirmUsernameButton.isEnabled = true }
                .subscribe(Consumer {  }, RxErrorHandler.handleEmptyError()))
    }

    private fun showConfirmationAndFinish() {
        HabiticaSnackbar.showSnackbar(snackbarView, getString(R.string.username_confirmed), HabiticaSnackbar.SnackbarDisplayType.SUCCESS)
        runDelayed(3, TimeUnit.SECONDS) {
            finish()
        }
    }
}