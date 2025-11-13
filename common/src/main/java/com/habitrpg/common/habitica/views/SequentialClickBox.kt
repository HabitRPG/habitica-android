package com.habitrpg.common.habitica.helpers

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun SequentialClickBox(
    onTrigger: () -> Unit,
    modifier: Modifier = Modifier,
    onRemainingClicks: (Int) -> Unit = {},
    clicksToShowCountdown: Int = 4,
    clicksToTrigger: Int = 7,
    timeout: Long = 1_500L,
    content: @Composable (Modifier) -> Unit
) {
    var clicks by remember { mutableIntStateOf(0) }
    var lastClickTime by remember { mutableLongStateOf(0L) }
    val clickableModifier = modifier.clickable {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > timeout.coerceAtLeast(0L)) {
            clicks = 0
        }
        clicks++
        lastClickTime = currentTime

        val remaining = clicksToTrigger - clicks
        // Only trigger the countdown toast after the user has clicked the logo 4 or more times
        if (clicks >= clicksToShowCountdown) {
            onRemainingClicks(remaining)
        }

        if (clicks >= clicksToTrigger) {
            // After closing the preferences, let the user re-open by just clicking the logo once
            clicks = clicksToTrigger - 1
            onTrigger()
        }
    }
    content(clickableModifier)
}
