package com.habitrpg.android.habitica.ui.fragments.preferences

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.habitrpg.android.habitica.ui.views.ChangePasswordScreen
import com.habitrpg.common.habitica.theme.HabiticaTheme
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.MutableLiveData

class ChangePasswordBottomSheet(
    var onForgotPassword: () -> Unit? = {},
    var onPasswordChanged: (oldPassword: String, newPassword: String) -> Unit = { _, _ -> }
) : BottomSheetDialogFragment() {
    override fun onStart() {
        super.onStart()

        val nightModeFlags = requireContext()
            .resources
            .configuration
            .uiMode and Configuration.UI_MODE_NIGHT_MASK

        if (nightModeFlags == Configuration.UI_MODE_NIGHT_NO) {
            dialog?.window?.let { window ->
                window.statusBarColor = ContextCompat.getColor(
                    requireContext(),
                    android.R.color.transparent
                )
                window.navigationBarColor = ContextCompat.getColor(
                    requireContext(),
                    android.R.color.transparent
                )

                WindowInsetsControllerCompat(window, window.decorView).apply {
                    isAppearanceLightStatusBars = true
                    isAppearanceLightNavigationBars = true
                }
            }
        }

        dialog?.let { dlg ->
            val bottomSheet = dlg.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
            val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
            behavior.skipCollapsed = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                HabiticaTheme {
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { visible = true }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn()
                    ) {
                        ChangePasswordScreen(
                            onBack = { dismiss() },
                            onSave = { oldPassword, newPassword->
                                onPasswordChanged(oldPassword, newPassword)
                            },
                            onForgot = {
                                onForgotPassword()
                                dismiss()
                            }
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "ChangePasswordFragment"
    }
}
