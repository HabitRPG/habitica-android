package com.habitrpg.android.habitica.ui.views

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

object WobblyCircle : Shape {
    override fun createOutline(
        size: Size, layoutDirection: LayoutDirection, density: Density
    ): Outline {
        val path = Path().apply {
            addOval(Rect(Offset(0f, -size.height / 0.4f),
                Size(size.width * 1.8f, size.height * 1.6f)))
        }

        return Outline.Generic(path)
    }
}
