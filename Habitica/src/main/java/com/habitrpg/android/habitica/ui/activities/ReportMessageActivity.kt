package com.habitrpg.android.habitica.ui.activities

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.ActivityReportMessageBinding
import com.habitrpg.android.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.helpers.setMarkdown
import kotlinx.coroutines.launch
import javax.inject.Inject

class ReportMessageActivity : BaseActivity() {

    private lateinit var binding: ActivityReportMessageBinding

    @Inject
    lateinit var socialRepository: SocialRepository

    private var raisedElevation = 0f

    private var messageID: String? = null
    private var groupID: String? = null
    private var isReporting: Boolean = false

    override fun getLayoutResId(): Int {
        return R.layout.activity_report_message
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
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
        binding.reportExplanationTextview.setMarkdown(getString(R.string.report_explanation))

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
        binding.titleTextView.text = getString(R.string.report_message_title, args.profileName)
        binding.messageTextView.text = args.text

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
            lifecycleScope.launch(ExceptionHandler.coroutine {
                isReporting = false
            }) {
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
