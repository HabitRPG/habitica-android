package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import android.view.LayoutInflater
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils

class WonChallengeDialog(context: Context) : HabiticaAlertDialog(context) {
    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        val view = inflater?.inflate(R.layout.dialog_won_challenge, null)
        setTitle(R.string.you_won_challenge)

        DataBindingUtils.loadImage(view?.findViewById<SimpleDraweeView>(R.id.hatchingPotion_view), "achievement-karaoke2x")

        setAdditionalContentView(view)
        addButton(R.string.hurray, true)
    }
}
