package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.habitrpg.common.habitica.R
import com.habitrpg.common.habitica.models.PlayerTier

class UsernameLabel
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    private val textView = TextView(context)
    private val tierIconView = ImageView(context)

    var username: String? = ""
        set(value) {
            field = value
            textView.text = value
        }

    var isNPC: Boolean = false
        set(value) {
            field = value
            tier = tier
        }

    var tier: Int = 0
        set(value) {
            field = value
            if (isNPC) {
                textView.setTextColor(ContextCompat.getColor(context, R.color.contributor_npc))
            } else {
                textView.setTextColor(PlayerTier.getColorForTier(context, value))
            }
            if (value == 0) {
                tierIconView.visibility = View.GONE
            } else {
                tierIconView.visibility = View.VISIBLE
                tierIconView.setImageBitmap(
                    HabiticaIconsHelper.imageOfContributorBadge(
                        value.toFloat(),
                        isNPC
                    )
                )
            }
        }

    init {
        val textViewParams =
            LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        textViewParams.gravity = Gravity.CENTER_VERTICAL
        textViewParams.weight = 1.0f
        addView(textView, textViewParams)
        val padding = context.resources.getDimension(R.dimen.spacing_small).toInt()
        textView.setPadding(0, 0, padding, 0)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            textView.typeface = Typeface.create(null, 600, false)
        }
        val iconViewParams =
            LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        iconViewParams.gravity = Gravity.CENTER_VERTICAL
        addView(tierIconView, iconViewParams)
    }
}

@Composable
fun ComposableUsernameLabel(
    username: String,
    tier: Int,
    modifier: Modifier = Modifier,
    isNPC: Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        ProvideTextStyle(value = TextStyle(fontWeight = FontWeight.SemiBold)) {
            Text(
                username,
                color =
                if (isNPC) {
                    colorResource(id = R.color.contributor_npc)
                } else {
                    Color(
                        PlayerTier.getColorForTier(
                            LocalContext.current,
                            tier
                        )
                    )
                }
            )
            if (tier > 0) {
                Image(
                    bitmap =
                    HabiticaIconsHelper.imageOfContributorBadge(tier.toFloat(), isNPC)
                        .asImageBitmap(),
                    contentDescription = null
                )
            }
        }
    }
}
