package com.habitrpg.android.habitica.ui.views

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.viewinterop.AndroidView
import com.habitrpg.common.habitica.views.AvatarView
import com.habitrpg.shared.habitica.models.Avatar

object AvatarCircleShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline =
        CircleShape.createOutline(size, 20f, 20f, 20f, 20f, layoutDirection)
}

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