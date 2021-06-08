package com.habitrpg.android.habitica.ui.fragments.preferences

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AppModule
import io.reactivex.rxjava3.disposables.CompositeDisposable
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
        compositeSubscription.add(userRepository.getUser().subscribe({ this.setUser(it) }, RxErrorHandler.handleEmptyError()))
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
