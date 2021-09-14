package com.habitrpg.android.habitica.ui.fragments.preferences

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.rxjava3.core.Flowable

class ProfilePreferencesFragment : BasePreferencesFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override var user: User? = null
        set(value) {
            field = value
            updateUserFields()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        HabiticaBaseApplication.userComponent?.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    private fun updateUserFields() {
        configurePreference(findPreference("display_name"), user?.profile?.name)
        configurePreference(findPreference("photo_url"), user?.profile?.imageUrl)
        configurePreference(findPreference("about"), user?.profile?.blurb)
    }

    private fun configurePreference(preference: Preference?, value: String?) {
        val editPreference = preference as? EditTextPreference
        editPreference?.text = value
        preference?.summary = value
    }

    override fun setupPreferences() {
        updateUserFields()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String) {
        val profileCategory = findPreference("profile") as? PreferenceCategory
        configurePreference(profileCategory?.findPreference(key), sharedPreferences?.getString(key, ""))
        if (sharedPreferences != null) {
            val newValue = sharedPreferences.getString(key, "") ?: return
            val observable: Flowable<User>? = when (key) {
                "display_name" -> {
                    if (newValue != user?.profile?.name) {
                        userRepository.updateUser("profile.name", newValue)
                    } else {
                        null
                    }
                }
                "photo_url" -> {
                    if (newValue != user?.profile?.imageUrl) {
                        userRepository.updateUser("profile.imageUrl", newValue)
                    } else {
                        null
                    }
                }
                "about" -> {
                    if (newValue != user?.profile?.blurb) {
                        userRepository.updateUser("profile.blurb", newValue)
                    } else {
                        null
                    }
                }
                else -> null
            }
            observable?.subscribe({}, RxErrorHandler.handleEmptyError())?.let { compositeSubscription.add(it) }
        }
    }
}
