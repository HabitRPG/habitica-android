package com.habitrpg.android.habitica.ui.fragments.preferences

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.DialogHabiticaAccountBinding
import com.habitrpg.android.habitica.models.user.User
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HabiticaAccountDialog(private var thisContext: Context) : BottomSheetDialogFragment(R.layout.dialog_habitica_account) {
    private var _binding: DialogHabiticaAccountBinding? = null
    private val binding get() = _binding!!

    var accountAction: String? = null
    var accountUpdateConfirmed: AccountUpdateConfirmed? = null
    var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.HabiticaAlertDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogHabiticaAccountBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let { _ ->
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = true
        }

        when (accountAction) {
            "reset_account" -> setResetAccountViews()
            "delete_account" -> setDeleteAccountViews()
        }

        binding.backImagebutton.setOnClickListener { dismiss() }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val habiticaAccountDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        habiticaAccountDialog.setOnShowListener { dialog: DialogInterface ->
            val notificationDialog = dialog as BottomSheetDialog
            notificationDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            notificationDialog.behavior.isDraggable = false
        }
        return habiticaAccountDialog
    }

    private fun setResetAccountViews() {
        binding.titleTextview.setText(R.string.reset_account_title)
        binding.warningDescriptionTextview.setText(R.string.reset_account_description_new)
        binding.confirmationTextInputLayout.setHint(R.string.password)
        if (user?.authentication?.hasPassword != true) {
            binding.warningDescriptionTextview.text = context?.getString(R.string.reset_account_description_no_pw)
            binding.confirmationTextInputLayout.setHint(R.string.confirm_deletion)
            binding.confirmationInputEdittext.inputType = InputType.TYPE_CLASS_TEXT
        }
        binding.confirmActionTextview.setText(R.string.reset_account)

        binding.confirmationInputEdittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.confirmActionTextview.setTextColor(ContextCompat.getColor(thisContext, R.color.gray_300))
                binding.confirmActionTextview.alpha = .4f
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.confirmationInputEdittext.text.toString().isNotEmpty()) {
                    if ((user?.authentication?.hasPassword != true && binding.confirmationInputEdittext.text.toString() == context?.getString(R.string.reset_caps)) ||
                        user?.authentication?.hasPassword == true
                    ) {
                        binding.confirmActionTextview.setTextColor(ContextCompat.getColor(thisContext, R.color.red_100))
                        binding.confirmActionTextview.alpha = 1.0f
                    }
                } else {
                    binding.confirmActionTextview.setTextColor(ContextCompat.getColor(thisContext, R.color.gray_300))
                    binding.confirmActionTextview.alpha = .4f
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        binding.confirmActionTextview.setOnClickListener {
            val confirmationString = binding.confirmationInputEdittext.text.toString()
            if (user?.authentication?.hasPassword != true) {
                if (confirmationString == context?.getString(R.string.reset_caps)) {
                    accountUpdateConfirmed?.resetConfirmedClicked(confirmationString)
                }
            } else {
                if (confirmationString.isNotEmpty()) {
                    accountUpdateConfirmed?.resetConfirmedClicked(confirmationString)
                }
            }
        }
    }

    private fun setDeleteAccountViews() {
        binding.titleTextview.setText(R.string.are_you_sure_you_want_to_delete)
        binding.confirmationTextInputLayout.setHint(R.string.password)
        binding.confirmActionTextview.setText(R.string.delete_account)
        binding.warningDescriptionTextview.text = context?.getString(R.string.delete_account_description)
        if (user?.authentication?.hasPassword != true) {
            binding.warningDescriptionTextview.text = context?.getString(R.string.delete_oauth_account_description)
            binding.confirmationTextInputLayout.setHint(R.string.confirm_deletion)
            binding.confirmationInputEdittext.inputType = InputType.TYPE_CLASS_TEXT
        }

        binding.confirmationInputEdittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.confirmActionTextview.setTextColor(ContextCompat.getColor(thisContext, R.color.gray_300))
                binding.confirmActionTextview.alpha = .4f
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (binding.confirmationInputEdittext.text.toString().isNotEmpty()) {
                    if ((user?.authentication?.hasPassword != true && binding.confirmationInputEdittext.text.toString() == context?.getString(R.string.delete_caps)) ||
                        user?.authentication?.hasPassword == true
                    ) {
                        binding.confirmActionTextview.setTextColor(ContextCompat.getColor(thisContext, R.color.red_100))
                        binding.confirmActionTextview.alpha = 1.0f
                    }
                } else {
                    binding.confirmActionTextview.setTextColor(ContextCompat.getColor(thisContext, R.color.gray_300))
                    binding.confirmActionTextview.alpha = .4f
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
        binding.confirmActionTextview.setOnClickListener {
            val confirmationString = binding.confirmationInputEdittext.text.toString()
            if (user?.authentication?.hasPassword != true) {
                if (confirmationString == context?.getString(R.string.delete_caps)) {
                    accountUpdateConfirmed?.deletionConfirmClicked(confirmationString)
                }
            } else {
                if (confirmationString.isNotEmpty()) {
                    accountUpdateConfirmed?.deletionConfirmClicked(confirmationString)
                }
            }
        }
    }

    override fun getTheme(): Int {
        return R.style.HabiticaAlertDialogTheme
    }

    interface AccountUpdateConfirmed {
        fun resetConfirmedClicked(confirmationString: String)
        fun deletionConfirmClicked(confirmationString: String)
    }

    companion object {
        const val TAG = "HabiticaAccountDialog"
    }
}
