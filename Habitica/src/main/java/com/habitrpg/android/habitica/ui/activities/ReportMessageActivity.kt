package com.habitrpg.android.habitica.ui.activities

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.ActivityReportMessageBinding
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.setMarkdown
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReportMessageActivity : BaseActivity() {

    private lateinit var binding: ActivityReportMessageBinding

    @Inject
    lateinit var socialRepository: SocialRepository

    private var raisedElevation = 0f

    private var messageID: String? = null
    private var displayName: String? = null
    private var groupID: String? = null
    private var isReporting: Boolean = false
    private var isReportUser: Boolean = false
    private var userId: String? = null

    override fun getLayoutResId(): Int {
        return R.layout.activity_report_message
    }

    override fun getContentView(layoutResId: Int?): View {
        binding = ActivityReportMessageBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""
        raisedElevation = binding.appBar.elevation
        setStatusBarDim(true)

        binding.bottomSheet.setOnTouchListener { _, _ -> true }
        binding.touchOutside.setOnClickListener { finish() }
        binding.reportExplanationTextview.setMarkdown(getString(R.string.report_post_explanation))

        BottomSheetBehavior.from<View>(binding.bottomSheet)
            .addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                @SuppressLint("SwitchIntDef")
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_HIDDEN -> finish()
                        BottomSheetBehavior.STATE_EXPANDED -> setStatusBarDim(false)
                        else -> setStatusBarDim(true)
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    // no op
                }
            })

        val args = navArgs<ReportMessageActivityArgs>().value
        messageID = args.messageID
        groupID = args.groupID
        displayName = args.profileName
        isReportUser = args.isReportUser
        userId = args.userId

        if (isReportUser) {
            binding.titleTextView.visibility = View.GONE
            binding.messageTextView.visibility = View.GONE
            binding.toolbarTitle.text = getString(R.string.report_user_title, args.profileName)
            binding.closeButton.visibility = View.VISIBLE
            binding.additionalExplanationTextview.visibility = View.VISIBLE
            binding.additionalExplanationTextview.text = getString(R.string.report_user_description, displayName ?: "")
            binding.additionalInfoInputLayout.hint = getString(R.string.report_user_hint)
            binding.reportExplanationTextview.text = getString(R.string.report_user_explanation)
        } else {
            binding.additionalExplanationTextview.visibility = View.GONE
            binding.titleTextView.text = getString(R.string.report_message_title, args.profileName)
            binding.messageTextView.text = args.text
        }

        binding.reportButton.setOnClickListener { reportMessage() }
        binding.closeButton.setOnClickListener { finish() }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
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
                if (isReportUser) {
                    if (binding.additionalInfoEdittext.text.isNullOrBlank()) {
                        binding.additionalInfoInputLayout.error = getString(R.string.report_user_hint)
                        isReporting = false
                        return@launch
                    }
                    socialRepository.flagMessage("Reporting User ${displayName ?: ""}: ${userId ?: ""}",
                        binding.additionalInfoEdittext.text.toString(), null
                    )
                    userId?.let { userId ->
                        socialRepository.blockMember(userId)
                    }
                } else {
                    socialRepository.flagMessage(messageID ?: "",
                        binding.additionalInfoEdittext.text.toString(), groupID
                    )
                }
                finish()
            }
        }
    }

    private fun setStatusBarDim(dim: Boolean) {
        if (dim) {
            binding.appBar.elevation = 0f
            window.statusBarColor = getThemeColor(R.attr.colorPrimaryDark)
            binding.closeButton.visibility = View.GONE
            binding.toolbarTitle.setTypeface(null, Typeface.BOLD)
        } else {
            binding.appBar.elevation = 8f
            window.statusBarColor = ContextCompat.getColor(this, R.color.offset_background)
            binding.closeButton.visibility = View.VISIBLE
            binding.toolbarTitle.setTypeface(null, Typeface.NORMAL)
        }

        if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            setSystemBarTheme(dim)
        }
    }

    override fun finish() {
        dismissKeyboard()
        super.finish()
    }

    @RequiresApi(api = VERSION_CODES.M)
    fun setSystemBarTheme(isDark: Boolean) {
        // Fetch the current flags.
        val lFlags = window.decorView.systemUiVisibility
        // Update the SystemUiVisibility dependening on whether we want a Light or Dark theme.
        window.decorView.systemUiVisibility = if (isDark) lFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() else lFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}
