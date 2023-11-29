package com.habitrpg.android.habitica.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.auth.LocalAuthentication
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.Authentication
import com.habitrpg.android.habitica.models.user.Profile
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.ui.theme.colors
import com.habitrpg.common.habitica.theme.HabiticaTheme
import com.habitrpg.common.habitica.views.ComposableAvatarView
import kotlin.random.Random

@Composable
fun GroupPlanMemberList(
    members: List<Member>?,
    group: Group?,
    onMemberClicked: (String) -> Unit
) {
    LazyColumn {
        item {
            Text(
                stringResource(R.string.member_list),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = HabiticaTheme.colors.textTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            )
        }
        for (
        member in members?.sortedByDescending { it.authentication?.timestamps?.lastLoggedIn }
            ?: emptyList()
        ) {
            item {
                val role = if (group?.isLeader(member.id) == true) {
                    stringResource(R.string.owner)
                } else if (group?.isManager(member.id) == true) {
                    stringResource(R.string.manager)
                } else {
                    stringResource(R.string.member)
                }
                MemberItem(
                    member,
                    role,
                    onMemberClicked,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MemberItem(
    member: Member,
    role: String,
    onMemberClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(HabiticaTheme.shapes.large)
            .background(HabiticaTheme.colors.windowBackground)
            .clickable {
                member.id?.let { onMemberClicked(it) }
            }
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            ComposableAvatarView(
                avatar = member,
                modifier = Modifier
                    .padding(6.dp)
                    .size(94.dp, 98.dp)
            )
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .height(104.dp)
                    .padding(end = 6.dp)
            ) {
                Text(
                    member.displayName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = HabiticaTheme.colors.textPrimary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        member.formattedUsername ?: "",
                        color = HabiticaTheme.colors.textTertiary
                    )
                    Spacer(
                        Modifier
                            .weight(1.0f)
                    )
                    ClassIcon(
                        member.stats?.habitClass,
                        member.hasClass,
                        modifier = Modifier.size(18.dp)
                    )
                    BuffIcon(member.stats?.isBuffed)
                    CurrencyText(
                        currency = "gold",
                        value = (member.stats?.gp) ?: 0.0,
                        decimals = 0,
                        animated = false
                    )
                }
                LabeledBar(
                    color = colorResource(R.color.hpColor),
                    barColor = HabiticaTheme.colors.contentBackgroundOffset,
                    value = member.stats?.hp ?: 0.0,
                    maxValue = (member.stats?.maxHealth ?: 0).toDouble(),
                    displayCompact = true,
                    barHeight = 5.dp,
                    animated = false
                )
                LabeledBar(
                    color = colorResource(R.color.xpColor),
                    barColor = HabiticaTheme.colors.contentBackgroundOffset,
                    value = member.stats?.exp ?: 0.0,
                    maxValue = (member.stats?.toNextLevel ?: 0).toDouble(),
                    displayCompact = true,
                    barHeight = 5.dp,
                    animated = false
                )
                if (member.hasClass) {
                    LabeledBar(
                        color = colorResource(R.color.mpColor),
                        barColor = HabiticaTheme.colors.contentBackgroundOffset,
                        value = member.stats?.mp ?: 0.0,
                        maxValue = (member.stats?.maxMP ?: 0).toDouble(),
                        displayCompact = true,
                        barHeight = 5.dp,
                        animated = false
                    )
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        stringResource(R.string.level_unabbreviated, member.stats?.lvl ?: 0),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = HabiticaTheme.colors.textTertiary
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        role,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = HabiticaTheme.colors.textSecondary
                    )
                }
            }
        }
    }
}

private class MemberProvider : PreviewParameterProvider<Member> {
    override val values: Sequence<Member>
        get() {
            val list = mutableListOf<Member>()
            for (x in 0..5) {
                val member = Member()
                member.profile = Profile()
                member.profile?.name = "User $x"
                member.authentication = Authentication()
                member.authentication?.localAuthentication = LocalAuthentication()
                member.authentication?.localAuthentication?.username = "user$x"
                member.stats = Stats()
                member.stats?.hp = Random.nextDouble()
                member.stats?.maxHealth = 50
                member.stats?.toNextLevel = Random.nextInt()
                member.stats?.exp =
                    Random.nextDouble(until = (member.stats?.toNextLevel ?: 0).toDouble())
                member.stats?.maxMP = Random.nextInt()
                member.stats?.mp = Random.nextDouble(until = (member.stats?.maxMP ?: 0).toDouble())
                member.stats?.lvl = Random.nextInt()
                list.add(member)
            }
            return list.asSequence()
        }
}

@Composable
@Preview
private fun Preview(@PreviewParameter(MemberProvider::class) member: Member) {
    MemberItem(member = member, role = "Manager", onMemberClicked = {})
}
