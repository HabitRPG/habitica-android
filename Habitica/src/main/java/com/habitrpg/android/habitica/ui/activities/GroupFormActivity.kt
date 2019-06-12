package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.ui.helpers.*
import com.habitrpg.android.habitica.ui.views.HabiticaAutocompleteTextView

class GroupFormActivity : BaseActivity() {

    private var groupID: String? = null
    private var groupType: String? = null
    private var groupName: String? = null
    private var groupDescription: String? = null
    private var groupPrivacy: String? = null
    private var groupLeader: String? = null
    private var leaderCreateChallenge = false

    private val cancelButton: ImageButton by bindView(R.id.cancel_button)
    private val saveButton: Button by bindView(R.id.save_button)
    private val groupNameEditText: HabiticaAutocompleteTextView by bindView(R.id.group_name_edittext)
    private val groupDescriptionEditText: HabiticaAutocompleteTextView by bindView(R.id.group_description_edittext)
    private val leaderCreateChallengeSwitch: Switch by bindView(R.id.leader_create_challenge_switch)
    private val privacyWrapper: LinearLayout by bindView(R.id.privacyWrapper)
    internal val privacySpinner: Spinner by bindView(R.id.privacySpinner)
    private val privacySeparator: View by bindView(R.id.privacy_separator)

    private var autocompleteAdapter: AutocompleteAdapter? = null

    override fun getLayoutResId(): Int {
        return R.layout.activity_group_form
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.extras?.let {bundle ->
            groupID = bundle.getString("groupID")
            groupType = bundle.getString("groupType")
            groupName = bundle.getString("name")
            groupDescription = bundle.getString("description")
            groupPrivacy = bundle.getString("privacy")
            groupLeader = bundle.getString("leader")
            leaderCreateChallenge = bundle.getBoolean("leaderCreateChallenge", false)
        }

        if (groupType == "party") {
            privacyWrapper.visibility = View.GONE
            privacySeparator.visibility = View.GONE
        }

        if (groupID != null) {
            fillForm()
        }

        cancelButton.setOnClickListener {
            finish()
            dismissKeyboard()
        }

        saveButton.setOnClickListener {
            finishActivitySuccessfuly()
        }

        autocompleteAdapter = AutocompleteAdapter(this)
        val tokenizer = AutocompleteTokenizer(listOf(':'))
        groupNameEditText.setAdapter(autocompleteAdapter)
        groupNameEditText.threshold = 2
        groupNameEditText.setTokenizer(tokenizer)
        groupDescriptionEditText.setAdapter(autocompleteAdapter)
        groupDescriptionEditText.setTokenizer(tokenizer)
        groupDescriptionEditText.threshold = 2
    }


    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    private fun fillForm() {
        groupNameEditText.setText(groupName)
        groupDescriptionEditText.setText(groupDescription)
        leaderCreateChallengeSwitch.isChecked = leaderCreateChallenge
        privacyWrapper.visibility = View.GONE
        saveButton.text = getString(R.string.save)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        dismissKeyboard()
        return true
    }

    override fun onBackPressed() {
        finish()
        dismissKeyboard()
    }

    private fun finishActivitySuccessfuly() {
        val name = groupNameEditText.text.toString()
        if (name.isEmpty()) {
            return
        }
        val resultIntent = Intent()
        val bundle = Bundle()
        bundle.putString("name", name)
        bundle.putString("groupType", groupType)
        bundle.putString("description", MarkdownParser.parseCompiled(this.groupDescriptionEditText.text))
        bundle.putBoolean("leaderCreateChallenge", leaderCreateChallengeSwitch.isActivated)
        bundle.putString("leader", this.groupLeader)
        resultIntent.putExtras(bundle)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
        dismissKeyboard()
    }

    companion object {

        const val GROUP_FORM_ACTIVITY = 11
    }
}
