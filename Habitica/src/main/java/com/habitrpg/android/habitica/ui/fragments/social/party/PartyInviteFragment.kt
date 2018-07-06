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
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.prefs.scanner.IntentIntegrator
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import java.util.*

class PartyInviteFragment : BaseFragment() {

    var isEmailInvite: Boolean = false

    private val inviteDescription: TextView? by bindView(R.id.inviteDescription)
    private val invitationWrapper: LinearLayout? by bindView(R.id.invitationWrapper)
    private val addInviteButton: Button? by bindView(R.id.addInviteButton)
    private val inviteQRButton: Button? by bindView(R.id.InviteByQR)

    val values: Array<String>
        get() {
            val values = ArrayList<String>()
            for (i in 0 until (invitationWrapper?.childCount ?: 0)) {
                val valueEditText = invitationWrapper?.getChildAt(i) as EditText
                if (valueEditText.text.toString().isNotEmpty()) {
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

        if (isEmailInvite) {
            inviteDescription?.text = getString(R.string.invite_email_description)
        } else {
            inviteDescription?.text = getString(R.string.invite_id_description)
        }

        addInviteField()

        addInviteButton?.setOnClickListener { addInviteField() }
        inviteQRButton?.setOnClickListener { startQRInvite() }
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    fun addInviteField() {
        val editText = EditText(context)

        if (isEmailInvite) {
            editText.setHint(R.string.email)
            editText.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        } else {
            editText.setHint(R.string.user_id)
        }
        invitationWrapper?.addView(editText)
    }

    fun startQRInvite() {
        val scanIntegrator = IntentIntegrator(activity)
        scanIntegrator.initiateScan()
    }
}
