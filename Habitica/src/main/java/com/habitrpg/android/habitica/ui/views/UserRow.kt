package com.habitrpg.android.habitica.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.common.habitica.views.ComposableAvatarView
import com.habitrpg.shared.habitica.models.Avatar

@Composable
fun UserRow(
    username: String,
    avatar: Avatar?,
    modifier: Modifier = Modifier,
    mainContentModifier: Modifier = Modifier,
    extraContent: @Composable (() -> Unit)? = null,
    endContent: @Composable (() -> Unit)? = null,
    color: Color? = null,
    configManager: AppConfigManager
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        Box(
            modifier =
            Modifier
                .padding(end = 12.dp)
                .clip(CircleShape)
                .size(40.dp)
                .padding(
                    end = 12.dp,
                    top = if (avatar?.currentMount?.isNotBlank() == true) 24.dp else 12.dp
                )
        ) {
            if (avatar != null) {
                ComposableAvatarView(
                    avatar = avatar,
                    configManager,
                    Modifier
                        .size(96.dp)
                        .requiredSize(96.dp)
                )
            }
        }

        Column(mainContentModifier) {
            Text(
                "@$username",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = color ?: MaterialTheme.colorScheme.primary
            )
            if (extraContent != null) {
                extraContent()
            }
        }
        Spacer(Modifier.weight(1f))
        if (endContent != null) {
            endContent()
        }
    }
}
