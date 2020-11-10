package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.notifications.ChallengeWonData
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils

class WonChallengeDialog(context: Context) : HabiticaAlertDialog(context) {
    fun configure(data: ChallengeWonData?) {
        if (data != null) {
            additionalContentView?.findViewById<TextView>(R.id.description_view)?.text = context.getString(R.string.won_achievement_description, data.name)

            addButton(context.getString(R.string.claim_x_gems, data.prize), true)
        } else {
            addButton(R.string.hurray, true)
        }
    }

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        val view = inflater?.inflate(R.layout.dialog_won_challenge, null)
        setTitle(R.string.you_won_challenge)

        DataBindingUtils.loadImage(view?.findViewById<SimpleDraweeView>(R.id.hatchingPotion_view), "achievement-karaoke2x")

        setAdditionalContentView(view)
    }
}
