package com.habitrpg.android.habitica.ui.fragments.social.party

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentComposeBinding
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.viewmodels.BaseViewModel
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PartyInviteViewModel @Inject constructor(
    userRepository : UserRepository,
    userViewModel : MainUserViewModel,
    val socialRepository : SocialRepository
) : BaseViewModel(userRepository, userViewModel) {
}

@AndroidEntryPoint
class PartyInviteFragment : BaseFragment<FragmentComposeBinding>() {

    @Inject
    lateinit var configManager : AppConfigManager

    override var binding : FragmentComposeBinding? = null

    override fun createBinding(
        inflater : LayoutInflater,
        container : ViewGroup?
    ) : FragmentComposeBinding {
        return FragmentComposeBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view : View, savedInstanceState : Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
