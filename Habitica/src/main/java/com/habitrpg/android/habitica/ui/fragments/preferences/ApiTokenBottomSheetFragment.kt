package com.habitrpg.android.habitica.ui.fragments.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.views.ApiTokenBottomSheet
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.SnackbarActivity

class ApiTokenBottomSheetFragment(
    private val apiToken: String
) : BottomSheetDialogFragment() {
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
                    dismiss()
                })
            }
        }
    }

    companion object {
        const val TAG = "ApiTokenBottomSheet"
    }
}
