package com.habitrpg.android.habitica.ui.views

import androidx.compose.foundation.Image
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
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.auth.LocalAuthentication
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.user.Authentication
import com.habitrpg.android.habitica.models.user.Profile
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.ui.theme.HabiticaTheme
import com.habitrpg.common.habitica.extensions.getThemeColor
import kotlin.random.Random

@Composable
fun GroupPlanMemberList(
    members: List<Member>,
    onMemberClicked: (String) -> Unit,
    onMoreClicked: (Member) -> Unit
) {
    LazyColumn {
        for (member in members) {
            item {
                MemberItem(
                    member,
                    onMemberClicked,
                    onMoreClicked,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MemberItem(
    member: Member,
    onMemberClicked: (String) -> Unit,
    onMoreClicked: (Member) -> Unit,
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
        TextButton(
            onClick = { onMoreClicked(member) }, modifier = Modifier
                .size(32.dp)
                .background(
                    Color(LocalContext.current.getThemeColor(R.attr.colorAccent)),
                    WobblyCircle
                )
                .align(Alignment.TopEnd)
        ) {
            Image(painterResource(R.drawable.menu_messages), null)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            ComposableAvatarView(avatar = member, modifier = Modifier.size(94.dp, 98.dp))
            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.height(100.dp)) {
                Text(
                    member.displayName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = HabiticaTheme.colors.textPrimary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        member.formattedUsername ?: "",
                        color = HabiticaTheme.colors.textSecondary
                    )
                    Spacer(
                        Modifier
                            .weight(1.0f)
                            .background(Color.Red)
                    )
                    ClassIcon(
                        member.stats?.habitClass,
                        member.hasClass,
                        modifier = Modifier.size(18.dp)
                    )
                    BuffIcon(member.stats?.isBuffed)
                    CurrencyText(currency = "gold", value = member.stats?.gp ?: 0.0)
                }
                LabeledBar(
                    color = colorResource(R.color.hpColor),
                    barColor = HabiticaTheme.colors.contentBackgroundOffset,
                    value = member.stats?.hp ?: 0.0,
                    maxValue = (member.stats?.maxHealth ?: 0).toDouble(),
                    displayCompact = true
                )
                LabeledBar(
                    color = colorResource(R.color.xpColor),
                    barColor = HabiticaTheme.colors.contentBackgroundOffset,
                    value = member.stats?.exp ?: 0.0,
                    maxValue = (member.stats?.toNextLevel ?: 0).toDouble(),
                    displayCompact = true
                )
                if (member.hasClass) {
                    LabeledBar(
                        color = colorResource(R.color.mpColor),
                        barColor = HabiticaTheme.colors.contentBackgroundOffset,
                        value = member.stats?.mp ?: 0.0,
                        maxValue = (member.stats?.maxMP ?: 0).toDouble(),
                        displayCompact = true
                    )
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        stringResource(R.string.level_unabbreviated, member.stats?.lvl ?: 0),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = HabiticaTheme.colors.textPrimary
                    )
                    Text(
                        "", fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                        color = HabiticaTheme.colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun BuffIcon(buffed: Boolean?, modifier: Modifier = Modifier) {
    if (buffed == true) {
        Image(HabiticaIconsHelper.imageOfBuffIcon().asImageBitmap(), null, modifier = modifier)
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
                member.authentication?.localAuthentication?.username = "user${x}"
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
    MemberItem(member = member, onMemberClicked = {}, onMoreClicked = {})
}