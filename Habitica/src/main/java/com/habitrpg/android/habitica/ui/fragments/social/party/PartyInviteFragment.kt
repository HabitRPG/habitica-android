package com.habitrpg.android.habitica.ui.fragments.social.party

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.viewModels
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentComposeBinding
import com.habitrpg.android.habitica.models.invitations.InviteResponse
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.theme.colors
import com.habitrpg.android.habitica.ui.viewmodels.BaseViewModel
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.LoadingButtonState
import com.habitrpg.common.habitica.extensions.isValidEmail
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.theme.HabiticaTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun uUIDFromStringOrNull(name: String): UUID? {
    return try {
        UUID.fromString(name)
    } catch (_: IllegalArgumentException) {
        null
    }
}

@HiltViewModel
class PartyInviteViewModel
@Inject
constructor(
    userRepository: UserRepository,
    userViewModel: MainUserViewModel,
    val socialRepository: SocialRepository
) : BaseViewModel(userRepository, userViewModel) {
    val invites = mutableStateListOf("")

    suspend fun sendInvites(): List<InviteResponse>? {
        val inviteMap =
            mapOf<String, MutableList<Any>>(
                "emails" to mutableListOf(),
                "uuids" to mutableListOf(),
                "usernames" to mutableListOf()
            )
        for (invite in invites) {
            if (invite.isValidEmail()) {
                inviteMap["emails"]?.add(
                    mapOf(
                        "name" to "",
                        "email" to invite
                    )
                )
            } else if (uUIDFromStringOrNull(invite) != null) {
                inviteMap["uuids"]?.add(invite)
            } else if (invite.isNotBlank()) {
                inviteMap["usernames"]?.add(invite)
            }
        }
        return socialRepository.inviteToGroup("party", inviteMap)
    }
}

@AndroidEntryPoint
class PartyInviteFragment : BaseFragment<FragmentComposeBinding>() {
    val viewModel: PartyInviteViewModel by viewModels()

    override var binding: FragmentComposeBinding? = null

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentComposeBinding {
        return FragmentComposeBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        binding?.composeView?.setContent {
            HabiticaTheme {
                PartyInviteView(viewModel) {
                    MainNavigationController.navigateBack()
                }
            }
        }
        return view
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PartyInviteView(
    viewModel: PartyInviteViewModel,
    dismiss: () -> Unit
) {
    var inviteButtonState: LoadingButtonState by remember { mutableStateOf(LoadingButtonState.CONTENT) }
    val scope = rememberCoroutineScope()
    val scrollableState = rememberScrollState()

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(14.dp)
            .scrollable(scrollableState, Orientation.Vertical)
    ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp, bottom = 14.dp)
            ) {
                Text(
                    stringResource(R.string.invite_with_username_email),
                    color = HabiticaTheme.colors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    stringResource(R.string.habiticans_send_invite),
                    color = HabiticaTheme.colors.textSecondary,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal)
                )
            }
        }
        items(viewModel.invites.indices.toList()) { index ->
            if (viewModel.invites.size <= index) return@items
            val invite = viewModel.invites[index]
            val transition = updateTransition(viewModel.invites.size - 1 == index, label = "isLast")
            val rotation =
                transition.animateFloat(
                    label = "isAssigned",
                    transitionSpec = { spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow) }
                ) {
                    if (it) 135f else 0f
                }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 4.dp)
                    .background(HabiticaTheme.colors.windowBackground, HabiticaTheme.shapes.medium)
                    .padding(4.dp, 4.dp)
                    .animateItemPlacement()
            ) {
                Button(
                    onClick = {
                        if (viewModel.invites.size - 1 >= index && viewModel.invites[index].isNotBlank()) {
                            viewModel.invites.removeAt(index)
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier =
                    Modifier
                        .size(32.dp)
                        .padding(3.dp)
                ) {
                    Image(
                        painterResource(R.drawable.ic_close_white_24dp),
                        null,
                        colorFilter = ColorFilter.tint(HabiticaTheme.colors.textPrimary),
                        modifier =
                        Modifier
                            .rotate(rotation.value)
                            .size(32.dp)
                    )
                }

                TextField(
                    value = invite,
                    onValueChange = { value ->
                        if (viewModel.invites.size - 1 == index && viewModel.invites[index].isBlank()) {
                            viewModel.invites.add("")
                        }
                        viewModel.invites[index] = value
                    },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 16.sp),
                    placeholder = { Text(stringResource(R.string.username_or_email)) },
                    colors =
                    TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        focusedTextColor = HabiticaTheme.colors.textPrimary
                    ),
                    modifier =
                    Modifier
                        .onFocusChanged {
                            if (!it.isFocused) {
                                if (viewModel.invites.size > index && viewModel.invites[index].isBlank() && viewModel.invites.size - 1 != index && viewModel.invites.size > 1) {
                                    viewModel.invites.removeAt(index)
                                }
                            }
                        }
                )
            }
        }
        item {
            InviteButton(
                state = if (viewModel.invites.any { it.isNotBlank() }) inviteButtonState else LoadingButtonState.DISABLED,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    inviteButtonState = LoadingButtonState.LOADING
                    scope.launchCatching({
                        inviteButtonState = LoadingButtonState.FAILED
                        scope.launchCatching {
                            delay(2.toDuration(DurationUnit.SECONDS))
                            inviteButtonState = LoadingButtonState.CONTENT
                        }
                    }) {
                        val responses = viewModel.sendInvites()
                        if ((responses?.size ?: 0) > 0) {
                            inviteButtonState = LoadingButtonState.SUCCESS
                            delay(2.toDuration(DurationUnit.SECONDS))
                            dismiss()
                        } else {
                            inviteButtonState = LoadingButtonState.FAILED
                            delay(2.toDuration(DurationUnit.SECONDS))
                            inviteButtonState = LoadingButtonState.CONTENT
                        }
                    }
                }
            )
        }
    }
}
