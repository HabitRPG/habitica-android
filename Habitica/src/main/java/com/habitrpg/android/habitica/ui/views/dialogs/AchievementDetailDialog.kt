package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.extensions.fromHtml
import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils

class AchievementDetailDialog(val achievement: Achievement, context: Context): HabiticaAlertDialog(context) {

    private var iconView: SimpleDraweeView?
    private var descriptionView: TextView?

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        val view = inflater?.inflate(R.layout.dialog_achievement_detail, null)
        iconView = view?.findViewById(R.id.icon_view)
        descriptionView = view?.findViewById(R.id.description_view)
        setAdditionalContentView(view)
        setTitle(achievement.title)
        descriptionView?.setText(achievement.text?.fromHtml(), TextView.BufferType.SPANNABLE)
        val iconName = if (achievement.earned) {
            achievement.icon + "2x"
        } else {
            "achievement-unearned2x"
        }
        DataBindingUtils.loadImage(iconView, iconName)
        addCloseButton(true)
    }
}