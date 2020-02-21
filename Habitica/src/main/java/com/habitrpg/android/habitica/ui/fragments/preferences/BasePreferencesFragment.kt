package com.habitrpg.android.habitica.ui.fragments.preferences

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.shared.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AppModule
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import javax.inject.Inject
import javax.inject.Named

abstract class BasePreferencesFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var userRepository: UserRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String

    internal open var user: User? = null

    internal val compositeSubscription = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userID = preferenceManager.sharedPreferences.getString(context?.getString(R.string.SP_userID), null)
        if (userID != null) {
            compositeSubscription.add(userRepository.getUser(userID).subscribe(Consumer { this.setUser(it) }, RxErrorHandler.handleEmptyError()))
        }
    }

    override fun onDestroy() {
        userRepository.close()
        compositeSubscription.dispose()
        super.onDestroy()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_fragment, rootKey)
        setupPreferences()
    }

    protected abstract fun setupPreferences()

    open fun setUser(user: User?) {
        this.user = user
    }
}
