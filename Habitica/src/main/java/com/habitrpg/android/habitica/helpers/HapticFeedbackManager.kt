package com.habitrpg.android.habitica.helpers

import android.view.HapticFeedbackConstants
import android.view.View

class HapticFeedbackManager {
    companion object {
        fun tap(view: View) {
            try {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            } catch (_: SecurityException) {
                // some devices require VIBRATE permission for haptic feedback
            }
        }

        fun longPress(view: View) {
            try {
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            } catch (_: SecurityException) {
                // some devices require VIBRATE permission for haptic feedback
            }
        }
    }
}
