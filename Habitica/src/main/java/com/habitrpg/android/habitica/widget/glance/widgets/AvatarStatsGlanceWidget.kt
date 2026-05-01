package com.habitrpg.android.habitica.widget.glance.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.widget.glance.actions.openAppAction
import com.habitrpg.android.habitica.widget.glance.components.ValueBar
import com.habitrpg.android.habitica.widget.glance.data.AvatarBitmapCache
import com.habitrpg.android.habitica.widget.glance.data.StatsWidgetState
import com.habitrpg.android.habitica.widget.glance.data.widgetEntryPoint
import com.habitrpg.android.habitica.widget.glance.theme.HabiticaWidgetTheme
import com.habitrpg.android.habitica.widget.glance.theme.WidgetBarColors
import com.habitrpg.android.habitica.widget.glance.theme.WidgetColors
import kotlinx.coroutines.flow.firstOrNull

class AvatarStatsGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(140.dp, 80.dp),
            DpSize(220.dp, 120.dp),
            DpSize(300.dp, 200.dp),
        ),
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val user = widgetEntryPoint(context).userRepository().getUser().firstOrNull()
        AvatarBitmapCache.refreshIfNeeded(context, user)
        val state = StatsWidgetState.fromUser(
            context = context,
            user = user,
            avatarBitmapPath = AvatarBitmapCache.cachedFile(context).absolutePath,
        )
        provideContent {
            HabiticaWidgetTheme {
                StatsContent(state)
            }
        }
    }
}

@Composable
private fun StatsContent(state: StatsWidgetState) {
    val size = LocalSize.current
    val showAvatar = size.width >= 220.dp
    val showLabels = size.width >= 220.dp
    val showFooter = size.height >= 100.dp

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetColors.background)
            .padding(12.dp)
            .clickable(onClick = openAppAction()),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showAvatar) {
                AvatarImage(state)
                Spacer(GlanceModifier.width(12.dp))
            }
            Column(modifier = GlanceModifier.defaultWeight()) {
                ValueBar(
                    title = "Health",
                    value = state.hp,
                    maxValue = state.maxHp,
                    barColor = WidgetBarColors.red,
                    iconResId = R.drawable.widget_icon_heart,
                    showLabels = showLabels,
                )
                Spacer(GlanceModifier.height(if (showLabels) 14.dp else 8.dp))
                ValueBar(
                    title = "Experience",
                    value = state.exp,
                    maxValue = state.toNextLevel,
                    barColor = WidgetBarColors.yellow,
                    iconResId = R.drawable.widget_icon_experience,
                    showLabels = showLabels,
                )
                Spacer(GlanceModifier.height(if (showLabels) 14.dp else 8.dp))
                ValueBar(
                    title = "Mana",
                    value = state.mp,
                    maxValue = state.maxMp,
                    barColor = WidgetBarColors.blue,
                    iconResId = R.drawable.widget_icon_mana,
                    showLabels = showLabels,
                )
                if (showFooter) {
                    Spacer(GlanceModifier.height(12.dp))
                    StatsFooter(state)
                }
            }
        }
    }
}

@Composable
private fun AvatarImage(state: StatsWidgetState) {
    val bitmapFile = state.avatarBitmapPath?.let { java.io.File(it) }
    val bitmap = if (bitmapFile?.exists() == true) {
        android.graphics.BitmapFactory.decodeFile(bitmapFile.absolutePath)
    } else {
        null
    }
    val avatarSize = if (LocalSize.current.width >= 300.dp) 120.dp else 80.dp
    if (bitmap != null) {
        Image(
            provider = ImageProvider(bitmap),
            contentDescription = "Avatar",
            modifier = GlanceModifier.size(avatarSize),
        )
    } else {
        Box(modifier = GlanceModifier.size(avatarSize)) {}
    }
}

@Composable
private fun StatsFooter(state: StatsWidgetState) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Lv. ${state.level}",
            style = TextStyle(
                color = WidgetColors.text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
            modifier = GlanceModifier.defaultWeight(),
        )
        Image(
            provider = ImageProvider(R.drawable.widget_icon_gold),
            contentDescription = "Gold",
            modifier = GlanceModifier.size(16.dp),
        )
        Spacer(GlanceModifier.width(4.dp))
        Text(
            text = state.goldText,
            style = TextStyle(
                color = WidgetColors.text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(GlanceModifier.width(8.dp))
        Image(
            provider = ImageProvider(R.drawable.widget_icon_gem),
            contentDescription = "Gems",
            modifier = GlanceModifier.size(16.dp),
        )
        Spacer(GlanceModifier.width(4.dp))
        Text(
            text = state.gemsText,
            style = TextStyle(
                color = WidgetColors.text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        if (state.hourglassCount > 0) {
            Spacer(GlanceModifier.width(8.dp))
            Image(
                provider = ImageProvider(R.drawable.ic_clock_24dp),
                contentDescription = "Hourglasses",
                modifier = GlanceModifier.size(16.dp),
            )
            Spacer(GlanceModifier.width(4.dp))
            Text(
                text = state.hourglassesText,
                style = TextStyle(
                    color = WidgetColors.text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}
