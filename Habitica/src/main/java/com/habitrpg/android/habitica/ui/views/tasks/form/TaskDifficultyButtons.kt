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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.theme.HabiticaTheme
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.extensions.nameRes
import com.habitrpg.shared.habitica.models.tasks.TaskDifficulty

@Composable
fun TaskDifficultySelector(
    selected: TaskDifficulty,
    onSelect: (TaskDifficulty) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = modifier.fillMaxWidth()) {
        for (difficulty in TaskDifficulty.values())
            TaskDifficultySelection(
                value = difficulty,
                selected = selected == difficulty,
                icon = HabiticaIconsHelper.imageOfTaskDifficultyStars(
                    colorResource(R.color.white).toArgb(), difficulty.value, true
                ).asImageBitmap(),
                text = stringResource(difficulty.nameRes),
                onSelect = onSelect
            )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun TaskDifficultySelection(
    value: TaskDifficulty,
    selected: Boolean,
    icon: ImageBitmap,
    text: String,
    onSelect: (TaskDifficulty) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedState = updateTransition(selected)
    val context = LocalContext.current
    val textColor = selectedState.animateColor {
        if (it) HabiticaTheme.colors.tintedUiDetails else Color(context.getThemeColor(R.attr.textColorTintedSecondary))
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp), modifier = modifier) {
        Box(
            contentAlignment = Alignment.Center, modifier = Modifier
                .size(57.dp)
                .background(
                    Color(
                        LocalContext.current.getThemeColor(R.attr.colorTintedBackgroundOffset)
                    ), MaterialTheme.shapes.medium
                )
                .clip(MaterialTheme.shapes.medium)
                .clickable { onSelect(value) }
        ) {
            this@Column.AnimatedVisibility(
                selected,
                enter = scaleIn(spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium)),
                exit = scaleOut(spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium))
            ) {
                Box(
                    Modifier
                        .size(57.dp)
                        .background(HabiticaTheme.colors.tintedUiMain, MaterialTheme.shapes.medium)
                )
            }
            Image(icon, null, colorFilter = ColorFilter.tint(HabiticaTheme.colors.tintedUiDetails))
        }
        Text(
            text,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            fontSize = 14.sp,
            color = textColor.value
        )
    }
}

private class DifficultyProvider : PreviewParameterProvider<TaskDifficulty> {
    override val values = TaskDifficulty.values().asSequence()
}

@Preview
@Composable
private fun TaskDifficultySelectorPreview(@PreviewParameter(DifficultyProvider::class) difficulty: TaskDifficulty) {
    val selected = remember { mutableStateOf(difficulty) }
    TaskDifficultySelector(selected.value, { selected.value = it }, Modifier.width(300.dp))
}