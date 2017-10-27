package com.habitrpg.android.habitica.ui.fragments.preferences

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import com.habitrpg.android.habitica.HabiticaBaseApplication

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.QrCodeManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AppModule
import rx.functions.Action1
import rx.subscriptions.CompositeSubscription
import java.util.*
import javax.inject.Inject
import javax.inject.Named

abstract class BasePreferencesFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var userRepository: UserRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String

    internal open var user: User? = null

    internal val compositeSubscription = CompositeSubscription()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userRepository.getUser(userId).first().subscribe(Action1<User> {
            this.user = it
        }, RxErrorHandler.handleEmptyError())
    }

    override fun onDestroy() {
        userRepository.close()
        compositeSubscription.unsubscribe()
        super.onDestroy()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_fragment, rootKey)
        setupPreferences()
    }

    protected abstract fun setupPreferences()

}
