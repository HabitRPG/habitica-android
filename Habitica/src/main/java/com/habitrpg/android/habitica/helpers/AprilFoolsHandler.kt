package com.habitrpg.android.habitica.helpers

import com.facebook.imagepipeline.request.BasePostprocessor
import com.habitrpg.android.habitica.helpers.postProcessors.InvertPostProcessor
import com.habitrpg.android.habitica.ui.AvatarView
import java.util.*

class AprilFoolsHandler {

    companion object {
        var eventEnd = Date(2020, 3, 1)

        fun handle(name: String?, endDate: Date?) {
            if (endDate != null) {
                this.eventEnd = endDate
            }
            when(name) {
                "invert" -> invertFools()
            }
        }

        private fun invertFools() {
            AvatarView.postProcessors[AvatarView.LayerType.PET] = {
                if (Date().after(eventEnd)) {
                    null
                } else {
                    InvertPostProcessor()
                }
            }
        }
    }
}
