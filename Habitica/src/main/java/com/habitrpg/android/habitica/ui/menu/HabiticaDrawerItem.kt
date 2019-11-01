package com.habitrpg.android.habitica.ui.menu

import android.os.Bundle

data class HabiticaDrawerItem(var transitionId: Int, val identifier: String, val text: String, val isHeader: Boolean = false, var additionalInfoAsPill: Boolean = true) {
    constructor(transitionId: Int, identifier: String) : this(transitionId, identifier, "")

    var bundle: Bundle? = null
    var isPromo: Boolean = false
    var additionalInfo: String? = null
    var additionalInfoTextColor: Int? = null
    var showBubble: Boolean = false
    var isVisible: Boolean = true
    var isEnabled: Boolean = true
}