package com.habitrpg.android.habitica.helpers

import java.util.Date

class AprilFoolsHandler {
    companion object {
        private var eventEnd: Date? = null

        fun handle(
            name: String?,
            endDate: Date?,
        ) {
            if (endDate != null) {
                this.eventEnd = endDate
            }
            when (name) {
                "invert" -> invertFools()
            }
        }

        private fun invertFools() {
            /*AvatarView.postProcessors[AvatarView.LayerType.PET] = {
                if (Date().after(eventEnd)) {
                    null
                } else {
                    InvertPostProcessor()
                }
            }*/
        }
    }
}
