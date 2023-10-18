package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import android.view.LayoutInflater
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.views.PixelArtView

class FirstDropDialog(context: Context) : HabiticaAlertDialog(context) {

    private var eggView: PixelArtView?
    private var hatchingPotionView: PixelArtView?

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        val view = inflater?.inflate(R.layout.dialog_first_drop, null)
        eggView = view?.findViewById(R.id.egg_view)
        hatchingPotionView = view?.findViewById(R.id.hatchingPotion_view)
        setAdditionalContentView(view)
        addButton(R.string.go_to_items, isPrimary = true, isDestructive = false) { _, _ ->
            MainNavigationController.navigate(R.id.itemsFragment)
        }
        addButton(R.string.close, false)
        setTitle(R.string.first_drop_title)
    }

    fun configure(egg: String, hatchingPotion: String) {
        eggView?.loadImage("Pet_Egg_$egg")
        hatchingPotionView?.loadImage("Pet_HatchingPotion_$hatchingPotion")
    }
}
