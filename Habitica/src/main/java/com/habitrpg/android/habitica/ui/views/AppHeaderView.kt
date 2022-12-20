package com.habitrpg.android.habitica.ui.views

import android.content.res.Resources
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.TeamPlan
import com.habitrpg.android.habitica.models.auth.LocalAuthentication
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.user.Authentication
import com.habitrpg.android.habitica.models.user.Profile
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.shared.habitica.models.Avatar
import com.habitrpg.shared.habitica.models.AvatarStats
import kotlin.random.Random

@Composable
fun UserLevelText(user: Avatar) {
    val text = if (user.hasClass) {
        stringResource(
            id = R.string.user_level_with_class,
            user.stats?.lvl ?: 0,
            user.stats?.getTranslatedClassName(
                LocalContext.current.resources
            ) ?: ""
        )
    } else {
        stringResource(id = R.string.user_level, user.stats?.lvl ?: 0)
    }
    Text(
        text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = colorResource(R.color.text_primary)
    )
}

fun AvatarStats.getTranslatedClassName(resources: Resources): String {
        return when (habitClass) {
            Stats.HEALER -> resources.getString(R.string.healer)
            Stats.ROGUE -> resources.getString(R.string.rogue)
            Stats.WARRIOR -> resources.getString(R.string.warrior)
            Stats.MAGE -> resources.getString(R.string.mage)
            else -> resources.getString(R.string.warrior)
        }
}

@Composable
fun AppHeaderView(
    user: Avatar?,
    teamPlan: TeamPlan? = null,
    teamPlanMembers: List<Member>? = null,
    onMemberRowClicked: () -> Unit
) {
    Column {
        Row {
            ComposableAvatarView(
                user,
                Modifier
                    .size(110.dp, 100.dp)
                    .padding(end = 16.dp)
                    .clickable {
                        MainNavigationController.navigate(R.id.avatarOverviewFragment)
                    }
            )
            Column(modifier = Modifier.height(100.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.weight(1f)) {
                        LabeledBar(
                            icon = HabiticaIconsHelper.imageOfHeartLightBg(),
                            label = stringResource(R.string.HP_default),
                            color = colorResource(R.color.hpColor),
                            value = user?.stats?.hp ?: 0.0,
                            maxValue = user?.stats?.maxHealth?.toDouble() ?: 0.0,
                            displayCompact = teamPlan != null,
                            modifier = Modifier.weight(1f)
                        )
                        LabeledBar(
                            icon = HabiticaIconsHelper.imageOfExperience(),
                            label = stringResource(R.string.XP_default),
                            color = colorResource(R.color.xpColor),
                            value = user?.stats?.exp ?: 0.0,
                            maxValue = user?.stats?.toNextLevel?.toDouble() ?: 0.0,
                            displayCompact = teamPlan != null,
                            abbreviateValue = false,
                            abbreviateMax = false,
                            modifier = Modifier.weight(1f)
                        )
                        if (user?.hasClass == true) {
                            LabeledBar(
                                icon = HabiticaIconsHelper.imageOfMagic(),
                                label = stringResource(R.string.MP_default),
                                color = colorResource(R.color.mpColor),
                                value = user.stats?.mp ?: 0.0,
                                maxValue = user.stats?.maxMP?.toDouble() ?: 0.0,
                                displayCompact = teamPlan != null,
                                abbreviateValue = false,
                                abbreviateMax = false,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        MainNavigationController.navigate(R.id.skillsFragment)
                                    }
                            )
                        } else if ((user?.stats?.lvl ?: 0) < 10) {
                            LabeledBar(
                                icon = HabiticaIconsHelper.imageOfMagic(),
                                label = stringResource(R.string.unlock_level, 10),
                                color = colorResource(R.color.mpColor),
                                value = 0.0,
                                maxValue = 1.0,
                                displayCompact = teamPlan != null,
                                disabled = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    val animWidth = with(LocalDensity.current) { 48.dp.roundToPx() }
                    AnimatedVisibility(
                        visible = teamPlan != null,
                        enter = slideInHorizontally { animWidth } + fadeIn(),
                        exit = slideOutHorizontally { animWidth } + fadeOut()) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .width(72.dp)
                                .height(48.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(
                                    colorResource(R.color.window_background)
                                )
                                .clickable {
                                    MainNavigationController.navigate(
                                        R.id.guildFragment,
                                        bundleOf("groupID" to teamPlan?.id, "tabToOpen" to 1)
                                    )
                                }
                        ) {
                            Image(painterResource(R.drawable.icon_chat), null, colorFilter = ColorFilter.tint(
                                colorResource(R.color.text_ternary)))
                        }
                    }
                }
                val animHeight = with(LocalDensity.current) { 40.dp.roundToPx() }
                AnimatedVisibility(
                    visible = teamPlan != null,
                    enter = slideInVertically { animHeight } + fadeIn(),
                    exit = slideOutVertically { animHeight } + fadeOut()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .width(72.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(
                                colorResource(R.color.window_background)
                            )
                            .padding(top = 12.dp, start = 12.dp, end = 12.dp)
                            .clickable {
                                onMemberRowClicked()
                            }
                    ) {
                        for (member in teamPlanMembers?.filter { it.id != user?.id }?.sortedByDescending { it.authentication?.timestamps?.lastLoggedIn }?.take(6) ?: emptyList()) {
                            Box(modifier = Modifier
                                .clip(CircleShape)
                                .size(26.dp)
                                .padding(end = 6.dp, top = 4.dp)) {
                                ComposableAvatarView(
                                    avatar = member,
                                    Modifier
                                        .size(64.dp)
                                        .requiredSize(64.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.defaultMinSize(minHeight = 28.dp)) {
            ClassIcon(className = user?.stats?.habitClass, hasClass = user?.hasClass ?: false, modifier = Modifier.padding(4.dp))
            user?.let { UserLevelText(it) }
            Spacer(Modifier.weight(1f))
            if (user is User && user.isSubscribed) {
                CurrencyText(
                    "hourglasses",
                    user.hourglassCount.toDouble(),
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .clickable {
                            MainNavigationController.navigate(R.id.subscriptionPurchaseActivity)
                        }, decimals = 0)
            }
            CurrencyText("gold", user?.stats?.gp ?: 0.0, modifier = Modifier.padding(end = 12.dp), decimals = 0)
            CurrencyText("gems", user?.gemCount?.toDouble() ?: 0.0, modifier = Modifier.clickable {
                MainNavigationController.navigate(R.id.gemPurchaseActivity)
            }, decimals = 0)
        }
    }
}

private class UserProvider : PreviewParameterProvider<User> {
    override val values: Sequence<User>
        get() {
            val list = mutableListOf<User>()
            val member = User()
            member.profile = Profile()
            member.profile?.name = "User"
            member.authentication = Authentication()
            member.authentication?.localAuthentication = LocalAuthentication()
            member.authentication?.localAuthentication?.username = "username"
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
            return list.asSequence()
        }
}

@Composable
@Preview
private fun Preview(@PreviewParameter(UserProvider::class) user: User) {
    AppHeaderView(user) {

    }
}