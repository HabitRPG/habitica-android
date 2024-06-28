package com.habitrpg.android.habitica.ui.views.stable

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.habitrpg.android.habitica.models.inventory.Mount
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.views.PixelArtView

class MountView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    val hasLoadedImages: Boolean
        get() {
            return bodyView.bitmap != null && headView.bitmap != null
        }
    private val bodyView: PixelArtView = PixelArtView(context)
    private val headView: PixelArtView = PixelArtView(context)

    fun setMount(key: String) {
        bodyView.loadImage("Mount_Body_$key")
        headView.loadImage("Mount_Head_$key")
    }

    init {
        addView(bodyView)
        bodyView.layoutParams =
            LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        addView(headView)
        headView.layoutParams =
            LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
}

@Composable
fun MountView(
    mount: Mount,
    modifier: Modifier = Modifier
) {
    MountView(mount.key, modifier)
}

@Composable
fun MountView(
    mountKey: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MountView(context)
        },
        update = { view ->
            view.setMount(mountKey)
        }
    )
}
