package com.habitrpg.android.habitica.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun SegmentedControl(
    items: List<String>,
    defaultSelectedItemIndex: Int = 0,
    useFixedWidth: Boolean = false,
    itemWidth: Dp = 120.dp,
    cornerRadius: Int = 10,
    onItemSelection: (selectedItemIndex: Int) -> Unit,
) {
    val selectedIndex = remember { mutableIntStateOf(defaultSelectedItemIndex) }
    val color = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier,
    ) {
        items.forEachIndexed { index, item ->
            OutlinedButton(
                modifier =
                    when (index) {
                        0 -> {
                            if (useFixedWidth) {
                                Modifier
                                    .width(itemWidth)
                                    .offset(0.dp, 0.dp)
                                    .zIndex(if (selectedIndex.intValue == 0) 1f else 0f)
                            } else {
                                Modifier
                                    .wrapContentSize()
                                    .offset(0.dp, 0.dp)
                                    .zIndex(if (selectedIndex.intValue == 0) 1f else 0f)
                            }
                        }

                        else -> {
                            if (useFixedWidth) {
                                Modifier
                                    .width(itemWidth)
                                    .offset((-1 * index).dp, 0.dp)
                                    .zIndex(if (selectedIndex.intValue == index) 1f else 0f)
                            } else {
                                Modifier
                                    .wrapContentSize()
                                    .offset((-1 * index).dp, 0.dp)
                                    .zIndex(if (selectedIndex.intValue == index) 1f else 0f)
                            }
                        }
                    },
                onClick = {
                    selectedIndex.intValue = index
                    onItemSelection(selectedIndex.intValue)
                },
                shape =
                    when (index) {
                        0 -> {
                            RoundedCornerShape(
                                topStartPercent = cornerRadius,
                                topEndPercent = 0,
                                bottomStartPercent = cornerRadius,
                                bottomEndPercent = 0,
                            )
                        }

                        items.size - 1 -> {
                            RoundedCornerShape(
                                topStartPercent = 0,
                                topEndPercent = cornerRadius,
                                bottomStartPercent = 0,
                                bottomEndPercent = cornerRadius,
                            )
                        }

                        else -> {
                            RoundedCornerShape(
                                topStartPercent = 0,
                                topEndPercent = 0,
                                bottomStartPercent = 0,
                                bottomEndPercent = 0,
                            )
                        }
                    },
                border =
                    BorderStroke(
                        1.dp,
                        if (selectedIndex.intValue == index) {
                            color
                        } else {
                            color.copy(alpha = 0.75f)
                        },
                    ),
                colors =
                    if (selectedIndex.intValue == index) {
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = color,
                        )
                    } else {
                        ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)
                    },
            ) {
                Text(
                    text = item,
                    fontWeight = FontWeight.Normal,
                    color =
                        if (selectedIndex.intValue == index) {
                            Color.White
                        } else {
                            color.copy(alpha = 0.9f)
                        },
                )
            }
        }
    }
}
