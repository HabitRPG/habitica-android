package com.habitrpg.android.habitica.ui.fragments.social.party

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.FragmentComposeBinding
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.theme.HabiticaTheme
import com.habitrpg.android.habitica.ui.viewmodels.BaseViewModel
import com.habitrpg.common.habitica.helpers.launchCatching
import javax.inject.Inject

class PartySeekingViewModel: BaseViewModel() {
    val isRefreshing = mutableStateOf(false)

    @Inject
    lateinit var socialRepository: SocialRepository

    val seekingUsers = mutableStateOf<List<Member>>(emptyList())

    override fun inject(component : UserComponent) {
        component.inject(this)
    }

    init {
        retrieveUsers()
    }

    fun retrieveUsers() {
        isRefreshing.value = true
        viewModelScope.launchCatching {
            seekingUsers.value = socialRepository.retrievePartySeekingUsers() ?: emptyList()
            isRefreshing.value = false
        }
    }
}

class PartySeekingFragment: BaseFragment<FragmentComposeBinding>() {
    val viewModel: PartySeekingViewModel by viewModels()

    override var binding: FragmentComposeBinding? = null
    override fun createBinding(
        inflater : LayoutInflater,
        container : ViewGroup?
    ) : FragmentComposeBinding {
        return FragmentComposeBinding.inflate(inflater)
    }

    override fun injectFragment(component : UserComponent) {
        component.inject(this)
    }

    override fun onCreateView(
        inflater : LayoutInflater,
        container : ViewGroup?,
        savedInstanceState : Bundle?
    ) : View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        binding?.composeView?.setContent {
            HabiticaTheme {
                PartySeekingView(viewModel)
            }
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        viewModel.retrieveUsers()
    }
}

@Composable
fun PartySeekingListItem(user: Member,
    modifier : Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Text(user.username ?: "")
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PartySeekingView(
    viewModel: PartySeekingViewModel,
    modifier : Modifier = Modifier
) {
    val users: List<Member> by viewModel.seekingUsers
    val refreshing by viewModel.isRefreshing
    val pullRefreshState = rememberPullRefreshState(refreshing, { viewModel.retrieveUsers() })

    LazyColumn(modifier.pullRefresh(pullRefreshState)) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 14.dp)) {
                Text(stringResource(R.string.find_more_members), color = HabiticaTheme.colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(stringResource(R.string.habiticans_looking_party), color = HabiticaTheme.colors.textSecondary)
            }
        }
        items(users) {
            PartySeekingListItem(user = it)
        }
    }
}
