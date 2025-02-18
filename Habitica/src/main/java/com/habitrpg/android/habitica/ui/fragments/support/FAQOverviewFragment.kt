package com.habitrpg.android.habitica.ui.fragments.support

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.FAQRepository
import com.habitrpg.android.habitica.databinding.FragmentFaqOverviewBinding
import com.habitrpg.android.habitica.databinding.SupportFaqItemBinding
import com.habitrpg.android.habitica.extensions.applyScrollContentWindowInsets
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.UsernameLabel
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.helpers.AppTestingLevel
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.models.PlayerTier
import com.jaredrummler.android.device.DeviceName
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max

@AndroidEntryPoint
class FAQOverviewFragment : BaseMainFragment<FragmentFaqOverviewBinding>() {
    private var deviceInfo: DeviceName.DeviceInfo? = null
    override var binding: FragmentFaqOverviewBinding? = null

    @Inject
    lateinit var appConfigManager: AppConfigManager

    @Inject
    lateinit var userViewModel: MainUserViewModel

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentFaqOverviewBinding {
        return FragmentFaqOverviewBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var faqRepository: FAQRepository

    @Inject
    lateinit var configManager: AppConfigManager

    private val versionName: String by lazy {
        try {
            mainActivity?.packageManager?.getPackageInfo(
                mainActivity?.packageName ?: "",
                0
            )?.versionName
                ?: ""
        } catch (e: PackageManager.NameNotFoundException) {
            ""
        }
    }

    private val versionCode: Int by lazy {
        try {
            @Suppress("DEPRECATION")
            mainActivity?.packageManager?.getPackageInfo(
                mainActivity?.packageName ?: "",
                0
            )?.versionCode
                ?: 0
        } catch (e: PackageManager.NameNotFoundException) {
            0
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        hidesToolbar = true
        showsBackButton = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding?.npcHeader?.npcBannerView?.shopSpriteSuffix = configManager.shopSpriteSuffix()
        binding?.npcHeader?.npcBannerView?.identifier = "tavern"
        binding?.npcHeader?.namePlate?.setText(R.string.tavern_owner)
        binding?.npcHeader?.descriptionView?.isVisible = false

        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            DeviceName.with(context).request { info, _ ->
                deviceInfo = info
            }
        }

        binding?.healthSection?.findViewById<ImageView>(R.id.icon_view)?.setImageBitmap(
            HabiticaIconsHelper.imageOfHeartLarge()
        )
        binding?.experienceSection?.findViewById<ImageView>(R.id.icon_view)?.setImageBitmap(
            HabiticaIconsHelper.imageOfExperienceReward()
        )
        binding?.manaSection?.findViewById<ImageView>(R.id.icon_view)?.setImageBitmap(
            HabiticaIconsHelper.imageOfMagicLarge()
        )
        binding?.goldSection?.findViewById<ImageView>(R.id.icon_view)?.setImageBitmap(
            HabiticaIconsHelper.imageOfGoldReward()
        )
        binding?.gemsSection?.findViewById<ImageView>(R.id.icon_view)?.setImageBitmap(
            HabiticaIconsHelper.imageOfGem()
        )
        binding?.hourglassesSection?.findViewById<ImageView>(R.id.icon_view)?.setImageBitmap(
            HabiticaIconsHelper.imageOfHourglassLarge()
        )
        binding?.statsSection?.findViewById<ImageView>(R.id.icon_view)?.setImageBitmap(
            HabiticaIconsHelper.imageOfStats()
        )

        binding?.contribTierSection?.findViewById<ImageView>(R.id.icon_view)
            ?.setImageResource(R.drawable.contributor_icon)
        addPlayerTiers()

        val fullText = getString(R.string.need_help_description)
        val clickableText = "contact us"
        val spannableString = SpannableStringBuilder(fullText)
        val clickableSpan =
            object : ClickableSpan() {
                override fun onClick(textView: View) {
                    sendEmail("[Android] Question")
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = false
                }
            }
        val startIndex = max(0, fullText.indexOf(clickableText))
        val endIndex = startIndex + clickableText.length
        spannableString.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding?.moreHelpTextView?.text = spannableString
        binding?.moreHelpTextView?.movementMethod = LinkMovementMethod.getInstance()

        this.loadArticles()

        binding?.scrollContent?.let { applyScrollContentWindowInsets(it) }
    }

    override fun onDestroy() {
        faqRepository.close()
        super.onDestroy()
    }

    private fun loadArticles() {
        lifecycleScope.launchCatching {
            faqRepository.getArticles().collect {
                val context = context ?: return@collect
                if (binding?.faqLinearLayout == null) return@collect
                for (article in it) {
                    val binding =
                        SupportFaqItemBinding.inflate(
                            context.layoutInflater,
                            binding?.faqLinearLayout,
                            true
                        )
                    binding.textView.text = article.question
                    binding.root.setOnClickListener {
                        val direction = FAQOverviewFragmentDirections.openFAQDetail(null, null)
                        direction.position = article.position ?: 0
                        MainNavigationController.navigate(direction)
                    }
                }
            }
        }
    }

    private fun sendEmail(subject: String) {
        val version = Build.VERSION.SDK_INT
        val deviceName = deviceInfo?.name ?: DeviceName.getDeviceName()
        val manufacturer = deviceInfo?.manufacturer ?: Build.MANUFACTURER
        val newLine = "%0D%0A"
        var bodyOfEmail =
            Uri.encode("Device: $manufacturer $deviceName") +
                newLine + Uri.encode("Android Version: $version") +
                newLine +
                Uri.encode(
                    "AppVersion: " +
                        getString(
                            R.string.version_info,
                            versionName,
                            versionCode
                        )
                )

        if (appConfigManager.testingLevel().name != AppTestingLevel.PRODUCTION.name) {
            bodyOfEmail += " " + Uri.encode(appConfigManager.testingLevel().name)
        }
        bodyOfEmail += newLine + Uri.encode("User ID: ${userViewModel.userID}")

        userViewModel.user.value?.let { user ->
            bodyOfEmail += newLine + Uri.encode("Level: " + (user.stats?.lvl ?: 0)) +
                newLine +
                Uri.encode(
                    "Class: " + (
                        if (user.preferences?.disableClasses == true) {
                            "Disabled"
                        } else {
                            (
                                user.stats?.habitClass
                                    ?: "None"
                                )
                        }
                        )
                ) +
                newLine + Uri.encode("Is in Inn: " + (user.preferences?.sleep ?: false)) +
                newLine + Uri.encode("Uses Costume: " + (user.preferences?.costume ?: false)) +
                newLine + Uri.encode("Custom Day Start: " + (user.preferences?.dayStart ?: 0)) +
                newLine +
                Uri.encode(
                    "Timezone Offset: " + (user.preferences?.timezoneOffset ?: 0)
                )
        }

        bodyOfEmail += "%0D%0ADetails:%0D%0A%0D%0A"

        mainActivity?.let {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            val mailto =
                "mailto:" + appConfigManager.supportEmail() +
                    "?subject=" + Uri.encode(subject) +
                    "&body=" + bodyOfEmail
            emailIntent.data = Uri.parse(mailto)

            startActivity(Intent.createChooser(emailIntent, "Choose an Email client:"))
        }
    }

    private fun addPlayerTiers() {
        val tiers = PlayerTier.getTiers()
        for (tier in tiers) {
            context?.let {
                val container = FrameLayout(it)
                container.background = ContextCompat.getDrawable(it, R.drawable.rounded_border)
                container.background.setTint(PlayerTier.getColorForTier(it, tier.id))
                container.background.alpha = 50
                val label = UsernameLabel(it, null)
                label.tier = tier.id
                label.username = tier.title
                val params =
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER
                    )
                container.addView(label, params)
                container.isVisible = false
                binding?.contribTierSection?.addView(container)
                val containerParams = container.layoutParams as LinearLayout.LayoutParams
                containerParams.setMargins(
                    12.dpToPx(context),
                    0,
                    12.dpToPx(context),
                    if (tiers.last() == tier) 12.dpToPx(context) else 6.dpToPx(context)
                )
                val padding = context?.resources?.getDimension(R.dimen.spacing_medium)?.toInt() ?: 0
                container.setPadding(0, padding, 0, padding)
            }
        }
        (binding?.contribTierSection?.parent as? ViewGroup)?.invalidate()
    }
}
