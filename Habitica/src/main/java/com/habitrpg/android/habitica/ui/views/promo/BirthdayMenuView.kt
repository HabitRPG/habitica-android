package com.habitrpg.android.habitica.ui.views.promo

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.getShortRemainingString
import com.habitrpg.android.habitica.helpers.MainNavigationController
import java.util.Date

@Composable
fun BirthdayBanner(endDate: Date, modifier: Modifier = Modifier) {
    var value by remember { mutableStateOf(0) }

    DisposableEffect(Unit) {
        val handler = Handler(Looper.getMainLooper())

        val runnable = {
            value += 1
        }

        handler.postDelayed(runnable, 1000)

        onDispose {
            handler.removeCallbacks(runnable)
        }
    }
    Column(
        modifier
            .fillMaxWidth()
            .clickable {
                MainNavigationController.navigate(R.id.birthdayActivity)
            }
    ) {
        Column(Modifier.fillMaxWidth()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
                modifier = Modifier
                    .height(67.dp)
                    .fillMaxWidth()
                    .background(colorResource(R.color.brand_100))
                    .padding(start = 10.dp)) {
                Image(
                    painterResource(R.drawable.birthday_menu_text), null
                )
                Text(
                    stringResource(R.string.exclusive_items_await),
                    color = colorResource(R.color.yellow_100),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(33.dp)
                    .background(colorResource(R.color.brand_300))
                    .padding(horizontal = 10.dp)
            ) {
                Text(
                    stringResource(R.string.ends_in_x, endDate.getShortRemainingString()).uppercase(),
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