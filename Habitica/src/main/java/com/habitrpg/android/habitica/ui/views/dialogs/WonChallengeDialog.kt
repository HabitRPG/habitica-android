package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.extensions.fromHtml
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.models.notifications.ChallengeWonData
import com.habitrpg.common.habitica.views.PixelArtView

class WonChallengeDialog(context: Context) : HabiticaAlertDialog(context) {
    fun configure(data: ChallengeWonData?) {
        val imageView = additionalContentView?.findViewById<PixelArtView>(R.id.achievement_view)
        imageView?.loadImage("achievement-karaoke-2x")

        if (data?.name != null) {
            additionalContentView?.findViewById<TextView>(R.id.description_view)?.text = context.getString(R.string.won_achievement_description, data.name).fromHtml()
        }
        if ((data?.prize ?: 0) > 0) {
            addButton(context.getString(R.string.claim_x_gems, data?.prize), true)
            additionalContentView?.findViewById<ImageView>(R.id.achievement_confetti_left)?.visibility = View.GONE
            additionalContentView?.findViewById<ImageView>(R.id.achievement_confetti_right)?.visibility = View.GONE
        } else {
            addButton(R.string.hurray, true)
            additionalContentView?.findViewById<ImageView>(R.id.achievement_confetti_view)?.visibility = View.GONE
        }
    }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        val view = inflater?.inflate(R.layout.dialog_won_challenge, null)
        setTitle(R.string.you_won_challenge)
        setAdditionalContentView(view)
    }
}
