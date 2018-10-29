package com.habitrpg.android.habitica.ui.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import kotlinx.android.synthetic.main.fragment_about.*

class AboutFragment : Fragment() {

    internal var userId = ""
    private val androidSourceCodeLink = "https://github.com/HabitRPG/habitrpg-android/"
    private val twitterLink = "https://twitter.com/habitica"

    private fun openGooglePlay() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("market://details?id=com.habitrpg.android.habitica")
        startActivity(intent)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //Gets the userId that was passed from MainActivity -> MainDrawerBuilder -> About Activity
        userId = this.activity?.intent?.getStringExtra("userId") ?: ""
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    private val versionName: String by lazy {
        try {
            @Suppress("DEPRECATION")
            activity?.packageManager?.getPackageInfo(activity?.packageName, 0)?.versionName ?: ""
        } catch (e: PackageManager.NameNotFoundException) {
            ""
        }
    }

    private val versionCode: Int by lazy {
        try {
            @Suppress("DEPRECATION")
            activity?.packageManager?.getPackageInfo(activity?.packageName, 0)?.versionCode ?: 0
        } catch (e: PackageManager.NameNotFoundException) {
            0
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        versionInfo.text = getString(R.string.version_info, versionName, versionCode)

        sourceCodeLink.setOnClickListener { openBrowserLink(androidSourceCodeLink) }
        twitter.setOnClickListener { openBrowserLink(twitterLink) }
        sourceCodeButton.setOnClickListener { openBrowserLink(androidSourceCodeLink) }
        reportBug.setOnClickListener { sendEmail("[Android] Bugreport") }
        sendFeedback.setOnClickListener { sendEmail("[Android] Feedback") }
        googlePlayStoreButton.setOnClickListener { openGooglePlay() }
    }

    private fun openBrowserLink(url: String) {
        val uriUrl = Uri.parse(url)
        val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
        startActivity(launchBrowser)
    }

    private fun sendEmail(subject: String) {
        val version = Build.VERSION.SDK_INT
        val device = Build.DEVICE
        val bodyOfEmail = "Device: " + device +
                " \nAndroid Version: " + version +
                " \nAppVersion: " + getString(R.string.version_info, versionName, versionCode) +
                " \nUser ID: " + userId +
                " \nDetails: "

        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "mobile@habitica.com", null))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        emailIntent.putExtra(Intent.EXTRA_TEXT, bodyOfEmail)
        startActivity(Intent.createChooser(emailIntent, "Send email..."))
    }
}
