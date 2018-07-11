package com.habitrpg.android.habitica.ui.fragments.preferences

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.EditTextPreference
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceCategory
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.Flowable
import io.reactivex.functions.Consumer

class ProfilePreferencesFragment: BasePreferencesFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override var user: User? = null
    set(value) {
        field = value
        updateUserFields()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        HabiticaBaseApplication.component?.inject(this)
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val profileCategory = findPreference("profile") as? PreferenceCategory
        configurePreference(profileCategory?.findPreference(key), sharedPreferences?.getString(key, ""))
        if (sharedPreferences != null) {
            val observable: Flowable<User>? = when (key) {
                "display_name" -> userRepository.updateUser(user, "profile.name", sharedPreferences.getString(key, ""))
                "photo_url" -> userRepository.updateUser(user, "profile.photo", sharedPreferences.getString(key, ""))
                "about" -> userRepository.updateUser(user, "profile.blurb", sharedPreferences.getString(key, ""))
                else -> null
            }
            observable?.subscribe(Consumer {}, RxErrorHandler.handleEmptyError())
        }
    }

}