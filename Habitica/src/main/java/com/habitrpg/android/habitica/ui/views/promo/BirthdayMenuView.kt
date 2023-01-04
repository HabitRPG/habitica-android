package com.habitrpg.android.habitica.ui.views.promo

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.MainNavigationController

@Composable
fun BirthdayBanner() {
    Column(
        Modifier
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
                    stringResource(R.string.ends_in_x).uppercase(),
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