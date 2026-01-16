package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.extensions.fromHtml
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.common.habitica.views.PixelArtView

class RebirthEnabledDialog(context: Context) : HabiticaAlertDialog(context) {
    private val rebirthContentView = context.layoutInflater.inflate(R.layout.dialog_rebirth_enabled, null)

    init {
        setAdditionalContentView(rebirthContentView)
        configure()
    }

    private fun configure() {
        setTitle(R.string.rebirth_enabled_title)
        rebirthContentView.findViewById<TextView>(R.id.title_view).text = context.getString(R.string.rebirth_enabled_subtitle)
        rebirthContentView.findViewById<TextView>(R.id.description_view).text = context.getString(R.string.rebirth_enabled_description).fromHtml()
        rebirthContentView.findViewById<PixelArtView>(R.id.icon_view).loadImage("rebirth_orb")

        addButton(R.string.onwards, isPrimary = true, isDestructive = false) { _, _ ->
        }
        addButton(R.string.go_to_market, isPrimary = false, isDestructive = false) { _, _ ->
            MainNavigationController.navigate(R.id.marketFragment)
        }
    }
}
