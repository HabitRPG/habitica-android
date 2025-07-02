package com.habitrpg.android.habitica.ui.fragments.preferences

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.habitrpg.common.habitica.theme.HabiticaTheme
import androidx.compose.runtime.setValue
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior

class SettingsFormBottomSheet : BottomSheetDialogFragment() {

    var content: @Composable () -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // this is a workaround to prevent the screen from appearing blank during config changes (Light/Dark mode change for example)
        retainInstance = true
    }

    override fun onStart() {
        super.onStart()
        val nightModeFlags = requireContext()
            .resources
            .configuration
            .uiMode and Configuration.UI_MODE_NIGHT_MASK

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO) {
            dialog?.window?.apply {
                statusBarColor     = Color.TRANSPARENT
                navigationBarColor = Color.TRANSPARENT
                WindowInsetsControllerCompat(this, decorView).apply {
                    isAppearanceLightStatusBars      = true
                    isAppearanceLightNavigationBars  = true
                }
            }
        }

        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.let { sheet ->
                sheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                BottomSheetBehavior.from(sheet).apply {
                    state         = BottomSheetBehavior.STATE_EXPANDED
                    isDraggable   = false
                    skipCollapsed = true
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            HabiticaTheme {
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { visible = true }
                AnimatedVisibility(visible = visible, enter = fadeIn()) {
                    content()
                }
            }
        }
    }

    companion object {
        const val TAG = "SettingsFormBottomSheet"
    }
}
