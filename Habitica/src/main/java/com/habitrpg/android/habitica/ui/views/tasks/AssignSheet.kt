package com.habitrpg.android.habitica.ui.views.tasks

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.ui.views.UserRow

@Composable
fun AssignSheet(
    members: List<Member>,
    assignedMembers: List<String>,
    configManager: AppConfigManager,
    onAssignClick: (String) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Box {
            Text(
                stringResource(R.string.assign_to),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colorResource(R.color.gray_200),
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
            )
            TextButton(
                onClick = onCloseClick,
                colors = ButtonDefaults.textButtonColors(),
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                Text(stringResource(R.string.done))
            }
        }
        for (member in members) {
            val isAssigned = assignedMembers.contains(member.id)
            AssignSheetRow(
                member = member,
                isAssigned = isAssigned,
                configManager,
                onAssignClick = onAssignClick,
            )
        }
    }
}

@Composable
fun AssignSheetRow(
    member: Member,
    isAssigned: Boolean,
    configManager: AppConfigManager,
    onAssignClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    UserRow(
        username = member.displayName,
        avatar = member,
        color = colorResource(R.color.text_primary),
        extraContent = {
            Text(
                member.formattedUsername ?: "",
                color = colorResource(R.color.text_ternary),
            )
        },
        endContent = {
            IsAssignedIndicator(isAssigned = isAssigned)
        },
        configManager = configManager,
        modifier =
            modifier
                .clickable {
                    onAssignClick(member.id)
                }
                .padding(30.dp, 12.dp)
                .heightIn(min = 24.dp)
                .fillMaxWidth(),
    )
}

@Composable
private fun IsAssignedIndicator(
    isAssigned: Boolean,
    modifier: Modifier = Modifier,
) {
    val transition = updateTransition(isAssigned, label = "isAssigned")
    val rotation =
        transition.animateFloat(
            label = "isAssigned",
            transitionSpec = { spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow) },
        ) {
            if (it) 0f else 135f
        }
    val backgroundColor =
        transition.animateColor(
            label = "isAssigned",
            transitionSpec = { tween(450, easing = FastOutLinearInEasing) },
        ) {
            if (it) MaterialTheme.colorScheme.primary else colorResource(id = R.color.transparent)
        }
    val color =
        transition.animateColor(
            label = "isAssigned",
            transitionSpec = { tween(450, easing = FastOutLinearInEasing) },
        ) {
            colorResource(if (it) R.color.white else R.color.text_dimmed)
        }
    val borderColor =
        transition.animateColor(
            label = "isAssigned",
            transitionSpec = { tween(450, easing = FastOutLinearInEasing) },
        ) {
            if (it) MaterialTheme.colorScheme.primary else colorResource(id = R.color.text_dimmed)
        }
    Image(
        painterResource(R.drawable.ic_close_white_24dp),
        null,
        colorFilter = ColorFilter.tint(color.value),
        modifier =
            modifier
                .rotate(rotation.value)
                .size(24.dp)
                .background(
                    backgroundColor.value,
                    CircleShape,
                )
                .border(
                    2.dp,
                    borderColor.value,
                    CircleShape,
                )
                .padding(3.dp),
    )
}

@Composable
@Preview
private fun IsAssignedIndicatorPreview() {
    val isAssigned = remember { mutableStateOf(false) }
    Column {
        IsAssignedIndicator(isAssigned = isAssigned.value)
        IsAssignedIndicator(isAssigned = !isAssigned.value)
    }
}
