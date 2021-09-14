package com.habitrpg.android.habitica.ui.fragments.social.party

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.databinding.FragmentPartyInviteBinding
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import java.util.*
import javax.inject.Inject

class PartyInviteFragment : BaseFragment<FragmentPartyInviteBinding>() {

    @Inject
    lateinit var configManager: AppConfigManager

    override var binding: FragmentPartyInviteBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentPartyInviteBinding {
        return FragmentPartyInviteBinding.inflate(inflater, container, false)
    }

    var isEmailInvite: Boolean = false

    val values: Array<String>
        get() {
            val values = ArrayList<String>()
            for (i in 0 until (binding?.invitationWrapper?.childCount ?: 0)) {
                val valueEditText = binding?.invitationWrapper?.getChildAt(i) as? EditText
                if (valueEditText?.text?.toString()?.isNotEmpty() == true) {
                    values.add(valueEditText.text.toString())
                }
            }
            return values.toTypedArray()
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isEmailInvite) {
            binding?.inviteDescription?.text = getString(R.string.invite_email_description)
        } else {
            binding?.inviteDescription?.text = getString(R.string.invite_username_description)
        }

        addInviteField()

        binding?.addInviteButton?.setOnClickListener { addInviteField() }
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun addInviteField() {
        val editText = EditText(context)

        if (isEmailInvite) {
            editText.setHint(R.string.email)
            editText.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        } else {
            editText.setHint(R.string.username)
        }
        binding?.invitationWrapper?.addView(editText)
    }
}
