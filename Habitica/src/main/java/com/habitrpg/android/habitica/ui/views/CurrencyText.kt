package com.habitrpg.android.habitica.ui.views

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.helpers.NumberAbbreviator

@Composable
fun CurrencyText(
    currency: String,
    value: Double,
    modifier: Modifier = Modifier,
    decimals: Int = 0,
    minForAbbrevation: Int = 0,
    animated: Boolean = true
) {
    val animatedValue = if (animated) animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    ).value else value.toFloat()
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        when (currency) {
            "gold" -> HabiticaIconsHelper.imageOfGold()
            "gems" -> HabiticaIconsHelper.imageOfGem()
            "hourglasses" -> HabiticaIconsHelper.imageOfHourglass()
            else -> null
        }?.asImageBitmap()?.let { Image(it, null, Modifier.padding(end = 5.dp)) }
        Text(
            NumberAbbreviator.abbreviate(null, animatedValue, decimals, minForAbbrevation),
            color = when (currency) {
                "gold" -> colorResource(R.color.text_gold)
                "gems" -> colorResource(R.color.text_green)
                "hourglasses" -> colorResource(R.color.text_brand)
                else -> colorResource(R.color.text_primary)
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}