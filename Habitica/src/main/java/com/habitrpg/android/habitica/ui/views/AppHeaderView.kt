package com.habitrpg.android.habitica.ui.views

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel

@Composable
fun UserLevelText(user: User) {
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

@Composable
fun AppHeaderView(
    viewModel: MainUserViewModel,
) {
    val user by viewModel.user.observeAsState(null)
    val teamPlan by viewModel.currentTeamPlan.collectAsState(null)
    val teamPlanMembers by viewModel.currentTeamPlanMembers.observeAsState()
    Column {
        Row {
            ComposableAvatarView(
                user,
                Modifier
                    .size(110.dp, 100.dp)
                    .padding(end = 16.dp)
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
                            modifier = Modifier.weight(1f)
                        )
                        if (user?.hasClass == true) {
                            LabeledBar(
                                icon = HabiticaIconsHelper.imageOfMagic(),
                                label = stringResource(R.string.MP_default),
                                color = colorResource(R.color.mpColor),
                                value = user?.stats?.mp ?: 0.0,
                                maxValue = user?.stats?.maxMP?.toDouble() ?: 0.0,
                                displayCompact = teamPlan != null,
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
                                        bundleOf("groupID" to teamPlan?.id)
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
                        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .height(40.dp)
                            .width(72.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(
                                colorResource(R.color.window_background)
                            )
                            .clickable {
                                MainNavigationController.navigate(
                                    R.id.guildFragment,
                                    bundleOf("groupID" to teamPlan?.id)
                                )
                            }
                    ) {
                        for (member in teamPlanMembers?.filter { it.id != user?.id }?.take(6) ?: emptyList()) {
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (user?.hasClass == true) {
                val icon = when (user?.stats?.habitClass) {
                    "warrior" -> HabiticaIconsHelper.imageOfWarriorLightBg().asImageBitmap()
                    "wizard" -> HabiticaIconsHelper.imageOfMageLightBg().asImageBitmap()
                    "healer" -> HabiticaIconsHelper.imageOfHealerLightBg().asImageBitmap()
                    "rogue" -> HabiticaIconsHelper.imageOfRogueLightBg().asImageBitmap()
                    else -> null
                }
                if (icon != null) {
                    Image(bitmap = icon, "", modifier = Modifier.padding(end = 4.dp))
                }
            }
            user?.let { UserLevelText(it) }
            Spacer(Modifier.weight(1f))
            user?.hourglassCount?.toDouble()
                ?.let { CurrencyText("hourglasses", it, modifier = Modifier.padding(end = 12.dp)) }
            CurrencyText("gold", user?.stats?.gp ?: 0.0, modifier = Modifier.padding(end = 12.dp))
            CurrencyText("gems", user?.gemCount?.toDouble() ?: 0.0)
        }
    }
}