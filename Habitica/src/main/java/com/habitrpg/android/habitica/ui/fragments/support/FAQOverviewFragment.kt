package com.habitrpg.android.habitica.ui.fragments.support

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.FAQRepository
import com.habitrpg.android.habitica.databinding.FragmentFaqOverviewBinding
import com.habitrpg.android.habitica.databinding.SupportFaqItemBinding
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.UsernameLabel
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.helpers.AppTestingLevel
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.models.PlayerTier
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.max
import androidx.core.net.toUri

@AndroidEntryPoint
class FAQOverviewFragment : BaseMainFragment<FragmentFaqOverviewBinding>() {
    override var binding: FragmentFaqOverviewBinding? = null

    @Inject
    lateinit var appConfigManager: AppConfigManager

    @Inject
    lateinit var userViewModel: MainUserViewModel

    private var searchJob: Job? = null
    private val collapsibleItems = mutableListOf<SearchableFAQItem.CollapsibleItem>()
    private var navigableItems = mutableListOf<SearchableFAQItem.NavigableItem>()
    private var currentSearchQuery = ""

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
        } catch (_: PackageManager.NameNotFoundException) {
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
        } catch (_: PackageManager.NameNotFoundException) {
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

        setupCollapsibleItems()
        setupSearchBar()
        this.loadArticles()
    }

    override fun onResume() {
        super.onResume()
        if (currentSearchQuery.isNotEmpty()) {
            performSearch(currentSearchQuery)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
                navigableItems.clear()
                for (article in it) {
                    navigableItems.add(SearchableFAQItem.NavigableItem(article))

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
        val deviceName = Build.MODEL
        val manufacturer = Build.MANUFACTURER
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
                newLine + Uri.encode("Damage paused: " + (user.preferences?.sleep ?: false)) +
                newLine + Uri.encode("Uses Costume: " + (user.preferences?.costume ?: false)) +
                newLine + Uri.encode("Custom Day Start: " + (user.preferences?.dayStart ?: 0)) +
                newLine + Uri.encode("Analytics Enabled: " + (user.preferences?.analyticsConsent ?: "No Response")) +
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
            emailIntent.data = mailto.toUri()

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

    private fun setupCollapsibleItems() {
        collapsibleItems.clear()
        binding?.healthSection?.let { section ->
            collapsibleItems.add(
                SearchableFAQItem.CollapsibleItem(
                    title = getString(R.string.health_points),
                    subtitle = "HP",
                    description = getString(R.string.health_description),
                    collapsibleSection = section
                )
            )
        }

        binding?.experienceSection?.let { section ->
            collapsibleItems.add(
                SearchableFAQItem.CollapsibleItem(
                    title = getString(R.string.experience_points),
                    subtitle = "EXP",
                    description = getString(R.string.experience_description),
                    collapsibleSection = section
                )
            )
        }

        binding?.manaSection?.let { section ->
            collapsibleItems.add(
                SearchableFAQItem.CollapsibleItem(
                    title = getString(R.string.mana_points),
                    subtitle = "MP",
                    description = getString(R.string.mana_description),
                    collapsibleSection = section
                )
            )
        }

        binding?.goldSection?.let { section ->
            collapsibleItems.add(
                SearchableFAQItem.CollapsibleItem(
                    title = getString(R.string.gold_capitalized),
                    subtitle = getString(R.string.currency),
                    description = getString(R.string.gold_description),
                    collapsibleSection = section
                )
            )
        }

        binding?.gemsSection?.let { section ->
            collapsibleItems.add(
                SearchableFAQItem.CollapsibleItem(
                    title = getString(R.string.gems),
                    subtitle = getString(R.string.premium_currency),
                    description = getString(R.string.gems_description),
                    collapsibleSection = section
                )
            )
        }

        binding?.hourglassesSection?.let { section ->
            collapsibleItems.add(
                SearchableFAQItem.CollapsibleItem(
                    title = getString(R.string.mystic_hourglasses),
                    subtitle = getString(R.string.subscriber_currency),
                    description = getString(R.string.hourglasses_description),
                    collapsibleSection = section
                )
            )
        }

        binding?.statsSection?.let { section ->
            collapsibleItems.add(
                SearchableFAQItem.CollapsibleItem(
                    title = getString(R.string.stat_allocation),
                    subtitle = "STR, CON, INT, PER",
                    description = getString(R.string.stat_description),
                    collapsibleSection = section
                )
            )
        }

        binding?.contribTierSection?.let { section ->
            collapsibleItems.add(
                SearchableFAQItem.CollapsibleItem(
                    title = getString(R.string.contributor_tiers),
                    subtitle = "Habitica helpers",
                    description = getString(R.string.contrib_tier_description),
                    collapsibleSection = section
                )
            )
        }
    }

    private fun setupSearchBar() {
        binding?.searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString() ?: ""
                performSearch(query)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()
        currentSearchQuery = query.trim()

        if (currentSearchQuery.isEmpty()) {
            showOriginalContent()
            return
        }

        searchJob = lifecycleScope.launch {
            delay(200)

            val results = searchFAQItems(currentSearchQuery)

            if (results.isEmpty()) {
                showEmptyState()
            } else {
                showSearchResults(results)
            }
        }
    }

    private fun searchFAQItems(query: String): List<SearchableFAQItem> {
        val queryLower = query.lowercase()
        val results = mutableListOf<SearchableFAQItem>()

        for (item in collapsibleItems) {
            val titleMatch = item.title.lowercase().contains(queryLower)
            val subtitleMatch = item.subtitle?.lowercase()?.contains(queryLower) == true
            val descriptionMatch = item.description.lowercase().contains(queryLower)

            if (titleMatch || subtitleMatch || descriptionMatch) {
                results.add(item.copy(matchSnippet = null))
            }
        }

        for (item in navigableItems) {
            val article = item.article
            val questionMatch = article.question?.lowercase()?.contains(queryLower) == true
            val answerMatch = article.answer?.lowercase()?.contains(queryLower) == true

            if (questionMatch || answerMatch) {
                results.add(item.copy(matchSnippet = null))
            }
        }

        return results
    }

    private fun generateSnippet(text: String, query: String): String {
        val cleanText = text.replace(Regex("\\*\\*|\\n"), " ").replace(Regex("\\s+"), " ")
        val index = cleanText.lowercase().indexOf(query)

        if (index == -1) return ""

        val snippetStart = maxOf(0, index - 30)
        val snippetEnd = minOf(cleanText.length, index + query.length + 30)

        var snippet = cleanText.substring(snippetStart, snippetEnd)
        if (snippetStart > 0) snippet = "...$snippet"
        if (snippetEnd < cleanText.length) snippet = "$snippet..."

        return snippet
    }

    private fun showOriginalContent() {
        restoreCollapsibleSectionsToOriginal()
        binding?.originalContentContainer?.isVisible = true
        binding?.searchResultsContainer?.isVisible = false
        binding?.searchEmptyState?.isVisible = false
    }

    private fun restoreCollapsibleSectionsToOriginal() {
        val searchContainer = binding?.searchResultsContainer
        val originalContainer = binding?.originalContentContainer

        if (originalContainer != null && searchContainer != null) {
            val sections = listOf(
                binding?.healthSection,
                binding?.experienceSection,
                binding?.manaSection,
                binding?.goldSection,
                binding?.gemsSection,
                binding?.hourglassesSection,
                binding?.statsSection,
                binding?.contribTierSection
            )

            sections.forEach { section ->
                if (section != null && section.parent == searchContainer) {
                    searchContainer.removeView(section)
                    val insertIndex = findInsertionIndex(originalContainer, section.id)
                    originalContainer.addView(section, insertIndex)
                }
            }
        }
    }

    private fun findInsertionIndex(container: ViewGroup, sectionId: Int): Int {
        val expectedOrder = mapOf(
            R.id.health_section to 1,
            R.id.experience_section to 2,
            R.id.mana_section to 3,
            R.id.gold_section to 4,
            R.id.gems_section to 5,
            R.id.hourglasses_section to 6,
            R.id.stats_section to 7,
            R.id.contrib_tier_section to 8
        )
        val currentOrder = expectedOrder[sectionId] ?: return container.childCount

        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            val childOrder = expectedOrder[child.id] ?: continue

            if (childOrder > currentOrder) {
                return i
            }
        }

        return container.childCount
    }

    private fun showEmptyState() {
        binding?.originalContentContainer?.isVisible = false
        binding?.searchResultsContainer?.isVisible = false
        binding?.searchEmptyState?.isVisible = true
    }

    private fun showSearchResults(results: List<SearchableFAQItem>) {
        val context = context ?: return

        binding?.originalContentContainer?.isVisible = false
        binding?.searchEmptyState?.isVisible = false
        binding?.searchResultsContainer?.isVisible = true

        val searchContainer = binding?.searchResultsContainer
        if (searchContainer != null) {
            searchContainer.removeAllViews()
        }

        results.forEachIndexed { index, result ->
            when (result) {
                is SearchableFAQItem.CollapsibleItem -> {
                    val section = result.collapsibleSection
                    val parent = section.parent as? ViewGroup

                    parent?.removeView(section)
                    section.alpha = 0f
                    section.translationY = 20f
                    binding?.searchResultsContainer?.addView(section)

                    section.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(200)
                        .setStartDelay((index * 30).toLong())
                        .start()

                    result.matchSnippet?.let { snippet ->
                        val snippetView = TextView(context)
                        snippetView.text = highlightQuery(snippet, currentSearchQuery)
                        snippetView.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                        snippetView.textSize = 13f
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.setMargins(
                            context.resources.getDimensionPixelSize(R.dimen.spacing_large),
                            0,
                            context.resources.getDimensionPixelSize(R.dimen.spacing_large),
                            context.resources.getDimensionPixelSize(R.dimen.spacing_medium)
                        )
                        snippetView.layoutParams = params
                        snippetView.alpha = 0f
                        snippetView.setOnClickListener {
                            section.performClick()
                        }
                        binding?.searchResultsContainer?.addView(snippetView)

                        snippetView.animate()
                            .alpha(1f)
                            .setDuration(200)
                            .setStartDelay((index * 30 + 50).toLong())
                            .start()
                    }
                }
                is SearchableFAQItem.NavigableItem -> {
                    val itemBinding = SupportFaqItemBinding.inflate(
                        context.layoutInflater,
                        binding?.searchResultsContainer,
                        false
                    )
                    itemBinding.textView.text = result.title
                    itemBinding.root.setOnClickListener {
                        val direction = FAQOverviewFragmentDirections.openFAQDetail(null, null)
                        direction.position = result.article.position ?: 0
                        MainNavigationController.navigate(direction)
                    }

                    itemBinding.root.alpha = 0f
                    itemBinding.root.translationY = 20f
                    binding?.searchResultsContainer?.addView(itemBinding.root)

                    itemBinding.root.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(200)
                        .setStartDelay((index * 30).toLong())
                        .start()
                }
            }
        }
    }

    private fun highlightQuery(text: String, query: String): SpannableString {
        val spannable = SpannableString(text)
        val startIndex = text.lowercase().indexOf(query.lowercase())

        if (startIndex >= 0) {
            spannable.setSpan(
                StyleSpan(android.graphics.Typeface.BOLD),
                startIndex,
                startIndex + query.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return spannable
    }
}
