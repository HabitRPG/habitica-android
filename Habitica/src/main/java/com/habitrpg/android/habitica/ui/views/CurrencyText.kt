package com.habitrpg.android.habitica.ui.views

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.helpers.NumberAbbreviator

@Composable
fun CurrencyText(
    currency: String,
    value: Int,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 12.sp,
    decimals: Int = 0,
    minForAbbreviation: Int = 0,
    animated: Boolean = true
) {
    CurrencyText(
        currency = currency,
        value = value.toDouble(),
        modifier,
        fontSize,
        decimals,
        minForAbbreviation,
        animated
    )
}

@Composable
fun CurrencyText(
    currency: String,
    value: Double,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 12.sp,
    decimals: Int = 0,
    minForAbbreviation: Int = 0,
    animated: Boolean = true
) {
    val animatedValue =
        if (animated) {
            animateFloatAsState(
                targetValue = value.toFloat(),
                animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
            ).value
        } else {
            value.toFloat()
        }
    val currencyName = when (currency) {
        "gold" -> stringResource(R.string.gold_plural)
        "gems" -> stringResource(R.string.gems)
        "hourglasses" -> stringResource(R.string.mystic_hourglasses)
        else -> ""
    }
    val amount = NumberAbbreviator.abbreviate(null, animatedValue, decimals, minForAbbreviation)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.semantics(mergeDescendants = true) {
        contentDescription = "$amount $currencyName"
    }) {
        when (currency) {
            "gold" -> HabiticaIconsHelper.imageOfGold()
            "gems" -> HabiticaIconsHelper.imageOfGem()
            "hourglasses" -> HabiticaIconsHelper.imageOfHourglass()
            else -> null
        }?.asImageBitmap()?.let { Image(it, null, Modifier.padding(end = 5.dp)) }
        Text(
            text = amount,
            color =
                when (currency) {
                    "gold" -> colorResource(R.color.text_gold)
                    "gems" -> colorResource(R.color.text_green)
                    "hourglasses" -> colorResource(R.color.text_brand)
                    else -> colorResource(R.color.text_primary)
                },
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold
        )
    }
}
