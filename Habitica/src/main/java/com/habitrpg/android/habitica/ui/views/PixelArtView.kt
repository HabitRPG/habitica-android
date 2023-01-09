package com.habitrpg.android.habitica.ui.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.views.PixelArtView

@Composable
fun PixelArtView(
    imageName: String?,
    modifier: Modifier = Modifier,
    imageFormat: String? = null
) {
    AndroidView(
        modifier = modifier, // Occupy the max size in the Compose UI tree
        factory = { context ->
            PixelArtView(context)
        },
        update = { view ->
            if (imageName != null) {
                view.loadImage(imageName, imageFormat)
            } else {
                view.bitmap = null
            }
        }
    )
}