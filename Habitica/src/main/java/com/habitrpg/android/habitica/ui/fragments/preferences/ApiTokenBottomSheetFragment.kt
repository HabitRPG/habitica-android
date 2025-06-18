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

class ApiTokenBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var apiToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiToken = arguments?.getString(ARG_API_TOKEN) ?: ""
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
