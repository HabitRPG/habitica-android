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
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.members.PlayerTier
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.views.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.social.UsernameLabel
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_tavern_detail.*
import kotlinx.android.synthetic.main.shop_header.*
import javax.inject.Inject
import javax.inject.Named

class TavernDetailFragment : BaseFragment() {

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

    private var user: User? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_tavern_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shopSpriteSuffix = configManager.shopSpriteSuffix()

        compositeSubscription.add(userRepository.getUser(userId).subscribe(Consumer {
            this.user = it
            this.updatePausedState()
        }, RxErrorHandler.handleEmptyError()))

        descriptionView.setText(R.string.tavern_description)
        namePlate.setText(R.string.tavern_owner)

        npcBannerView.shopSpriteSuffix = configManager.shopSpriteSuffix()
        npcBannerView.identifier = "tavern"

        addPlayerTiers()
        bindButtons()

        compositeSubscription.add(socialRepository.getGroup(Group.TAVERN_ID)
                .doOnNext {  if (!it.hasActiveQuest) worldBossSection.visibility = View.GONE }
                .filter { it.hasActiveQuest }
                .doOnNext { questProgressView.progress = it.quest}
                .flatMapMaybe { inventoryRepository.getQuestContent(it.quest?.key ?: "").firstElement() }
                .subscribe(Consumer {
                    questProgressView.quest = it
                    worldBossSection.visibility = View.VISIBLE
                }, RxErrorHandler.handleEmptyError()))

        compositeSubscription.add(socialRepository.getGroup(Group.TAVERN_ID)
                .filter { it.hasActiveQuest }
                .doOnNext { descriptionView.setText(R.string.tavern_description_world_boss) }
                .filter { group -> group.quest?.rageStrikes?.any { it.key == "tavern" } ?: false }
                .filter { group -> group.quest?.rageStrikes?.filter { it.key == "tavern" }?.get(0)?.wasHit == true }
                .subscribe(Consumer {
                    val key = it.quest?.key
                    if (key != null) {
                        shopSpriteSuffix = key
                    }
                }, RxErrorHandler.handleEmptyError()))

        compositeSubscription.add(socialRepository.retrieveGroup(Group.TAVERN_ID).subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))

        user.notNull { questProgressView.configure(it) }
    }

    override fun onDestroy() {
        userRepository.close()
        socialRepository.close()
        inventoryRepository.close()
        super.onDestroy()
    }

    private fun bindButtons() {
        innButton.setOnClickListener {
            user?.notNull { user -> userRepository.sleep(user).subscribe(Consumer { }, RxErrorHandler.handleEmptyError()) }
        }
        guidelinesButton.setOnClickListener {
            MainNavigationController.navigate(R.id.guidelinesActivity)
        }
        faqButton.setOnClickListener {
            MainNavigationController.navigate(R.id.FAQOverviewFragment)
        }
        reportButton.setOnClickListener {
            MainNavigationController.navigate(R.id.aboutFragment)
        }

        worldBossSection.infoIconView.setOnClickListener {
            val context = this.context
            val quest = questProgressView.quest
            if (context != null && quest != null) {
                showWorldBossInfoDialog(context, quest)
            }
        }
    }


    private fun updatePausedState() {
        if (innButton == null) {
            return
        }
        if (user?.preferences?.sleep == true) {
            innButton .setText(R.string.tavern_inn_checkOut)
        } else {
            innButton.setText(R.string.tavern_inn_rest)
        }
    }

    private fun addPlayerTiers() {
        for (tier in PlayerTier.getTiers()) {
            context.notNull {
                val container = FrameLayout(it)
                container.background = ContextCompat.getDrawable(it, R.drawable.layout_rounded_bg_gray_700)
                val label = UsernameLabel(context, null)
                label.tier = tier.id
                label.username = tier.title
                val params = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER)
                container.addView(label, params)
                playerTiersView.addView(container)
                val padding = context?.resources?.getDimension(R.dimen.spacing_medium)?.toInt() ?: 0
                container.setPadding(0, padding, 0, padding)
            }
        }
        playerTiersView.invalidate()
    }

    override fun injectFragment(component: AppComponent) {
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
            background?.setColorFilter(quest.colors?.extraLightColor ?: 0, PorterDuff.Mode.MULTIPLY)
            promptView?.background = background

            alert.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.close)) { dialog, _ ->
                dialog.dismiss()
            }
            alert.show() }
    }
}
