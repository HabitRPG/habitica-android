package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import com.habitrpg.android.habitica.databinding.DialogOpenMysteryitemBinding
import com.habitrpg.common.habitica.extensionsCommon.dpToPx
import com.habitrpg.common.habitica.extensionsCommon.layoutInflater

class OpenedMysteryitemDialog(context: Context) : HabiticaAlertDialog(context) {
    val binding = DialogOpenMysteryitemBinding.inflate(context.layoutInflater)

    init {
        setAdditionalContentView(binding.root)
        dialogWidth = 302.dpToPx(context)
    }
}
