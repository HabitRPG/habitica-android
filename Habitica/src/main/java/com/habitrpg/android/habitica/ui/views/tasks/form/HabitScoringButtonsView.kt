package com.habitrpg.android.habitica.ui.views.tasks.form

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.theme.colors
import com.habitrpg.common.habitica.theme.HabiticaTheme
import com.habitrpg.common.habitica.extensions.getThemeColor

@Composable
fun HabitScoringSelector(
    selectedUp: Boolean,
    selectedDown: Boolean,
    onSelectUp: () -> Unit,
    onSelectDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterHorizontally),
        modifier = modifier
    ) {
        HabitScoringSelection(
            selected = selectedUp,
            icon = painterResource(id = R.drawable.habit_plus),
            text = stringResource(R.string.positive_habit_form),
            onSelect = onSelectUp
        )
        HabitScoringSelection(
            selected = selectedDown,
            icon = painterResource(id = R.drawable.habit_minus),
            text = stringResource(R.string.negative_habit_form),
            onSelect = onSelectDown
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun HabitScoringSelection(
    selected: Boolean,
    icon: Painter,
    text: String,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedState = updateTransition(selected)
    val context = LocalContext.current

    val borderColor = selectedState.animateColor {
        if (it) HabiticaTheme.colors.tintedUiMain else Color(context.getThemeColor(R.attr.textColorTintedSecondary))
    }
    val iconColor = selectedState.animateColor {
        if (it) HabiticaTheme.colors.tintedUiDetails else Color(context.getThemeColor(R.attr.textColorTintedSecondary))
    }
    val textColor = selectedState.animateColor {
        if (it) Color(context.getThemeColor(R.attr.textColorTintedPrimary)) else Color(context.getThemeColor(R.attr.textColorTintedSecondary))
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .size(34.dp)
                .border(
                    1.dp,
                    borderColor.value,
                    CircleShape
                )
                .clip(CircleShape)
                .clickable { onSelect() }
        ) {
            this@Column.AnimatedVisibility(
                selected,
                enter = scaleIn(spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium)),
                exit = scaleOut(spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
            ) {
                Box(
                    Modifier
                        .size(32.dp)
                        .background(HabiticaTheme.colors.tintedUiMain, CircleShape)
                )
            }
            Image(icon, null, colorFilter = ColorFilter.tint(iconColor.value))
        }
        Text(
            text,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            fontSize = 14.sp,
            color = textColor.value
        )
    }
}

@Preview
@Composable
private fun Preview() {
    val selectedUp = remember { mutableStateOf(true) }
    val selectedDown = remember { mutableStateOf(false) }
    Box(
        Modifier
            .background(MaterialTheme.colors.background)
            .width(300.dp)
            .padding(8.dp)
    ) {
        HabitScoringSelector(
            selectedUp.value,
            selectedDown.value,
            { selectedUp.value = !selectedUp.value },
            { selectedDown.value = !selectedDown.value },
            Modifier.align(Alignment.Center)
        )
    }
}
