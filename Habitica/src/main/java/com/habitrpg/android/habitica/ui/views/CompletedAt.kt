package com.habitrpg.android.habitica.ui.views

import android.text.format.DateUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import java.text.DateFormat
import java.util.Date

val completedTimeFormatToday: DateFormat = DateFormat.getTimeInstance(DateFormat.SHORT)
val completedTimeFormat: DateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

@Composable
fun CompletedAt(
    completedAt: Date?,
) {
    val completedToday = completedAt?.time?.let { DateUtils.isToday(it) } ?: false
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Image(painterResource(R.drawable.completed), null)
        Text(stringResource(R.string.completed_at,
            completedAt?.let { if (completedToday) completedTimeFormatToday.format(it) else completedTimeFormat.format(it) }
                ?: ""),
            fontSize = 14.sp,
            color = if (completedToday) colorResource(R.color.green_10) else colorResource(R.color.text_secondary),
            modifier = Modifier.padding(start = 4.dp))
    }
}