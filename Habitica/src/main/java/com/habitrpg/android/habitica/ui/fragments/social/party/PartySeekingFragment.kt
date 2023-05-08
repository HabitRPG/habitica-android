package com.habitrpg.android.habitica.ui.fragments.social.party

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
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
import com.habitrpg.android.habitica.ui.views.LoadingButton
import com.habitrpg.android.habitica.ui.views.LoadingButtonState
import com.habitrpg.android.habitica.ui.views.LoadingButtonType
import com.habitrpg.android.habitica.ui.views.progress.HabiticaCircularProgressView
import com.habitrpg.android.habitica.ui.views.progress.HabiticaPullRefreshIndicator
import com.habitrpg.android.habitica.ui.views.social.PartySeekingListItem
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@HiltViewModel
class PartySeekingViewModel @Inject constructor(
    userRepository : UserRepository,
    userViewModel : MainUserViewModel,
    val socialRepository : SocialRepository
) : BaseViewModel(userRepository, userViewModel) {
    val isRefreshing = mutableStateOf(false)
    val seekingUsers : Flow<PagingData<Member>>
    val successfulInvites = mutableStateListOf<String>()
    val inviteStates = mutableStateMapOf<String, LoadingButtonState>()
    init {
        seekingUsers = Pager(
            config = PagingConfig(
                pageSize = 30,
                prefetchDistance = 10
            ),
            pagingSourceFactory = {
                PartySeekingPagingSource(socialRepository)
            }
        ).flow.cachedIn(viewModelScope)
    }

    suspend fun inviteUser(member : Member) : InviteResponse? {
        return socialRepository.inviteToGroup(
            "party", mapOf(
                "uuids" to listOf(member.id)
            )
        )?.firstOrNull()
    }

    suspend fun rescindInvite(member : Member) : Member? {
        return socialRepository.removeMemberFromGroup("party", member.id)?.firstOrNull()
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
}

@Composable
fun InviteButton(
    state : LoadingButtonState,
    onClick : () -> Unit,
    modifier : Modifier = Modifier,
    isAlreadyInvited: Boolean = false,
) {
    LoadingButton(state = state, onClick = onClick,
        type = if (isAlreadyInvited) LoadingButtonType.DESTRUCTIVE else LoadingButtonType.NORMAL,
        colors = ButtonDefaults.buttonColors(
        backgroundColor = if (isAlreadyInvited) HabiticaTheme.colors.errorBackground else HabiticaTheme.colors.tintedUiSub,
        contentColor = Color.White,
    ), modifier = modifier, successContent = {
        if (isAlreadyInvited) {
            Text(stringResource(R.string.rescinded))
        } else {
            Text(stringResource(R.string.invited))
        }
    }) {
        if (isAlreadyInvited) {
            Text(stringResource(R.string.rescind_invite))
        } else {
            Text(stringResource(R.string.send_invite))
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun PartySeekingView(
    viewModel : PartySeekingViewModel,
    modifier : Modifier = Modifier
) {
    val pageData = viewModel.seekingUsers.collectAsLazyPagingItems()
    val refreshing by viewModel.isRefreshing
    val pullRefreshState = rememberPullRefreshState(refreshing, { pageData.refresh() })
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 36.dp, bottom = 14.dp)
                ) {
                    Text(
                        stringResource(R.string.find_more_members),
                        color = HabiticaTheme.colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    if (pageData.itemCount == 0 && pageData.loadState.refresh is LoadState.NotLoading && pageData.loadState.append is LoadState.NotLoading) {
                        Text(
                            stringResource(R.string.habiticans_looking_party_empty),
                            textAlign = TextAlign.Center,
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
                            color = HabiticaTheme.colors.textSecondary, modifier = Modifier
                                .width(250.dp)
                                .align(alignment = Alignment.CenterHorizontally)
                        )
                        Image(
                            painterResource(R.drawable.looking_for_party_empty), null,
                            modifier = Modifier.padding(top = 50.dp)
                        )
                    } else {
                        Text(
                            stringResource(R.string.habiticans_looking_party),
                            textAlign = TextAlign.Center,
                            color = HabiticaTheme.colors.textSecondary, modifier = Modifier
                                .width(250.dp)
                                .align(alignment = Alignment.CenterHorizontally)
                        )
                    }
                }
            }
            items(
                items = pageData
            ) {
                if (it == null) return@items
                PartySeekingListItem(
                    user = it,
                    inviteState =viewModel.inviteStates[it.id] ?: LoadingButtonState.CONTENT,
                    isInvited = viewModel.successfulInvites.contains(it.id),
                    modifier = Modifier
                        .animateItemPlacement()
                        .padding(horizontal = 14.dp)
                ) { member ->
                    scope.launchCatching({
                        viewModel.inviteStates[member.id] = LoadingButtonState.FAILED
                    }) {
                        viewModel.inviteStates[member.id] = LoadingButtonState.LOADING
                        val response: Any? = if (viewModel.successfulInvites.contains(member.id)) viewModel.rescindInvite(member) else viewModel.inviteUser(member)
                        if (response != null) {
                            viewModel.inviteStates[member.id] = LoadingButtonState.SUCCESS
                            delay(4.toDuration(DurationUnit.SECONDS))
                            if (viewModel.successfulInvites.contains(member.id)) {
                                viewModel.successfulInvites.remove(member.id)
                            } else {
                                viewModel.successfulInvites.add(member.id)
                            }
                            viewModel.inviteStates[member.id] = LoadingButtonState.CONTENT
                        } else {
                            viewModel.inviteStates[member.id] = LoadingButtonState.FAILED
                        }
                    }
                }
            }

            when (pageData.loadState.refresh) {
                is LoadState.Error -> {
                }

                is LoadState.Loading -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillParentMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            HabiticaCircularProgressView()
                        }
                    }
                }

                else -> {}
            }

            when (pageData.loadState.append) {
                is LoadState.Error -> {
                }

                is LoadState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            HabiticaCircularProgressView(indicatorSize = 32.dp)
                        }
                    }
                }

                else -> {}
            }
        }
        HabiticaPullRefreshIndicator(
            pageData.itemCount == 0,
            refreshing,
            pullRefreshState,
            Modifier.align(Alignment.TopCenter)
        )
    }
}

class PartySeekingPagingSource(
    private val repository : SocialRepository,
) : PagingSource<Int, Member>() {
    override fun getRefreshKey(state : PagingState<Int, Member>) : Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params : LoadParams<Int>) : LoadResult<Int, Member> {
        return try {
            val page = params.key ?: 0
            val response = repository.retrievePartySeekingUsers(page)

            LoadResult.Page(
                data = response ?: emptyList(),
                prevKey = if (page == 0) null else page.minus(1),
                nextKey = if ((response?.size ?: 0) < 30) null else page.plus(1),
            )
        } catch (e : Exception) {
            LoadResult.Error(e)
        }
    }
}
