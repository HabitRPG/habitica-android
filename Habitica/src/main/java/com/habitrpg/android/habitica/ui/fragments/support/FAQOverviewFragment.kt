package com.habitrpg.android.habitica.ui.fragments.support

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.os.bundleOf
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.FAQRepository
import com.habitrpg.android.habitica.databinding.FragmentFaqOverviewBinding
import com.habitrpg.android.habitica.databinding.SupportFaqItemBinding
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.setMarkdown
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import io.reactivex.functions.Consumer
import javax.inject.Inject

class FAQOverviewFragment : BaseMainFragment<FragmentFaqOverviewBinding>() {

    override var binding: FragmentFaqOverviewBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentFaqOverviewBinding {
        return FragmentFaqOverviewBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var faqRepository: FAQRepository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        hidesToolbar = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.healthSection?.findViewById<ImageView>(R.id.icon_view)?.setImageBitmap(HabiticaIconsHelper.imageOfHeartLarge())
        binding?.experienceSection?.findViewById<ImageView>(R.id.icon_view)?.setImageBitmap(HabiticaIconsHelper.imageOfExperienceReward())
        binding?.manaSection?.findViewById<ImageView>(R.id.icon_view)?.setImageBitmap(HabiticaIconsHelper.imageOfMagicLarge())
        binding?.goldSection?.findViewById<ImageView>(R.id.icon_view)?.setImageBitmap(HabiticaIconsHelper.imageOfGoldReward())
        binding?.gemsSection?.findViewById<ImageView>(R.id.icon_view)?.setImageBitmap(HabiticaIconsHelper.imageOfGem())
        binding?.hourglassesSection?.findViewById<ImageView>(R.id.icon_view)?.setImageBitmap(HabiticaIconsHelper.imageOfHourglassLarge())
        binding?.statsSection?.findViewById<ImageView>(R.id.icon_view)?.setImageBitmap(HabiticaIconsHelper.imageOfStats())

        binding?.moreHelpTextView?.setMarkdown(context?.getString(R.string.need_help_header_description, "[Habitica Help Guild](https://habitica.com/groups/guild/5481ccf3-5d2d-48a9-a871-70a7380cee5a)"))
        binding?.moreHelpTextView?.setOnClickListener { MainNavigationController.navigate(R.id.guildFragment, bundleOf("groupID" to "5481ccf3-5d2d-48a9-a871-70a7380cee5a")) }
        binding?.moreHelpTextView?.movementMethod = LinkMovementMethod.getInstance()

        this.loadArticles()
    }

    override fun onDestroy() {
        faqRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun loadArticles() {
        compositeSubscription.add(faqRepository.getArticles().subscribe(Consumer {
            val context = context ?: return@Consumer
            for (article in it) {
                val binding = SupportFaqItemBinding.inflate(context.layoutInflater, binding?.faqLinearLayout, true)
                binding.textView.text = article.question
                binding.root.setOnClickListener {
                    val direction = FAQOverviewFragmentDirections.openFAQDetail(null, null)
                    direction.position = article.position ?: 0
                    MainNavigationController.navigate(direction)
                }
            }
        }, RxErrorHandler.handleEmptyError()))
    }

}
