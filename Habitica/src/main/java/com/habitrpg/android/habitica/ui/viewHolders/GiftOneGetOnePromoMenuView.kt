package com.habitrpg.android.habitica.ui.viewHolders

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.widget.Button
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.ui.activities.GiftOneGetOneInfoActivity

class GiftOneGetOnePromoMenuView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    init {
        inflate(R.layout.promo_gift_one_get_one, true)
        setBackgroundColor(ContextCompat.getColor(context, R.color.teal_50))
        clipToPadding = false
        clipChildren = false
        clipToOutline = false
        findViewById<Button>(R.id.button).setOnClickListener {
            val intent = Intent(context, GiftOneGetOneInfoActivity::class.java)
            context.startActivity(intent)
        }
    }


}
