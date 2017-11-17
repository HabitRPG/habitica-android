package com.habitrpg.android.habitica.ui.menu

class HabiticaDrawerItem(val identifier: String, val text: String, val isHeader: Boolean = false) {

    var additionalInfo: String? = null
    var isVisible: Boolean = true
    var isEnabled: Boolean = true
}