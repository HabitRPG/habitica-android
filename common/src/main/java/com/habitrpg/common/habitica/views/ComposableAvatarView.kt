package com.habitrpg.common.habitica.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.habitrpg.shared.habitica.models.Avatar

@Composable
fun ComposableAvatarView(
    avatar: Avatar?,
    modifier: Modifier = Modifier,
    showBackground: Boolean = true,
    showPet: Boolean = true,
    showMount: Boolean = true,
    showSleeping: Boolean = true,
) {
    AndroidView(
        modifier = modifier, // Occupy the max size in the Compose UI tree
        factory = { context ->
            AvatarView(context, showBackground, showPet, showMount, showSleeping)
        },
        update = { view ->
            if (avatar != null) {
                view.setAvatar(avatar)
            }
        },
    )
}
