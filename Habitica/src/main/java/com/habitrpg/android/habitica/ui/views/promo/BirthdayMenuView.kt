package com.habitrpg.android.habitica.ui.views.promo

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.getShortRemainingString
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.ui.views.PixelArtView
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import java.util.Date
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun BirthdayBanner(endDate: Date, modifier: Modifier = Modifier) {
    if (endDate.before(Date())) {
        return
    }
    Column(
        modifier
            .fillMaxWidth()
            .clickable {
                MainNavigationController.navigate(R.id.birthdayActivity)
            }
    ) {
        Column(Modifier.fillMaxWidth()) {
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .height(67.dp)
                    .fillMaxWidth()
                    .background(colorResource(R.color.brand_100))
            ) {
                Row(Modifier
                    .align(Alignment.CenterEnd)) {
                    Image(
                        painterResource(R.drawable.birthday_menu_gems),
                        null,
                        modifier = Modifier
                            .align(Alignment.Top)
                            .offset((40).dp)
                    )
                    PixelArtView(
                        imageName = "stable_Pet-Gryphatrice-Jubilant",
                        Modifier
                            .requiredSize(104.dp)
                            .scale(-1f, 1f)
                            .offset((-30).dp)
                    )
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(
                        2.dp,
                        Alignment.CenterVertically
                    ),
                    modifier = Modifier
                        .padding(start = 8.dp)
                ) {
                    Image(
                        painterResource(R.drawable.birthday_menu_text), null
                    )
                    Text(
                        stringResource(R.string.exclusive_items_await),
                        color = colorResource(R.color.yellow_100),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(33.dp)
                    .background(colorResource(R.color.brand_300))
                    .padding(horizontal = 10.dp)
            ) {
                TimeRemainingText(
                    endDate,
                    R.string.ends_in_x,
                    color = colorResource(R.color.yellow_50),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    stringResource(R.string.see_more).uppercase(),
                    color = colorResource(R.color.white),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TimeRemainingText(
    endDate: Date,
    formatString: Int,
    color: Color,
    fontSize: TextUnit,
    fontWeight: FontWeight
) {
    var value by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        val diff = endDate.time - Date().time
        if (diff.milliseconds > 1.hours) {
            delay(1.minutes)
        } else if (diff < 0) {
            this.cancel()
        } else {
            delay(1.seconds)
        }
        value += 1
    }
    Text(
        stringResource(
            formatString,
            endDate.getShortRemainingString()
        ).uppercase(),
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight
    )
}