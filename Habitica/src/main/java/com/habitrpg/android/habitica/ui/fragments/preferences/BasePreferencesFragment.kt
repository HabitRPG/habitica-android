package com.habitrpg.android.habitica.ui.fragments.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceFragmentCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import javax.inject.Inject

abstract class BasePreferencesFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var userViewModel: MainUserViewModel

    internal open var user: User? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        userViewModel.user.observe(viewLifecycleOwner) { setUser(it) }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroy() {
        userRepository.close()
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
