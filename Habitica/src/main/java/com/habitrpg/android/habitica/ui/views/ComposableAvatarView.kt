package com.habitrpg.android.habitica.ui.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.habitrpg.common.habitica.views.AvatarView
import com.habitrpg.shared.habitica.models.Avatar

@Composable
fun ComposableAvatarView(
    avatar: Avatar?,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier, // Occupy the max size in the Compose UI tree
        factory = { context ->
            AvatarView(context)
        },
        update = { view ->
            if (avatar != null) {
                view.setAvatar(avatar)
            }
        }
    )
}