package com.habitrpg.android.habitica.events

import android.graphics.drawable.Drawable
import android.view.View
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.SnackbarDisplayType

/**
 * Created by phillip on 26.06.17.
 */
class ShowSnackbarEvent {
    constructor(title: String, type: SnackbarDisplayType) {
        this.title = title
        this.type = type
    }

    constructor()

    var leftImage: Drawable? = null
    var title: String? = null
    var text: String? = null
    var type: SnackbarDisplayType = SnackbarDisplayType.NORMAL
    var specialView: View? = null
    var rightIcon: Drawable? = null
    var rightTextColor = 0
    var rightText: String? = null
}
