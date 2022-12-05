package com.habitrpg.android.habitica.ui.views.tasks

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.Assignable
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
            .padding(15.dp, 12.dp)
            .heightIn(min = 24.dp)
            .fillMaxWidth()
        for (assignable in assigned) {
            UserRow(
                username = assignable.identifiableName, modifier = rowModifier,
                color = color,
                extraContent = {
                    completedAt[assignable.id]?.let { CompletedAt(completedAt = it) }
                }
            )
        }
        if (showEditButton) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
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
                .fillMaxWidth()) {
                Image(
                    painterResource(R.drawable.edit),
                    null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colors.primary)
                )
                Text(
                    stringResource(R.string.edit_assignees), color = color,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}