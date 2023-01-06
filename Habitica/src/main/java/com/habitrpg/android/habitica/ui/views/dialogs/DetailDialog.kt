package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.ui.views.shops.PurchaseDialogQuestContent

class DetailDialog(context: Context) : HabiticaAlertDialog(context) {

    var quest: QuestContent? = null
        set(value) {
            field = value
            if (value == null) return

            val contentView = PurchaseDialogQuestContent(context)
            contentView.setQuestContentItem(value)
            setAdditionalContentView(contentView)
        }

    init {
        addCloseButton()
    }
}
