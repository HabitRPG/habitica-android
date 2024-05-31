package com.habitrpg.common.habitica.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.themeadapter.material3.createMdc3Theme

@Composable
fun HabiticaTheme(
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val (colors, _, _) =
        createMdc3Theme(
            context = context,
            layoutDirection = layoutDirection,
            setTextColors = true,
        )
    MaterialTheme(
        colorScheme = colors ?: MaterialTheme.colorScheme,
        typography =
            Typography(
                displayLarge =
                    TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                        letterSpacing = (0.05).sp,
                    ),
                displayMedium =
                    TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 28.sp,
                        letterSpacing = (0.05).sp,
                    ),
                titleLarge =
                    TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                    ),
                titleMedium =
                    TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        letterSpacing = 0.1.sp,
                    ),
                titleSmall =
                    TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    ),
                bodyLarge =
                    TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        letterSpacing = 0.35.sp,
                        lineHeight = 16.sp,
                    ),
                bodyMedium =
                    TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        letterSpacing = 0.2.sp,
                        lineHeight = 16.sp,
                    ),
                labelMedium =
                    TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        letterSpacing = 1.25.sp,
                    ),
                labelSmall =
                    TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    ),
            ),
        shapes =
            Shapes(
                RoundedCornerShape(4.dp),
                RoundedCornerShape(8.dp),
                RoundedCornerShape(12.dp),
            ),
        content = content,
    )
}

val Typography.caption1
    get() = labelMedium
val Typography.caption2
    get() =
        TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            letterSpacing = 0.4.sp,
        )
val Typography.caption3
    get() =
        TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            letterSpacing = 0.3.sp,
            lineHeight = 14.sp,
        )
val Typography.caption4
    get() =
        TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            letterSpacing = 0.35.sp,
        )
val Typography.subtitle3
    get() =
        TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            letterSpacing = 0.15.sp,
        )

object HabiticaTheme {
    val typography: Typography
        @Composable
        get() = MaterialTheme.typography

    val shapes: Shapes
        @Composable
        get() = MaterialTheme.shapes
}

class HabiticaColors(
    val windowBackground: Color,
    val contentBackground: Color,
    val contentBackgroundOffset: Color,
    val offsetBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textQuad: Color,
    val textDimmed: Color,
    val tintedUiMain: Color,
    val tintedUiSub: Color,
    val tintedUiDetails: Color,
    val pixelArtBackground: Color,
    val errorBackground: Color,
    val errorColor: Color,
    val successBackground: Color,
    val successColor: Color,
)

class HabiticaTypography
