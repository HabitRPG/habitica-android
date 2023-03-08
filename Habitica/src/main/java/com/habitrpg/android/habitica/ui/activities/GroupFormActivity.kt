package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ActivityGroupFormBinding
import com.habitrpg.android.habitica.ui.helpers.AutocompleteAdapter
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.common.habitica.helpers.MarkdownParser
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupFormActivity : BaseActivity() {

    private lateinit var binding: ActivityGroupFormBinding
    private var groupID: String? = null
    private var groupType: String? = null
    private var groupName: String? = null
    private var groupDescription: String? = null
    private var groupPrivacy: String? = null
    private var groupLeader: String? = null
    private var leaderCreateChallenge = false

    private var autocompleteAdapter: AutocompleteAdapter? = null

    override fun getLayoutResId(): Int {
        return R.layout.activity_group_form
    }

    override fun getContentView(layoutResId: Int?): View {
        binding = ActivityGroupFormBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.extras?.let { bundle ->
            groupID = bundle.getString("groupID")
            groupType = bundle.getString("groupType")
            groupName = bundle.getString("name")
            groupDescription = bundle.getString("description")
            groupPrivacy = bundle.getString("privacy")
            groupLeader = bundle.getString("leader")
            leaderCreateChallenge = bundle.getBoolean("leaderCreateChallenge", false)
        }

        if (groupType == "party") {
            binding.privacyWrapper.visibility = View.GONE
            binding.privacySeparator.visibility = View.GONE
        }

        if (groupID != null) {
            fillForm()
        }

        binding.cancelButton.setOnClickListener {
            finish()
            dismissKeyboard()
        }

        binding.saveButton.setOnClickListener {
            finishActivitySuccessfuly()
        }
    }

    private fun fillForm() {
        binding.groupNameEditText.setText(groupName)
        binding.groupDescriptionEditText.setText(groupDescription)
        binding.leaderCreateChallengeSwitch.isChecked = leaderCreateChallenge
        binding.privacyWrapper.visibility = View.GONE
        binding.saveButton.text = getString(R.string.save)
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
        val name = binding.groupNameEditText.text.toString()
        if (name.isEmpty()) {
            return
        }
        val resultIntent = Intent()
        val bundle = Bundle()
        bundle.putString("name", name)
        bundle.putString("groupType", groupType)
        bundle.putString("description", MarkdownParser.parseCompiled(binding.groupDescriptionEditText.text.toString()))
        bundle.putBoolean("leaderOnlyChallenges", binding.leaderCreateChallengeSwitch.isChecked)
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
