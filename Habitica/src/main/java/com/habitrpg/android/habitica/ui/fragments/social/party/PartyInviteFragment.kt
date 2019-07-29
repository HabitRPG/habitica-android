package com.habitrpg.android.habitica.ui.fragments.social.party

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import java.util.*
import javax.inject.Inject

class PartyInviteFragment : BaseFragment() {

    @Inject
    lateinit var configManager: AppConfigManager

    var isEmailInvite: Boolean = false

    private val inviteDescription: TextView? by bindView(R.id.inviteDescription)
    private val invitationWrapper: LinearLayout? by bindView(R.id.invitationWrapper)
    private val addInviteButton: Button? by bindView(R.id.addInviteButton)

    val values: Array<String>
        get() {
            val values = ArrayList<String>()
            for (i in 0 until (invitationWrapper?.childCount ?: 0)) {
                val valueEditText = invitationWrapper?.getChildAt(i) as? EditText
                if (valueEditText?.text?.toString()?.isNotEmpty() == true) {
                    values.add(valueEditText.text.toString())
                }
            }
            return values.toTypedArray()
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_party_invite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetViews()

        when {
            isEmailInvite -> inviteDescription?.text = getString(R.string.invite_email_description)
            else -> inviteDescription?.text = getString(R.string.invite_username_description)
        }

        addInviteField()

        addInviteButton?.setOnClickListener { addInviteField() }
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun addInviteField() {
        val editText = EditText(context)

        when {
            isEmailInvite -> {
                editText.setHint(R.string.email)
                editText.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            }
            else -> editText.setHint(R.string.username)
        }
        invitationWrapper?.addView(editText)
    }
}
