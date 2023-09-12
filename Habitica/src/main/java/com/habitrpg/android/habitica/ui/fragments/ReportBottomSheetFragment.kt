package com.habitrpg.android.habitica.ui.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.common.habitica.databinding.FragmentReportMessageBinding
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.helpers.setMarkdown
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReportBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentReportMessageBinding

    @Inject
    lateinit var socialRepository: SocialRepository

    private var reportType: String? = null
    private var messageID: String? = null
    private var messageText: String? = null
    private var profileName: String? = null
    private var reportingUserId: String? = null
    private var groupID: String? = null
    private var isReporting: Boolean = false
    private var source: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReportMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dialog: DialogInterface ->
            val notificationDialog = dialog as BottomSheetDialog
            notificationDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            notificationDialog.behavior.isDraggable = false
        }
        return bottomSheetDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        reportType = arguments?.getString(REPORT_TYPE)
        messageID = arguments?.getString(MESSAGE_ID)
        groupID = arguments?.getString(GROUP_ID)
        messageText = arguments?.getString(MESSAGE_TEXT)
        profileName = arguments?.getString(PROFILE_NAME)
        reportingUserId = arguments?.getString(REPORTING_USER_ID)
        source = arguments?.getString(SOURCE_VIEW)


        binding.messageTextView.text = arguments?.getString(messageText)
        binding.reportButton.setOnClickListener {
            if (reportType == REPORT_TYPE_MESSAGE)
                reportMessage()
            else if (reportType == REPORT_TYPE_USER)
                reportUser()
        }
        binding.closeButton.setOnClickListener { dismiss() }

        if (reportType == REPORT_TYPE_USER) {
            binding.toolbarTitle.text = getString(R.string.report_player_toolbar_title, profileName)
            binding.additionalExplanationTextview.visibility = View.VISIBLE
            binding.infoTextInputLayout.hint = getString(R.string.report_player_hint)
            binding.additionalExplanationTextview.setMarkdown(
                getString(
                    R.string.report_user_description,
                    profileName
                )
            )
            binding.reportExplanationTextview.setMarkdown(getString(R.string.report_user_explanation))
            binding.titleTextView.text = getString(R.string.report_player_title, profileName)
            binding.messageTextView.visibility = View.GONE
        } else if (reportType == REPORT_TYPE_MESSAGE) {
            binding.additionalExplanationTextview.visibility = View.GONE
            binding.infoTextInputLayout.hint = getString(R.string.report_message_hint)
            binding.reportExplanationTextview.setMarkdown(getString(R.string.report_message_explanation))
            binding.titleTextView.text = getString(R.string.report_message_title, profileName)
            binding.messageTextView.text = messageText
        }
    }

    private fun reportMessage() {
        if (isReporting) {
            return
        }
        isReporting = true
        messageID?.let {
            lifecycleScope.launch(
                ExceptionHandler.coroutine {
                    isReporting = false
                }
            ) {
                socialRepository.flagMessage(
                    messageID ?: "",
                    binding.additionalInfoEdittext.text.toString(),
                    groupID
                )
                dismiss()
            }
        }
    }

    private fun reportUser() {
        if (isReporting) {
            return
        }
        val userIdBeingReported = reportingUserId
        if (userIdBeingReported.isNullOrBlank()) {
            return
        }
        isReporting = true
        lifecycleScope.launchCatching {
            val reportReasonInfo = binding.additionalInfoEdittext.text.toString()
            val updateData = mapOf(
                "comment" to reportReasonInfo,
                "source" to (source ?: "")
            )
            socialRepository.reportMember(userIdBeingReported, updateData)
            socialRepository.blockMember(userIdBeingReported)
            Toast.makeText(context, "$profileName Reported", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    companion object {
        const val TAG = "ReportBottomSheetFragment"
        const val REPORT_TYPE_MESSAGE = "report_type_message"
        const val REPORT_TYPE_USER = "report_type_user"

        private const val REPORTING_USER_ID = "reporting_user_id"
        private const val REPORT_TYPE = "report_type"
        private const val PROFILE_NAME = "profile_name"
        private const val MESSAGE_ID = "message_id"
        private const val MESSAGE_TEXT = "message_text"
        private const val GROUP_ID = "group_id"
        private const val SOURCE_VIEW = "source_view"


        fun newInstance(
            reportType: String,
            profileName: String = "",
            userIdBeingReported: String,
            messageId: String = "",
            messageText: String,
            groupId: String,
            sourceView: String
        ): ReportBottomSheetFragment {
            val args = Bundle()
            args.putString(REPORT_TYPE, reportType)
            args.putString(PROFILE_NAME, profileName)
            args.putString(REPORTING_USER_ID, userIdBeingReported)
            args.putString(MESSAGE_ID, messageId)
            args.putString(MESSAGE_TEXT, messageText)
            args.putString(GROUP_ID, groupId)
            args.putString(SOURCE_VIEW, sourceView)
            val fragment = ReportBottomSheetFragment()
            fragment.arguments = args
            return fragment
        }
    }
}

