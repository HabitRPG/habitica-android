package com.habitrpg.android.habitica.ui.fragments.preferences

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.user.User


class HabiticaAccountDialog(private var thisContext: Context, private val accountAction: String, val accountUpdateConfirmed: AccountUpdateConfirmed, val user: User?) : DialogFragment() {

    private lateinit var mainView: View
    private var backBtn: ImageButton? = null
    private var title: TextView? = null
    private var warningDescription: TextView? = null
    private var confirmationTextInputLayout: TextInputLayout? = null
    private var confirmationText: TextInputEditText? = null
    private var confirmationAction: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mainView = inflater.inflate(R.layout.dialog_habitica_account, container, false)
        return mainView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()

        when (accountAction) {
            "reset_account" -> setResetAccountViews()
            "delete_account" -> setDeleteAccountViews()
        }

    }

    private fun setResetAccountViews() {
        title?.setText(R.string.reset_account_title)
        warningDescription?.setText(R.string.reset_account_description)
        confirmationTextInputLayout?.setHint(R.string.confirm_reset)
        confirmationAction?.setText(R.string.reset_account)

        confirmationText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                confirmationAction?.setTextColor(ContextCompat.getColor(thisContext, R.color.gray_10))
                confirmationAction?.alpha = .4f
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (confirmationText?.text.toString() == context?.getString(R.string.reset_caps)) {
                    confirmationAction?.setTextColor(ContextCompat.getColor(thisContext, R.color.red_100))
                    confirmationAction?.alpha = 1.0f
                } else {
                    confirmationAction?.setTextColor(ContextCompat.getColor(thisContext, R.color.gray_10))
                    confirmationAction?.alpha = .4f
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        confirmationAction?.setOnClickListener {
            if (confirmationText?.text.toString() == context?.getString(R.string.reset_caps)) {
                accountUpdateConfirmed.resetConfirmedClicked()
            }
        }
    }

    private fun setDeleteAccountViews() {
        title?.setText(R.string.are_you_sure_you_want_to_delete)
        confirmationTextInputLayout?.setHint(R.string.password)
        confirmationAction?.setText(R.string.delete_account)
        warningDescription?.text = context?.getString(R.string.delete_account_description)
        if (user?.authentication?.hasPassword != true) {
            warningDescription?.text = context?.getString(R.string.delete_oauth_account_description)
            confirmationTextInputLayout?.setHint(R.string.confirm_deletion)
        }

        confirmationText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                confirmationAction?.setTextColor(ContextCompat.getColor(thisContext, R.color.gray_10))
                confirmationAction?.alpha = .4f
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (confirmationText?.text.toString().length >= 5) {
                    if ((user?.authentication?.hasPassword != true && confirmationText?.text.toString() == context?.getString(R.string.delete_caps)) ||
                            user?.authentication?.hasPassword == true) {
                        confirmationAction?.setTextColor(ContextCompat.getColor(thisContext, R.color.red_100))
                        confirmationAction?.alpha = 1.0f
                    }
                } else {
                    confirmationAction?.setTextColor(ContextCompat.getColor(thisContext, R.color.gray_10))
                    confirmationAction?.alpha = .4f
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
        confirmationAction?.setOnClickListener {
            if (confirmationText?.text.toString() == context?.getString(R.string.delete_caps)) {
                accountUpdateConfirmed.deletionConfirmClicked(confirmationText?.text.toString())
            }
        }
    }

    private fun initViews() {
        backBtn = mainView.findViewById(R.id.back_imagebutton)
        backBtn?.setOnClickListener { this.dismiss() }
        title = mainView.findViewById(R.id.title_textview)
        warningDescription = mainView.findViewById(R.id.warning_description_textview)
        confirmationTextInputLayout = mainView.findViewById(R.id.confirmation_text_input_layout)
        confirmationText = mainView.findViewById(R.id.confirmation_input_edittext)
        confirmationAction = mainView.findViewById(R.id.confirm_action_textview)
    }

    override fun getTheme(): Int {
        return R.style.HabiticaAccountDialogTheme
    }


    interface AccountUpdateConfirmed {
        fun resetConfirmedClicked()
        fun deletionConfirmClicked(confirmationString: String)
    }


}

