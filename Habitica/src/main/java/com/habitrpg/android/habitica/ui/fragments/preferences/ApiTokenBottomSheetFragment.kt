package com.habitrpg.android.habitica.ui.fragments.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.habitrpg.android.habitica.ui.views.ApiTokenBottomSheet

class ApiTokenBottomSheetFragment(
    private val apiToken: String,
    private val onCopyToken: (String) -> Unit,
) : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ApiTokenBottomSheet(apiToken = apiToken, onCopyToken = onCopyToken)
            }
        }
    }

    companion object {
        const val TAG = "ApiTokenBottomSheet"
    }
}
