package com.habitrpg.android.habitica.ui.fragments.support

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.FAQRepository
import com.habitrpg.android.habitica.databinding.FragmentFaqOverviewBinding
import com.habitrpg.android.habitica.databinding.SupportFaqItemBinding
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.UsernameLabel
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.helpers.setMarkdown
import com.habitrpg.common.habitica.models.PlayerTier
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FAQOverviewFragment : BaseMainFragment<FragmentFaqOverviewBinding>() {

    override var binding: FragmentFaqOverviewBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentFaqOverviewBinding {
        return FragmentFaqOverviewBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var faqRepository: FAQRepository
    @Inject
    lateinit var configManager: AppConfigManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        hidesToolbar = true
        showsBackButton = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

        binding?.contribTierSection?.findViewById<ImageView>(R.id.icon_view)?.setImageResource(R.drawable.contributor_icon)
        addPlayerTiers()

        binding?.moreHelpTextView?.setMarkdown(context?.getString(R.string.need_help_header_description, "[Habitica Help Guild](https://habitica.com/groups/guild/5481ccf3-5d2d-48a9-a871-70a7380cee5a)"))
        binding?.moreHelpTextView?.setOnClickListener { MainNavigationController.navigate(R.id.guildFragment, bundleOf("groupID" to "5481ccf3-5d2d-48a9-a871-70a7380cee5a")) }
        binding?.moreHelpTextView?.movementMethod = LinkMovementMethod.getInstance()

        this.loadArticles()
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
                    val binding = SupportFaqItemBinding.inflate(
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
                val params = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
                container.addView(label, params)
                container.isVisible = false
                binding?.contribTierSection?.addView(container)
                val containerParams = container.layoutParams as LinearLayout.LayoutParams
                containerParams.setMargins(12.dpToPx(context), 0, 12.dpToPx(context), if (tiers.last() == tier) 12.dpToPx(context) else 6.dpToPx(context))
                val padding = context?.resources?.getDimension(R.dimen.spacing_medium)?.toInt() ?: 0
                container.setPadding(0, padding, 0, padding)
            }
        }
        (binding?.contribTierSection?.parent as? ViewGroup)?.invalidate()
    }
}
