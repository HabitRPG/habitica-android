package com.habitrpg.android.habitica.ui.fragments.preferences

import android.content.ClipData
import android.content.ClipboardManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.views.ApiTokenBottomSheet
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.SnackbarActivity

class ApiTokenBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var apiToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiToken = arguments?.getString(ARG_API_TOKEN) ?: ""
    }

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
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ApiTokenBottomSheet(apiToken = apiToken, onCopyToken = { copiedToken ->
                    (activity as? SnackbarActivity)?.showSnackbar(
                        content = getString(R.string.copied_to_clipboard, copiedToken),
                        displayType = HabiticaSnackbar.SnackbarDisplayType.SUCCESS,
                    )
                    val clipboard: ClipboardManager? =
                        context?.let { getSystemService(it, ClipboardManager::class.java) }
                    clipboard?.setPrimaryClip(ClipData.newPlainText("API Token", copiedToken))
                    dismiss()
                })
            }
        }
    }

    companion object {
        private const val ARG_API_TOKEN = "arg_api_token"
        fun newInstance(apiToken: String): ApiTokenBottomSheetFragment =
            ApiTokenBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_API_TOKEN, apiToken)
                }
            }

        const val TAG = "ApiTokenBottomSheet"
    }
}
