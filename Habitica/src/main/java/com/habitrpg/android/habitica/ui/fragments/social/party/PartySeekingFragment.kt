package com.habitrpg.android.habitica.ui.fragments.social.party

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentComposeBinding
import com.habitrpg.android.habitica.helpers.Analytics
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.EventCategory
import com.habitrpg.android.habitica.helpers.HitType
import com.habitrpg.android.habitica.models.invitations.InviteResponse
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.theme.colors
import com.habitrpg.android.habitica.ui.viewmodels.BaseViewModel
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.LoadingButton
import com.habitrpg.android.habitica.ui.views.LoadingButtonState
import com.habitrpg.android.habitica.ui.views.LoadingButtonType
import com.habitrpg.android.habitica.ui.views.progress.HabiticaPullRefreshIndicator
import com.habitrpg.android.habitica.ui.views.social.PartySeekingListItem
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.theme.HabiticaTheme
import com.habitrpg.common.habitica.views.HabiticaCircularProgressView
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@HiltViewModel
class PartySeekingViewModel
    @Inject
    constructor(
        userRepository: UserRepository,
        userViewModel: MainUserViewModel,
        val socialRepository: SocialRepository,
        val configManager: AppConfigManager,
    ) : BaseViewModel(userRepository, userViewModel) {
        val isRefreshing = mutableStateOf(false)
        val seekingUsers: Flow<PagingData<Member>>
        val inviteStates = mutableStateMapOf<String, Pair<Boolean, LoadingButtonState>>()

        init {
            seekingUsers =
                Pager(
                    config =
                        PagingConfig(
                            pageSize = 30,
                            prefetchDistance = 10,
                        ),
                    pagingSourceFactory = {
                        PartySeekingPagingSource(socialRepository)
                    },
                ).flow.cachedIn(viewModelScope)
        }

        suspend fun inviteUser(member: Member): InviteResponse? {
            return socialRepository.inviteToGroup(
                "party",
                mapOf(
                    "uuids" to listOf(member.id),
                ),
            )?.firstOrNull()
        }

        suspend fun rescindInvite(member: Member): Member? {
            return socialRepository.removeMemberFromGroup("party", member.id)?.firstOrNull()
        }
    }

@AndroidEntryPoint
class PartySeekingFragment : BaseFragment<FragmentComposeBinding>() {
    val viewModel: PartySeekingViewModel by viewModels()

