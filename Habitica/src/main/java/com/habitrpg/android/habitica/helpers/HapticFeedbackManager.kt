package com.habitrpg.android.habitica.helpers

import android.view.HapticFeedbackConstants
import android.view.View

class HapticFeedbackManager {
    companion object {
        fun tap(view: View) {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }

        fun longPress(view: View) {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
}
