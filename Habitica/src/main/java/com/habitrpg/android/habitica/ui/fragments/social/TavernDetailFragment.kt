package com.habitrpg.android.habitica.ui.fragments.social

import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentTavernDetailBinding
import com.habitrpg.android.habitica.extensions.setTintWith
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.shared.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.members.PlayerTier
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.shared.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.social.UsernameLabel
import javax.inject.Inject
import javax.inject.Named

class TavernDetailFragment : BaseFragment<FragmentTavernDetailBinding>() {

    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String
    @Inject
    lateinit var configManager: AppConfigManager

    private var shopSpriteSuffix = ""

    override var binding: FragmentTavernDetailBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentTavernDetailBinding {
        return FragmentTavernDetailBinding.inflate(inflater, container, false)
    }

    private var user: User? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shopSpriteSuffix = configManager.shopSpriteSuffix()

        compositeSubscription.add(userRepository.getUser(userId).subscribe({
            this.user = it
            this.updatePausedState()
        }, RxErrorHandler.handleEmptyError()))

        binding?.shopHeader?.descriptionView?.setText(R.string.tavern_description)
        binding?.shopHeader?.namePlate?.setText(R.string.tavern_owner)

        binding?.shopHeader?.npcBannerView?.shopSpriteSuffix = configManager.shopSpriteSuffix()
        binding?.shopHeader?.npcBannerView?.identifier = "tavern"

        addPlayerTiers()
        bindButtons()

        compositeSubscription.add(socialRepository.getGroup(Group.TAVERN_ID)
                .doOnNext {  if (!it.hasActiveQuest) binding?.worldBossSection?.visibility = View.GONE }
                .filter { it.hasActiveQuest }
                .doOnNext {
                    binding?.questProgressView?.progress = it.quest
                    binding?.shopHeader?.descriptionView?.setText(R.string.tavern_description_world_boss)
                    val filtered = it.quest?.rageStrikes?.filter { strike -> strike.key == "tavern" }
                    if (filtered?.size ?: 0 > 0 && filtered?.get(0)?.wasHit == true) {
                        val key = it.quest?.key
                        if (key != null) {
                            shopSpriteSuffix = key
                        }
                    }
                }
                .flatMapMaybe { inventoryRepository.getQuestContent(it.quest?.key ?: "").firstElement() }
                .subscribe({
                    binding?.questProgressView?.quest = it
                    binding?.worldBossSection?.visibility = View.VISIBLE
                }, RxErrorHandler.handleEmptyError()))

        compositeSubscription.add(socialRepository.retrieveGroup(Group.TAVERN_ID).subscribe({ }, RxErrorHandler.handleEmptyError()))

        user?.let { binding?.questProgressView?.configure(it) }
    }

    override fun onDestroy() {
        userRepository.close()
        socialRepository.close()
        inventoryRepository.close()
        super.onDestroy()
    }

    private fun bindButtons() {
        binding?.innButton?.setOnClickListener {
            user?.let { user -> userRepository.sleep(user).subscribe({ }, RxErrorHandler.handleEmptyError()) }
        }
        binding?.guidelinesButton?.setOnClickListener {
            MainNavigationController.navigate(R.id.guidelinesActivity)
        }
        binding?.faqButton?.setOnClickListener {
            MainNavigationController.navigate(R.id.FAQOverviewFragment)
        }
        binding?.reportButton?.setOnClickListener {
            MainNavigationController.navigate(R.id.aboutFragment)
        }

        binding?.worldBossSection?.infoIconView?.setOnClickListener {
            val context = this.context
            val quest = binding?.questProgressView?.quest
            if (context != null && quest != null) {
                showWorldBossInfoDialog(context, quest)
            }
        }
    }


    private fun updatePausedState() {
        if (binding?.innButton == null) {
            return
        }
        if (user?.preferences?.sleep == true) {
            binding?.innButton?.setText(R.string.tavern_inn_checkOut)
        } else {
            binding?.innButton?.setText(R.string.tavern_inn_rest)
        }
    }

    private fun addPlayerTiers() {
        for (tier in PlayerTier.getTiers()) {
            context?.let {
                val container = FrameLayout(it)
                container.background = ContextCompat.getDrawable(it, R.drawable.layout_rounded_bg_window)
                val label = UsernameLabel(context, null)
                label.tier = tier.id
                label.username = tier.title
                val params = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER)
                container.addView(label, params)
                binding?.playerTiersView?.addView(container)
                val padding = context?.resources?.getDimension(R.dimen.spacing_medium)?.toInt() ?: 0
                container.setPadding(0, padding, 0, padding)
            }
        }
        (binding?.playerTiersView?.parent as? ViewGroup)?.invalidate()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    companion object {

        fun showWorldBossInfoDialog(context: Context, quest: QuestContent) {
            val alert = HabiticaAlertDialog(context)
            val bossName = quest.boss?.name ?: ""
            alert.setTitle(R.string.world_boss_description_title)
            //alert.setSubtitle(context.getString(R.string.world_boss_description_subtitle, bossName))
            alert.setAdditionalContentView(R.layout.world_boss_description_view)

            val descriptionView = alert.getContentView()
            val promptView: TextView? = descriptionView?.findViewById(R.id.worldBossActionPromptView)
            promptView?.text = context.getString(R.string.world_boss_action_prompt, bossName)
            promptView?.setTextColor(quest.colors?.lightColor ?: 0)
            val background = ContextCompat.getDrawable(context, R.drawable.rounded_border)
            background?.setTintWith(quest.colors?.extraLightColor ?: 0, PorterDuff.Mode.MULTIPLY)
            promptView?.background = background

            alert.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.close)) { dialog, _ ->
                dialog.dismiss()
            }
            alert.show() }
    }
}
