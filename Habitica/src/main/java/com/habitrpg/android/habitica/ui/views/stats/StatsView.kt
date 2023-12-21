package com.habitrpg.android.habitica.ui.views.stats

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.StatsViewBinding
import com.habitrpg.common.habitica.extensions.setTintWith
import com.habitrpg.android.habitica.helpers.HapticFeedbackManager
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.extensions.layoutInflater

@Composable
fun StatsViewComposable(
    statText: String,
    statColor: Color,
    levelValue: Int,
    equipmentValue: Int,
    buffValue: Int,
    allocatedValue: Int,
    canAllocate: Boolean,
    allocateAction: () -> Unit
) {
    Column(
        Modifier
            .background(colorResource(R.color.window_background))
            .clip(MaterialTheme.shapes.large)
    ) {
        Row(
            Modifier
                .height(43.dp)
                .fillMaxWidth()
                .background(statColor)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(statText, color = colorResource(R.color.white))
            Text("${levelValue + equipmentValue + buffValue + allocatedValue}", color = colorResource(R.color.white))
        }
        Row(
            Modifier.height(61.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center, Alignment.CenterHorizontally) {
                Text(text = "$levelValue", fontSize = 20.sp)
                Text(text = stringResource(R.string.level), color = colorResource(R.color.text_quad), fontSize = 12.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center, Alignment.CenterHorizontally) {
                Text(text = "$equipmentValue", fontSize = 20.sp)
                Text(text = stringResource(R.string.sidebar_equipment), color = colorResource(R.color.text_quad), fontSize = 12.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center, Alignment.CenterHorizontally) {
                Text(text = "$buffValue", fontSize = 20.sp)
                Text(text = stringResource(R.string.buffs), color = colorResource(R.color.text_quad), fontSize = 12.sp)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(colorResource(if (canAllocate) R.color.offset_background_30 else R.color.window_background)),
                verticalArrangement = Arrangement.Center,
                Alignment.CenterHorizontally
            ) {
                Text(text = "$allocatedValue", fontSize = 20.sp, color = if (canAllocate) statColor else colorResource(R.color.text_primary))
                Text(text = stringResource(R.string.allocated), color = if (canAllocate) statColor else colorResource(R.color.text_quad), fontSize = 12.sp)
            }
            AnimatedVisibility(visible = canAllocate) {
                TextButton(
                    onClick = allocateAction,
                    Modifier
                        .width(48.dp)
                        .fillMaxHeight()
                        .background(
                            colorResource(id = R.color.offset_background_30)
                        )
                ) {
                    Image(HabiticaIconsHelper.imageOfAttributeAllocateButton().asImageBitmap(), null)
                }
            }
        }
    }
}

@Preview
@Composable
fun StatsViewPreview() {
    Column(Modifier.background(colorResource(id = R.color.content_background))) {
        StatsViewComposable(
            statText = "Strength",
            statColor = colorResource(id = R.color.red_50),
            levelValue = 10,
            equipmentValue = 5,
            buffValue = 4,
            allocatedValue = 8,
            canAllocate = false
        ) {}
        StatsViewComposable(
            statText = "Intelligence",
            statColor = colorResource(id = R.color.blue_50),
            levelValue = 10,
            equipmentValue = 5,
            buffValue = 4,
            allocatedValue = 20,
            canAllocate = true
        ) {}
    }
}

class StatsView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private val binding = StatsViewBinding.inflate(context.layoutInflater, this, true)

    var levelValue: Int = 0
        set(value) {
            field = value
            binding.levelValueTextView.text = value.toString()
        }
    var equipmentValue: Int = 0
        set(value) {
            field = value
            binding.equipmentValueTextView.text = value.toString()
        }
    var buffValue: Int = 0
        set(value) {
            field = value
            binding.buffValueTextView.text = value.toString()
        }
    var allocatedValue: Int = 0
        set(value) {
            field = value
            binding.allocatedValueTextView.text = value.toString()
        }

    var totalValue: Int = 0
        set(value) {
            field = value
            binding.totalValueTextView.text = value.toString()
        }

    var canDistributePoints: Boolean = false
        set(value) {
            field = value
            binding.allocateButton.visibility = if (value) View.VISIBLE else View.GONE
            if (value) {
                binding.allocatedWrapper.setBackgroundColor(ContextCompat.getColor(context, R.color.offset_background_30))
                binding.allocateButton.setBackgroundColor(ContextCompat.getColor(context, R.color.offset_background_30))
                binding.allocatedValueTextView.setTextColor(statColor)
                binding.allocatedLabelView.setTextColor(statColor)
            } else {
                binding.allocatedWrapper.setBackgroundColor(context.getThemeColor(R.attr.colorWindowBackground))
                binding.allocateButton.setBackgroundColor(context.getThemeColor(R.attr.colorWindowBackground))
                binding.allocatedValueTextView.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                binding.allocatedLabelView.setTextColor(ContextCompat.getColor(context, R.color.text_quad))
            }
        }

    var allocateAction: (() -> Unit)? = null

    private var statColor: Int = 0

    init {
        val attributes = context.theme?.obtainStyledAttributes(
            attrs,
            R.styleable.StatsView,
            0,
            0
        )

        val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.layout_top_rounded_bg_white)
        if (attributes != null) {
            statColor = attributes.getColor(R.styleable.StatsView_statsColor, 0)
            backgroundDrawable?.setTintWith(attributes.getColor(R.styleable.StatsView_titleBackgroundColor, 0), PorterDuff.Mode.MULTIPLY)
            binding.titleTextView.text = attributes.getString(R.styleable.StatsView_statsTitle)
        }
        binding.titleWrapper.background = backgroundDrawable

        binding.allocateButton.setOnClickListener {
            HapticFeedbackManager.tap(this)
            allocateAction?.invoke()
        }

        binding.allocateButton.setImageBitmap(HabiticaIconsHelper.imageOfAttributeAllocateButton())
    }
}
