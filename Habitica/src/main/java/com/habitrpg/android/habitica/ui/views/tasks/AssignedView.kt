package com.habitrpg.android.habitica.ui.views.tasks

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.Assignable
import com.habitrpg.android.habitica.ui.theme.HabiticaTheme
import com.habitrpg.android.habitica.ui.views.CompletedAt
import com.habitrpg.android.habitica.ui.views.UserRow
import java.util.Date

@Composable
fun AssignedView(
    assigned: List<Assignable>,
    completedAt: Map<String, Date>,
    backgroundColor: Color,
    color: Color,
    onEditClick: () -> Unit,
    onUndoClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    showEditButton: Boolean = false
) {
    Column(modifier.fillMaxWidth()) {
        val rowModifier = Modifier
            .padding(vertical = 4.dp)
            .background(
                backgroundColor,
                MaterialTheme.shapes.medium
            )
            .heightIn(min = 66.dp)
            .padding(start = 16.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
        for (assignable in assigned) {
            UserRow(
                username = assignable.identifiableName,
                avatar = assignable.avatar,
                modifier = rowModifier,
                mainContentModifier = Modifier
                    .padding(vertical = 12.dp)
                    .heightIn(min = 24.dp),
                color = color,
                extraContent = {
                    completedAt[assignable.id]?.let { CompletedAt(completedAt = it) }
                },
                endContent = {
                    completedAt[assignable.id]?.let {
                        if (showEditButton) {
                            UndoTaskCompletion(
                                Modifier.clickable {
                                    assignable.id?.let { it1 -> onUndoClick(it1) }
                                }
                            )
                        }
                    }
                }
            )
        }
        if (showEditButton) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable {
                        onEditClick()
                    }
                    .padding(vertical = 4.dp)
                    .background(
                        backgroundColor,
                        MaterialTheme.shapes.medium
                    )
                    .padding(15.dp, 12.dp)
                    .heightIn(min = 24.dp)
                    .fillMaxWidth()
            ) {
                Image(
                    painterResource(R.drawable.edit),
                    null,
                    colorFilter = ColorFilter.tint(color)
                )
                Text(
                    stringResource(R.string.edit_assignees),
                    color = color,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
fun UndoTaskCompletion(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .width(51.dp)
            .heightIn(min = 66.dp)
            .fillMaxHeight()
            .background(HabiticaTheme.colors.contentBackgroundOffset)
    ) {
        Image(
            painterResource(R.drawable.checkmark),
            null,
            contentScale = ContentScale.None,
            modifier = Modifier
                .size(24.dp)
                .background(HabiticaTheme.colors.windowBackground, HabiticaTheme.shapes.small)
        )
        Text(
            stringResource(R.string.undo),
            fontSize = 12.sp,
            color = HabiticaTheme.colors.textSecondary
        )
    }
}
