package com.habitrpg.android.habitica.ui.views

import android.content.res.Resources
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.primarySurface
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
import com.habitrpg.android.habitica.models.user.Flags
import com.habitrpg.android.habitica.models.user.Preferences
import com.habitrpg.android.habitica.models.user.Profile
import com.habitrpg.android.habitica.models.user.Purchases
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.SubscriptionPlan
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.theme.HabiticaTheme
import com.habitrpg.shared.habitica.models.Avatar
import kotlin.random.Random

@Composable
fun UserLevelText(user : Avatar) {
    val text = if (user.hasClass) {
        stringResource(
            id = R.string.user_level_with_class,
            user.stats?.lvl ?: 0,
            getTranslatedClassName(
                LocalContext.current.resources,
                user.stats?.habitClass
            )
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

fun getTranslatedClassName(resources : Resources, className : String?) : String {
    return when (className) {
        Stats.HEALER -> resources.getString(R.string.healer)
        Stats.ROGUE -> resources.getString(R.string.rogue)
        Stats.WARRIOR -> resources.getString(R.string.warrior)
        Stats.MAGE -> resources.getString(R.string.mage)
        else -> resources.getString(R.string.warrior)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppHeaderView(
    user : Avatar?,
    modifier : Modifier = Modifier,
    teamPlan : TeamPlan? = null,
    teamPlanMembers : List<Member>? = null,
    onMemberRowClicked : () -> Unit
) {
    Column(modifier) {
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
            val animationValue =
                animateFloatAsState(targetValue = if (teamPlan != null) 1f else 0f).value
            Box(modifier = Modifier.height(100.dp)) {
                Column(
                    Modifier.padding(
                        bottom = (animationValue * 48f).dp,
                        end = (animationValue * 80f).dp
                    )
                ) {
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
                    } else if (user?.preferences?.disableClasses != true && user?.flags?.classSelected == false) {
                        HabiticaButton(
                            background = MaterialTheme.colors.primarySurface,
                            color = MaterialTheme.colors.onPrimary,
                            onClick = {
                                MainNavigationController.navigate(R.id.classSelectionActivity)
                            },
                            contentPadding = PaddingValues(0.dp),
                            fontSize = 14.sp,
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(stringResource(R.string.choose_class))
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                val animWidth = with(LocalDensity.current) { 48.dp.roundToPx() }
                androidx.compose.animation.AnimatedVisibility(
                    visible = teamPlan != null,
                    enter = slideInHorizontally { animWidth } + fadeIn(),
                    exit = slideOutHorizontally { animWidth } + fadeOut(),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
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
                        Image(
                            painterResource(R.drawable.icon_chat), null,
                            colorFilter = ColorFilter.tint(
                                colorResource(R.color.text_ternary)
                            )
                        )
                    }
                }
                val animHeight = with(LocalDensity.current) { 40.dp.roundToPx() }
                androidx.compose.animation.AnimatedVisibility(
                    visible = teamPlan != null,
                    enter = slideInVertically { animHeight } + fadeIn(),
                    exit = slideOutVertically { animHeight } + fadeOut(),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    AnimatedContent(targetState = teamPlanMembers?.filter { it.id != user?.id },
                        transitionSpec = {
                            ContentTransform(
                                targetContentEnter =  fadeIn(animationSpec = tween(200, easing = FastOutSlowInEasing)) + slideInVertically { height -> height },
                                initialContentExit = fadeOut(animationSpec = tween(200)) + slideOutVertically { height -> -height }
                            )
                        }) {members ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(
                                12.dp,
                                Alignment.CenterHorizontally
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .width(72.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(
                                    colorResource(R.color.window_background)
                                )
                                .padding(start = 12.dp, end = 12.dp)
                                .clickable {
                                    onMemberRowClicked()
                                }
                        ) {
                            for (member in members
                                ?.sortedByDescending { it.authentication?.timestamps?.lastLoggedIn }
                                ?.take(6) ?: emptyList()) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .size(26.dp)
                                        .padding(end = 6.dp, top = 4.dp)
                                ) {
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
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.defaultMinSize(minHeight = 28.dp)
        ) {
            ClassIcon(
                className = user?.stats?.habitClass,
                hasClass = user?.hasClass ?: false,
                modifier = Modifier.padding(4.dp)
            )
            user?.let { UserLevelText(it) }
            Spacer(Modifier.weight(1f))
            if (user is User) {
                if (user.isSubscribed || user.hourglassCount > 0) {
                    CurrencyText(
                        "hourglasses",
                        user.hourglassCount.toDouble(),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable {
                                MainNavigationController.navigate(R.id.subscriptionPurchaseActivity)
                            },
                        decimals = 0
                    )
                }
                CurrencyText(
                    "gold",
                    user.stats?.gp ?: 0.0,
                    modifier = Modifier.padding(end = 12.dp),
                    decimals = 0,
                    minForAbbreviation = 10000
                )
                CurrencyText(
                    "gems",
                    user.gemCount.toDouble(),
                    modifier = Modifier.clickable {
                        MainNavigationController.navigate(R.id.gemPurchaseActivity)
                    },
                    decimals = 0
                )
            }
        }
    }
}

private class UserProvider : PreviewParameterProvider<Pair<User, TeamPlan?>> {

    private fun generateMember() : User {
        val member = User()
        member.profile = Profile()
        member.profile?.name = "User"
        member.authentication = Authentication()
        member.authentication?.localAuthentication = LocalAuthentication()
        member.authentication?.localAuthentication?.username = "username"
        member.preferences = Preferences()
        member.preferences?.disableClasses = false
        member.flags = Flags()
        member.flags?.classSelected = true
        member.purchased = Purchases()
        member.purchased?.plan = SubscriptionPlan()
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

    override val values : Sequence<Pair<User, TeamPlan?>>
        get() {
            val list = mutableListOf<Pair<User, TeamPlan?>>()
            val earlyMember = generateMember()
            earlyMember.stats?.lvl = 5
            list.add(Pair(earlyMember, null))
            val needsClass = generateMember()
            needsClass.stats?.lvl = 24
            needsClass.stats?.habitClass = "healer"
            needsClass.flags?.classSelected = false
            list.add(Pair(needsClass, null))
            val classDisabled = generateMember()
            classDisabled.stats?.lvl = 24
            classDisabled.stats?.habitClass = "rogue"
            classDisabled.preferences?.disableClasses = true
            list.add(Pair(classDisabled, null))
            val subscriber = generateMember()
            subscriber.purchased?.plan?.planId = "basic_earned"
            subscriber.purchased?.plan?.customerId = "123"
            subscriber.stats?.habitClass = "warrior"
            list.add(Pair(subscriber, null))
            val onlyHourglasses = generateMember()
            onlyHourglasses.hourglassCount = 3
            onlyHourglasses.stats?.habitClass = "wizard"
            list.add(Pair(onlyHourglasses, null))

            val teamplanUser = generateMember()
            val teamPlan = TeamPlan()
            list.add(Pair(teamplanUser, teamPlan))
            return list.asSequence()
        }
}

@Composable
@Preview
private fun Preview(@PreviewParameter(UserProvider::class) data: Pair<User, TeamPlan>) {
    HabiticaTheme {
        AppHeaderView(
            data.first,
            teamPlan = data.second,
            modifier = Modifier
                .background(HabiticaTheme.colors.contentBackground)
                .padding(8.dp)
        ) {
        }
    }
}
