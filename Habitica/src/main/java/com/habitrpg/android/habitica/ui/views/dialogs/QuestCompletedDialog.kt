package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.inventory.QuestContent

class QuestCompletedDialog(context: Context) : HabiticaAlertDialog(context) {

    var quest: QuestContent? = null
        set(value) {
            field = value
            if (value == null) return

            val contentView = QuestCompletedDialogContent(context)
            contentView.setQuestContent(value)
            setAdditionalContentView(contentView)
        }

    override fun dismiss() {
        dialog = null
        super.dismiss()
    }

    companion object {
        private var dialog: QuestCompletedDialog? = null

        fun showWithQuest(context: Context, quest: QuestContent) {
            if (dialog != null) return

            dialog = QuestCompletedDialog(context)
            dialog?.quest = quest
            dialog?.setTitle(R.string.quest_completed)
            dialog?.addButton(R.string.onwards, isPrimary = true, isDestructive = false)
            dialog?.enqueue()
        }
    }
}
