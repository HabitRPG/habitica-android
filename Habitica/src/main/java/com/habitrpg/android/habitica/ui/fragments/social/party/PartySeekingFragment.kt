package com.habitrpg.android.habitica.ui.fragments.social.party

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentComposeBinding
import com.habitrpg.android.habitica.models.invitations.InviteResponse
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.theme.HabiticaTheme
import com.habitrpg.android.habitica.ui.viewmodels.BaseViewModel
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.ClassIcon
import com.habitrpg.android.habitica.ui.views.ComposableAvatarView
import com.habitrpg.android.habitica.ui.views.LoadingButton
import com.habitrpg.android.habitica.ui.views.LoadingButtonState
import com.habitrpg.android.habitica.ui.views.getTranslatedClassName
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PartySeekingViewModel @Inject constructor(
    userRepository : UserRepository,
    userViewModel : MainUserViewModel,
    val socialRepository : SocialRepository
) : BaseViewModel(userRepository, userViewModel) {
    val isRefreshing = mutableStateOf(false)
    val seekingUsers = mutableStateOf<List<Member>>(emptyList())

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

    suspend fun inviteUser(member : Member) : InviteResponse? {
        return socialRepository.inviteToGroup(
            "party", mapOf(
                "uuids" to listOf(member.id ?: "")
            )
        )?.firstOrNull()
    }
}

@AndroidEntryPoint
class PartySeekingFragment : BaseFragment<FragmentComposeBinding>() {
    val viewModel : PartySeekingViewModel by viewModels()

    override var binding : FragmentComposeBinding? = null
    override fun createBinding(
        inflater : LayoutInflater,
        container : ViewGroup?
    ) : FragmentComposeBinding {
        return FragmentComposeBinding.inflate(inflater)
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

    override fun onStart() {
        super.onStart()
        viewModel.retrieveUsers()
    }
}

@Composable
fun ClassText(
    className : String?,
    hasClass : Boolean,
    fontSize : TextUnit,
    modifier : Modifier = Modifier,
    iconSize : Dp? = null
) {
    if (!hasClass) return
    val classColor = colorResource(
        when (className) {
            "warrior" -> R.color.text_red
            "wizard" -> R.color.text_blue
            "rogue" -> R.color.text_brand
            "healer" -> R.color.text_yellow
            else -> R.color.text_primary
        }
    )
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        ClassIcon(
            className = className,
            hasClass = true,
            modifier = Modifier.size(iconSize ?: with(LocalDensity.current) {
                fontSize.toDp()
            })
        )
        Text(
            getTranslatedClassName(LocalContext.current.resources, className),
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            color = classColor
        )
    }
}

@Composable
fun InviteButton(
    state : LoadingButtonState,
    onClick : () -> Unit,
    modifier : Modifier = Modifier
) {
    LoadingButton(state = state, onClick = onClick, modifier = modifier, successContent = {
        Text(stringResource(R.string.invited))
    }) {
        Text(stringResource(R.string.send_invite))
    }
}

@Composable
fun PartySeekingListItem(
    user : Member,
    modifier : Modifier = Modifier,
    onInvite : suspend (Member) -> InviteResponse?
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .background(HabiticaTheme.colors.windowBackground, HabiticaTheme.shapes.large)
            .padding(14.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            ComposableAvatarView(user, Modifier.size(94.dp, 98.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    user.displayName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = HabiticaTheme.colors.textPrimary
                )
                Text(
                    user.formattedUsername ?: "",
                    fontSize = 14.sp,
                    color = HabiticaTheme.colors.textTertiary
                )
                Divider(
                    color = colorResource(R.color.divider_color),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.level_abbreviated, user.stats?.lvl ?: 0),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = HabiticaTheme.colors.textPrimary
                    )
                    ClassText(
                        user.stats?.habitClass,
                        fontSize = 14.sp,
                        iconSize = 18.dp,
                        hasClass = user.preferences?.disableClasses == false
                    )
                }
                Text(
                    stringResource(R.string.x_checkins, user.loginIncentives),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = HabiticaTheme.colors.textPrimary
                )
                Text(
                    Locale(user.preferences?.language ?: "en").getDisplayName(Locale.getDefault()),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = HabiticaTheme.colors.textPrimary
                )
            }
        }
        val scope = rememberCoroutineScope()
        var inviteState : LoadingButtonState by remember { mutableStateOf(LoadingButtonState.CONTENT) }
        InviteButton(state = inviteState, modifier = Modifier.fillMaxWidth(), onClick = {
            scope.launchCatching({
                inviteState = LoadingButtonState.FAILED
            }) {
                inviteState = LoadingButtonState.LOADING
                val response = onInvite(user)
                if (response != null) {
                    inviteState = LoadingButtonState.SUCCESS
                } else {
                    inviteState = LoadingButtonState.FAILED
                }
            }
        })
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun PartySeekingView(
    viewModel : PartySeekingViewModel,
    modifier : Modifier = Modifier
) {
    val users : List<Member> by viewModel.seekingUsers
    val refreshing by viewModel.isRefreshing
    val pullRefreshState = rememberPullRefreshState(refreshing, { viewModel.retrieveUsers() })

    Box(modifier = modifier.pullRefresh(pullRefreshState)) {
        LazyColumn {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp, bottom = 14.dp)
                ) {
                    Text(
                        stringResource(R.string.find_more_members),
                        color = HabiticaTheme.colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        stringResource(R.string.habiticans_looking_party),
                        color = HabiticaTheme.colors.textSecondary
                    )
                }
            }
            items(users) {
                PartySeekingListItem(user = it, modifier = Modifier.animateItemPlacement()) { member ->
                    return@PartySeekingListItem viewModel.inviteUser(member)
                }
            }
        }
        PullRefreshIndicator(refreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
    }
}
