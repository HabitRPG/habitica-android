package com.habitrpg.android.habitica.ui.views

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.habitrpg.android.habitica.R

@Composable
fun ClassText(
    className: String?,
    hasClass: Boolean,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    iconSize: Dp? = null,
) {
    if (!hasClass) return
    val classColor =
        colorResource(
            when (className) {
                "warrior" -> R.color.text_red
                "wizard" -> R.color.text_blue
                "rogue" -> R.color.text_brand
                "healer" -> R.color.text_yellow
                else -> R.color.text_primary
            },
        )
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        ClassIcon(
            className = className,
            hasClass = true,
            modifier =
                Modifier.size(
                    iconSize ?: with(LocalDensity.current) {
                        fontSize.toDp()
                    },
                ),
        )
        Text(
            getTranslatedClassName(LocalContext.current.resources, className),
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold,
            color = classColor,
        )
    }
}
