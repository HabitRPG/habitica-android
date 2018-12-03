package com.habitrpg.android.habitica.ui.menu

class HabiticaDrawerItem(val transitionId: Int,val identifier: String, val text: String, val isHeader: Boolean = false, var additionalInfoAsPill: Boolean = true) {

    var additionalInfo: String? = null
    var isVisible: Boolean = true
    var isEnabled: Boolean = true
}