    override var binding: FragmentComposeBinding? = null

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentComposeBinding {
        return FragmentComposeBinding.inflate(inflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
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
        Analytics.sendEvent("View Find Members", EventCategory.NAVIGATION, HitType.EVENT)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun InviteButton(
    state: LoadingButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAlreadyInvited: Boolean = false,
) {
    AnimatedContent(
        transitionSpec = {
            fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith
                fadeOut(animationSpec = tween(90))
        },
        targetState = isAlreadyInvited,
    ) { isInvited ->
        if (isInvited) {
            LoadingButton(
                state = state,
                onClick = onClick,
                type = LoadingButtonType.DESTRUCTIVE,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = HabiticaTheme.colors.errorBackground,
                        contentColor = Color.White,
                    ),
                modifier = modifier,
                successContent = {
                    Text(stringResource(R.string.rescinded))
                },
            ) {
                Text(stringResource(R.string.rescind_invite))
            }
        } else {
            LoadingButton(
                state = state,
                onClick = onClick,
                modifier = modifier,
                successContent = {
                    Text(stringResource(R.string.invited))
                },
            ) {
                Text(stringResource(R.string.send_invite))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PartySeekingView(
    viewModel: PartySeekingViewModel,
    modifier: Modifier = Modifier,
) {
    val pageData = viewModel.seekingUsers.collectAsLazyPagingItems()
    val refreshing by viewModel.isRefreshing
    val pullRefreshState = rememberPullToRefreshState()
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            pageData.refresh()
        }
    }
    if (!refreshing) {
        LaunchedEffect(true) {
            pullRefreshState.endRefresh()
        }
    }
    val scope = rememberCoroutineScope()

    Box(
        modifier =
        modifier
            .fillMaxSize()
            .nestedScroll(pullRefreshState.nestedScrollConnection)
    ) {
        LazyColumn {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 36.dp, bottom = 14.dp),
                ) {
                    Text(
                        stringResource(R.string.find_more_members),
                        color = HabiticaTheme.colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    if (pageData.itemCount == 0 && pageData.loadState.refresh is LoadState.NotLoading && pageData.loadState.append is LoadState.NotLoading) {
                        Text(
                            stringResource(R.string.habiticans_looking_party_empty),
                            textAlign = TextAlign.Center,
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
                            color = HabiticaTheme.colors.textSecondary,
                            modifier =
                            Modifier
                                .width(320.dp)
                                .align(alignment = Alignment.CenterHorizontally),
                        )
                        Image(
                            painterResource(R.drawable.looking_for_party_empty),
                            null,
                            modifier = Modifier.padding(top = 50.dp),
                        )
                    } else {
                        Text(
                            stringResource(R.string.habiticans_looking_party),
                            textAlign = TextAlign.Center,
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
                            color = HabiticaTheme.colors.textSecondary,
                            modifier =
                            Modifier
                                .width(320.dp)
                                .align(alignment = Alignment.CenterHorizontally),
                        )
                    }
                }
            }
            items(
                pageData.itemCount,
            ) {
                val item = pageData[it] ?: return@items
                PartySeekingListItem(
                    user = item,
                    inviteState =
                        viewModel.inviteStates[item.id]?.second
                            ?: LoadingButtonState.CONTENT,
                    isInvited = viewModel.inviteStates[item.id]?.first ?: false,
                    configManager = viewModel.configManager,
                    modifier =
                    Modifier
                        .animateItemPlacement()
                        .padding(horizontal = 14.dp),
                ) { member ->
                    scope.launchCatching({
                        viewModel.inviteStates[member.id] = Pair(false, LoadingButtonState.FAILED)
                    }) {
                        val isInvited = viewModel.inviteStates[member.id]?.first ?: false
                        viewModel.inviteStates[member.id] =
                            Pair(isInvited, LoadingButtonState.LOADING)
                        val response: Any? =
                            if (isInvited) {
                                viewModel.rescindInvite(member)
                            } else {
                                viewModel.inviteUser(
                                    member,
                                )
                            }
                        if (response != null) {
                            viewModel.inviteStates[member.id] =
                                Pair(isInvited, LoadingButtonState.SUCCESS)
                            delay(2500.toDuration(DurationUnit.MILLISECONDS))
                            viewModel.inviteStates[member.id] =
                                Pair(!isInvited, LoadingButtonState.CONTENT)
                        } else {
                            viewModel.inviteStates[member.id] =
                                Pair(isInvited, LoadingButtonState.FAILED)
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
                            modifier =
                                Modifier
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
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            HabiticaCircularProgressView(indicatorSize = 32.dp)
                        }
                    }
                }

                else -> {}
            }
        }
        PullToRefreshContainer(modifier = Modifier.align(Alignment.TopCenter),
            state = pullRefreshState,
            indicator = {
                HabiticaPullRefreshIndicator(
                    pageData.itemCount == 0,
                    refreshing,
                    it,
                    Modifier.align(Alignment.TopCenter),
                )
            })

    }
}

class PartySeekingPagingSource(
    private val repository: SocialRepository,
) : PagingSource<Int, Member>() {
    override fun getRefreshKey(state: PagingState<Int, Member>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Member> {
        return try {
            val page = params.key ?: 0
            val response = repository.retrievePartySeekingUsers(page)

            LoadResult.Page(
                data = response ?: emptyList(),
                prevKey = if (page == 0) null else page.minus(1),
                nextKey = if ((response?.size ?: 0) < 30) null else page.plus(1),
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
