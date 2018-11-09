package com.habitrpg.android.habitica.ui.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.plattysoft.leonids.ParticleSystem
import kotlinx.android.synthetic.main.fragment_about.*
import android.view.animation.AccelerateInterpolator



class AboutFragment : Fragment() {

    internal var userId = ""
    private val androidSourceCodeLink = "https://github.com/HabitRPG/habitrpg-android/"
    private val twitterLink = "https://twitter.com/habitica"

    private var versionNumberTappedCount = 0

    private fun openGooglePlay() {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = "market://details?id=com.habitrpg.android.habitica".toUri()
        startActivity(intent)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //Gets the userId that was passed from MainActivity -> MainDrawerBuilder -> About Activity
        userId = this.activity?.intent?.getStringExtra("userId") ?: ""
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        versionInfo.setOnClickListener {
            versionNumberTappedCount += 1
            when (versionNumberTappedCount) {
                1 -> context.notNull { context ->
                    Toast.makeText(context, "Oh! You tapped me!", Toast.LENGTH_SHORT).show()
                }
                in 4..7 -> context.notNull { context ->
                    Toast.makeText(context, "Only ${8 - versionNumberTappedCount} taps left!", Toast.LENGTH_SHORT).show()
                }
                8 -> {
                    context.notNull { context ->
                        Toast.makeText(context, "You were blessed with cats!", Toast.LENGTH_SHORT).show()
                    }
                    doTheThing()
                }
            }
        }
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
        val uriUrl = url.toUri()
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

    private fun doTheThing() {
        DataBindingUtils.loadImage("Pet-Sabretooth-Base") {bitmap ->
            activity?.runOnUiThread {
                activity.notNull {
                    ParticleSystem(it, 50, bitmap, 3000)
                            .setAcceleration(0.00013f, 90)
                            .setSpeedByComponentsRange(-0.08f, 0.08f, 0.05f, 0.1f)
                            .setFadeOut(200, AccelerateInterpolator())
                            .setRotationSpeed(100f)
                            .emitWithGravity(anchor, Gravity.BOTTOM, 20, 10000)
                }
            }
        }
        DataBindingUtils.loadImage("Pet-Sabretooth-Golden") {bitmap ->
            activity?.runOnUiThread {
                activity.notNull {
                    ParticleSystem(it, 50, bitmap, 3000)
                            .setAcceleration(0.00013f, 90)
                            .setSpeedByComponentsRange(-0.08f, 0.08f, 0.05f, 0.1f)
                            .setFadeOut(200, AccelerateInterpolator())
                            .setRotationSpeed(100f)
                            .emitWithGravity(anchor, Gravity.BOTTOM, 20, 10000)
                }
            }
        }
        DataBindingUtils.loadImage("Pet-Sabretooth-Red") {bitmap ->
            activity?.runOnUiThread {
                activity.notNull {
                    ParticleSystem(it, 50, bitmap, 3000)
                            .setAcceleration(0.00013f, 90)
                            .setSpeedByComponentsRange(-0.08f, 0.08f, 0.05f, 0.1f)
                            .setFadeOut(200, AccelerateInterpolator())
                            .setRotationSpeed(100f)
                            .emitWithGravity(anchor, Gravity.BOTTOM, 20, 10000)
                }
            }
        }
    }
}
