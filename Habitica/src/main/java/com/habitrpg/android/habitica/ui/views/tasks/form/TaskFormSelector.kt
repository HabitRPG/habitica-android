package com.habitrpg.android.habitica.ui.views.tasks.form

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.theme.colors
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.theme.HabiticaTheme

data class LabeledValue<V>(val label: String, val value: V)

@Composable
fun <V> TaskFormSelector(
    selected: V,
    values: List<LabeledValue<V>>,
    onSelect: (V) -> Unit,
    modifier: Modifier = Modifier,
    columnSize: Int = 2,
    spacing: Dp = 10.dp,
) {
    Column(verticalArrangement = Arrangement.spacedBy(spacing), modifier = modifier) {
        for (row in values.chunked(columnSize)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing),
            ) {
                for (value in row)
                    TaskFormSelection(
                        value = value.value,
                        selected = selected == value.value,
                        text = value.label,
                        onSelect = onSelect,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                    )
            }
        }
    }
}

@Composable
private fun <V> TaskFormSelection(
    value: V,
    selected: Boolean,
    text: String,
    onSelect: (V) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedState = updateTransition(selected)
    val context = LocalContext.current
    val textColor =
        selectedState.animateColor {
            if (it) HabiticaTheme.colors.tintedUiDetails else Color(context.getThemeColor(R.attr.textColorTintedSecondary))
        }
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .background(
                    Color(
                        LocalContext.current.getThemeColor(R.attr.colorTintedBackgroundOffset),
                    ),
                    MaterialTheme.shapes.medium,
                )
                .clip(MaterialTheme.shapes.medium)
                .clickable { onSelect(value) },
    ) {
        AnimatedVisibility(
            selected,
            enter = scaleIn(spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium)),
            exit = scaleOut(spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium)),
            modifier = Modifier.matchParentSize(),
        ) {
            Box(
                Modifier
                    .background(HabiticaTheme.colors.tintedUiMain, MaterialTheme.shapes.medium)
                    .matchParentSize(),
            )
        }
        Text(
            text,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            fontSize = 16.sp,
            color = textColor.value,
            modifier = Modifier.padding(15.dp),
        )
    }
}

@Preview
@Composable
private fun TaskFormSelectorPreview() {
    val selected = remember { mutableStateOf("second") }
    TaskFormSelector(
        selected.value,
        listOf(
            LabeledValue("First", "first"),
            LabeledValue("Second", "second"),
            LabeledValue("Third", "third"),
            LabeledValue("Fourth", "fourth"),
            LabeledValue("Fifth", "Fifth"),
            LabeledValue("Sixth", "sixth"),
        ),
        { selected.value = it },
        Modifier.width(300.dp),
    )
}
