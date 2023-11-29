package com.habitrpg.android.habitica.ui.views.social

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Divider
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.MainNavDirections
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.auth.LocalAuthentication
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.members.MemberFlags
import com.habitrpg.android.habitica.models.members.MemberPreferences
import com.habitrpg.android.habitica.models.user.Authentication
import com.habitrpg.android.habitica.models.user.ContributorInfo
import com.habitrpg.android.habitica.models.user.Profile
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.ui.fragments.social.party.InviteButton
import com.habitrpg.android.habitica.ui.theme.colors
import com.habitrpg.common.habitica.theme.HabiticaTheme
import com.habitrpg.android.habitica.ui.views.ClassText
import com.habitrpg.common.habitica.views.ComposableAvatarView
import com.habitrpg.android.habitica.ui.views.ComposableUsernameLabel
import com.habitrpg.android.habitica.ui.views.LoadingButtonState
import com.habitrpg.common.habitica.extensions.toLocale
import java.util.Locale
import kotlin.random.Random

@Composable
fun PartySeekingListItem(
    user: Member,
    modifier: Modifier = Modifier,
    inviteState: LoadingButtonState = LoadingButtonState.LOADING,
    isInvited: Boolean = false,
    showHeader: Boolean = false,
    showExtendedInfo: Boolean = true,
    onInvite: (Member) -> Unit
) {
    Column(
        modifier
            .fillMaxWidth()
            .clickable {
                val profileDirections = MainNavDirections.openProfileActivity(user.id)
                MainNavigationController.navigate(profileDirections)
            }
            .padding(bottom = 6.dp)
            .background(HabiticaTheme.colors.windowBackground, HabiticaTheme.shapes.large)
            .padding(14.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            ComposableAvatarView(
                user,
                Modifier
                    .size(94.dp, 98.dp)
                    .padding(top = 4.dp)
            )
            Column(
                verticalArrangement = Arrangement.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (showHeader) {
                    Text(
                        stringResource(R.string.pending_invite).uppercase(),
                        fontSize = 12.sp,
                        color = HabiticaTheme.colors.textQuad,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                ProvideTextStyle(value = TextStyle(fontSize = 14.sp)) {
                    ComposableUsernameLabel(
                        user.displayName,
                        user.contributor?.level ?: 0
                    )
                }
                Text(
                    user.formattedUsername ?: "",
                    fontSize = 14.sp,
                    color = HabiticaTheme.colors.textTertiary
                )
                Divider(
                    color = colorResource(R.color.divider_color),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 6.dp)
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
                        hasClass = user.hasClass
                    )
                }
                if (showExtendedInfo) {
                    Text(
                        stringResource(R.string.x_checkins, user.loginIncentives),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = HabiticaTheme.colors.textPrimary
                    )
                    Text(
                        "Language: ${user.preferences?.language?.toLocale()?.getDisplayLanguage(Locale.getDefault())}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = HabiticaTheme.colors.textPrimary
                    )
                }
            }
        }
        InviteButton(
            state = inviteState,
            isAlreadyInvited = isInvited,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            onClick = {
                onInvite(user)
            }
        )
    }
}

private class MemberProvider : PreviewParameterProvider<Member> {

    private fun generateMember(): Member {
        val member = Member()
        member.profile = Profile()
        member.profile?.name = "User"
        member.authentication = Authentication()
        member.authentication?.localAuthentication = LocalAuthentication()
        member.authentication?.localAuthentication?.username = "username"
        member.preferences = MemberPreferences()
        member.preferences?.disableClasses = false
        member.flags = MemberFlags()
        member.flags?.classSelected = true
        member.stats = Stats()
        member.stats?.hp = Random.nextDouble(from = 0.0, until = 50.0)
        member.stats?.maxHealth = 50
        member.stats?.toNextLevel = Random.nextInt(from = 0, until = 10000)
        member.stats?.exp =
            Random.nextDouble(until = (member.stats?.toNextLevel ?: 0).toDouble())
        member.stats?.maxMP = Random.nextInt(from = 0, until = 10000)
        member.stats?.mp = Random.nextDouble(until = (member.stats?.maxMP ?: 0).toDouble())
        member.stats?.lvl = Random.nextInt(from = 0, until = 9999)
        return member
    }

    override val values: Sequence<Member>
        get() {
            val list = mutableListOf<Member>()
            val earlyMember = generateMember()
            earlyMember.stats?.lvl = 5
            list.add(earlyMember)
            val needsClass = generateMember()
            needsClass.stats?.lvl = 24
            needsClass.stats?.habitClass = "healer"
            needsClass.flags?.classSelected = false
            needsClass.contributor = ContributorInfo()
            needsClass.contributor?.level = 4
            list.add(needsClass)
            val classDisabled = generateMember()
            classDisabled.stats?.lvl = 24
            classDisabled.stats?.habitClass = "rogue"
            classDisabled.preferences?.disableClasses = true
            list.add(classDisabled)
            val subscriber = generateMember()
            subscriber.stats?.habitClass = "warrior"
            list.add(subscriber)
            val onlyHourglasses = generateMember()
            onlyHourglasses.stats?.habitClass = "wizard"
            list.add(onlyHourglasses)
            return list.asSequence()
        }
}

@Preview
@Composable
private fun Preview(@PreviewParameter(MemberProvider::class) data: Member) {
    PartySeekingListItem(user = data, onInvite = {})
}
