package com.habitrpg.android.habitica.helpers

import com.habitrpg.android.habitica.helpers.postProcessors.InvertPostProcessor
import com.habitrpg.android.habitica.ui.AvatarView

class AprilFoolsHandler {

    companion object {
        fun handle(name: String?) {
            when(name) {
                "invert" -> invertFools()
            }
        }

        private fun invertFools() {
            AvatarView.postProcessors[AvatarView.LayerType.PET] = { InvertPostProcessor() }
        }
    }
}
