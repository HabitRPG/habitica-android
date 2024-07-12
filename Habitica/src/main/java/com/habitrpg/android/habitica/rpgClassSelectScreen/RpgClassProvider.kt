package com.habitrpg.android.habitica.rpgClassSelectScreen

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper

object RpgClassProvider {
    fun listOfClasses() = listOf(
        RpgClass(
            rpgName = R.string.warrior,
            rpgColor = R.color.maroon_50,
            textDescription = R.string.warrior_description,
            serverName = "warrior",
            pic = R.drawable.warrior

        ),
        RpgClass(
            rpgName = R.string.mage,
            rpgColor = R.color.blue_10,
            textDescription = R.string.mage_description,
            serverName = "wizard",
            pic = R.drawable.mage
        ),
        RpgClass(
            rpgName = R.string.rogue,
            rpgColor = R.color.brand_200,
            textDescription = R.string.rogue_description,
            serverName = "rogue",
            pic = R.drawable.rogue
        ),
        RpgClass(
            rpgName = R.string.healer,
            rpgColor = R.color.yellow_100,
            textDescription = R.string.healer_description,
            serverName = "healer",
            pic = R.drawable.healer
        ),
        RpgClass(
            rpgName = R.string.action_back,
            rpgColor = R.color.gray_200,
            textDescription = R.string.just_return_on_previous_screen,
            serverName = "back",
            pic = R.drawable.arrow_back
        ),
        RpgClass(
            rpgName = R.string.opt_out_class,
            rpgColor = R.color.gray_200,
            textDescription = R.string.opt_out_description,
            serverName = "optOut",
            pic = R.drawable.notification_close
    ),
    )
}