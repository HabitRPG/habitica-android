package com.habitrpg.android.habitica.ui.views.social

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.drawable.toBitmap
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.QuestCollectBinding
import com.habitrpg.android.habitica.databinding.QuestProgressBinding
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.inventory.QuestProgressCollect
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.views.HabiticaIcons
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.NPCBannerView
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.common.habitica.extensions.DataBindingUtils
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.helpers.setMarkdown
import kotlinx.coroutines.MainScope

class QuestProgressView : LinearLayout {
    private val binding = QuestProgressBinding.inflate(context.layoutInflater, this, true)

    private val rect = RectF()
    private val displayDensity = context.resources.displayMetrics.density

    var quest: QuestContent? = null
        set(value) {
            field = value
            configure()
        }
    var progress: Quest? = null
        set(value) {
            field = value
            configure()
        }

    constructor(context: Context) : super(context) {
        setupView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupView(context)
    }

    private var preferences: SharedPreferences? = null

    private fun setupView(context: Context) {
        setWillNotDraw(false)

        binding.caretView.setImageBitmap(HabiticaIconsHelper.imageOfCaret(ContextCompat.getColor(context, R.color.white), true))
        binding.questImageTitle.setOnClickListener {
            if (binding.questImageWrapper.visibility == View.VISIBLE) {
                hideQuestImage()
            } else {
                showQuestImage()
            }
        }

        binding.pendingDamageIconView.setImageBitmap(HabiticaIconsHelper.imageOfDamage())
        binding.bossHealthView.setSecondaryIcon(HabiticaIconsHelper.imageOfHeartDarkBg())
        binding.bossRageView.setSecondaryIcon(HabiticaIconsHelper.imageOfRage())

        binding.rageStrikeDescriptionView.setOnClickListener { showStrikeDescriptionAlert() }

        val density = resources.displayMetrics.density

        binding.questDescriptionSection.setCaretOffset((12 * density).toInt())

        preferences = context.getSharedPreferences("collapsible_sections", 0)
        if (preferences?.getBoolean("boss_art_collapsed", false) == true) {
            hideQuestImage()
        } else {
            showQuestImage()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        if (quest?.isValid == true) {
            val colors = quest?.colors
            if (colors != null) {
                rect.set(0.0f, 0.0f, width.toFloat() / displayDensity, height.toFloat() / displayDensity)
                canvas?.scale(displayDensity, displayDensity)
                HabiticaIcons.drawQuestBackground(canvas, rect, colors.darkColor, colors.mediumColor, colors.extraLightColor)
                canvas?.scale(1.0f / displayDensity, 1.0f / displayDensity)
            }
        }
        super.onDraw(canvas)
    }

    private fun configure() {
        val quest = this.quest
        val progress = this.progress
        if (quest == null || progress == null || !quest.isValid || !progress.isValid) {
            return
        }
        binding.collectionContainer.removeAllViews()
        if (quest.isBossQuest) {
            binding.bossNameView.text = quest.boss?.name
            binding.bossNameView.visibility = View.VISIBLE
            binding.bossHealthView.visibility = View.VISIBLE
            binding.bossHealthView.set(progress.progress?.hp ?: 0.0, quest.boss?.hp?.toDouble() ?: 0.0)
            binding.collectedItemsNumberView.visibility = View.GONE

            if (quest.boss?.hasRage == true) {
                binding.rageMeterView.visibility = View.VISIBLE
                binding.bossRageView.visibility = View.VISIBLE
                binding.rageMeterView.text = quest.boss?.rage?.title
                binding.bossRageView.set(progress.progress?.rage ?: 0.0, quest.boss?.rage?.value ?: 0.0)
                if (progress.hasRageStrikes()) {
                    setupRageStrikeViews()
                } else {
                    binding.rageStrikeDescriptionView.visibility = View.GONE
                }
            } else {
                binding.rageMeterView.visibility = View.GONE
                binding.bossRageView.visibility = View.GONE
                binding.rageStrikeDescriptionView.visibility = View.GONE
            }
        } else {
            binding.bossNameView.visibility = View.GONE
            binding.bossHealthView.visibility = View.GONE
            binding.rageMeterView.visibility = View.GONE
            binding.bossRageView.visibility = View.GONE
            binding.rageStrikeDescriptionView.visibility = View.GONE
            binding.collectedItemsNumberView.visibility = View.VISIBLE

            val collection = progress.progress?.collect
            if (collection != null) {
                setCollectionViews(collection, quest)
            }
        }
        binding.questDescription.setMarkdown(quest.notes)
        binding.questImageView.loadImage("quest_" + quest.key, "gif")
        binding.questFlourishesImageView.loadImage("quest_" + quest.key + "_flourishes")
        val lightColor = quest.colors?.lightColor
        if (lightColor != null) {
            binding.questImageSeparator.setBackgroundColor(lightColor)

            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(ContextCompat.getColor(context, R.color.transparent), lightColor)
            )
            gradientDrawable.cornerRadius = 0f
            binding.questImageWrapper.background = gradientDrawable
        }
        updateCaretImage()
        binding.questDescriptionSection.caretColor = quest.colors?.extraLightColor ?: 0
        binding.artCreditTextView.setTextColor(quest.colors?.extraLightColor ?: 0)
    }

    fun configure(user: User) {
        binding.pendingDamageTextView.text = context.getString(R.string.damage_pending, (user.party?.quest?.progress?.up ?: 0F))
        val collectedItems = user.party?.quest?.progress?.collectedItems ?: 0
        binding.collectedItemsNumberView.text = context.getString(R.string.quest_items_found, collectedItems)
    }

    private fun setupRageStrikeViews() {
        binding.rageStrikeDescriptionView.visibility = View.VISIBLE
        binding.rageStrikeDescriptionView.text = context.getString(R.string.rage_strike_count, progress?.activeRageStrikeNumber, progress?.rageStrikes?.size ?: 0)

        binding.rageStrikeContainer.removeAllViews()
        progress?.rageStrikes?.sortedByDescending { it.wasHit }?.forEach { strike ->
            val iconView = ImageView(context)
            if (strike.wasHit) {
                DataBindingUtils.loadImage(context, "rage_strike_${strike.key}") {
                    MainScope().launchCatching {
                        val bitmap = it.toBitmap()
                        val displayDensity = resources.displayMetrics.density
                        val width = bitmap.width * displayDensity
                        val height = bitmap.height * displayDensity
                        val scaledImage = Bitmap.createScaledBitmap(bitmap, width.toInt(), height.toInt(), false)
                        iconView.setImageBitmap(HabiticaIconsHelper.imageOfRageStrikeActive(context, scaledImage))
                        iconView.setOnClickListener {
                            showActiveStrikeAlert(strike.key)
                        }
                    }
                }
            } else {
                iconView.setImageBitmap(HabiticaIconsHelper.imageOfRageStrikeInactive())
                iconView.setOnClickListener { showPendingStrikeAlert() }
            }
            val params = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val spacing = context.resources.getDimension(R.dimen.spacing_medium).toInt()
            params.setMargins(spacing, 0, spacing, 0)
            binding.rageStrikeContainer.addView(iconView, params)
        }
    }

    private fun showActiveStrikeAlert(key: String) {
        val alert = HabiticaAlertDialog(context)
        alert.setTitle(context.getString(R.string.strike_active_title, getLocationName(key)))
//        alert.setSubtitle(context.getString(R.string.strike_active_subtitle, getNpcName(key)))
        alert.setMessage(context.getString(R.string.strike_active_description, getLongNPCName(key), quest?.boss?.name ?: "", getLocationName(key)))

        val npcBannerView = NPCBannerView(context, null)
        npcBannerView.shopSpriteSuffix = quest?.key ?: ""
        npcBannerView.identifier = key
        alert.setAdditionalContentView(npcBannerView)

        alert.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.close)) { dialog, _ ->
            dialog.dismiss()
        }
        alert.show()
    }

    private fun showPendingStrikeAlert() {
        val alert = HabiticaAlertDialog(context)
        alert.setTitle(R.string.pending_strike_title)
//        alert.setSubtitle(R.string.pending_strike_subtitle)
        // alert.setMessage(R.string.pending_strike_description)
        alert.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.close)) { dialog, _ ->
            dialog.dismiss()
        }
        alert.show()
    }

    private fun showStrikeDescriptionAlert() {
        val alert = HabiticaAlertDialog(context)
        alert.setTitle(R.string.strike_description_title)
//        alert.setSubtitle(R.string.strike_description_subtitle)
        // alert.setMessage(R.string.strike_description_description)
        alert.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.close)) { dialog, _ ->
            dialog.dismiss()
        }
        alert.show()
    }

    private fun setCollectionViews(collection: List<QuestProgressCollect>, quest: QuestContent) {
        val inflater = LayoutInflater.from(context)
        for (collect in collection) {
            val contentCollect = quest.getCollectWithKey(collect.key) ?: continue
            val collectBinding = QuestCollectBinding.inflate(inflater, binding.collectionContainer, true)
            collectBinding.iconView.loadImage("quest_" + quest.key + "_" + collect.key)
            collectBinding.nameView.text = contentCollect.text
            collectBinding.valueView.set(collect.count.toDouble(), contentCollect.count.toDouble())
        }
    }

    private fun getLocationName(key: String): String {
        return when (key) {
            "market" -> context.getString(R.string.market)
            "tavern" -> context.getString(R.string.sidebar_tavern)
            "questShop" -> context.getString(R.string.questShop)
            "seasonalShop" -> context.getString(R.string.seasonalShop)
            "stable" -> context.getString(R.string.sidebar_stable)
            else -> ""
        }
    }

    private fun getLongNPCName(key: String): String {
        return when (key) {
            "market" -> context.getString(R.string.market_owner_long)
            "tavern" -> context.getString(R.string.tavern_owner_long)
            "questShop" -> context.getString(R.string.questShop_owner_long)
            "seasonalShop" -> context.getString(R.string.seasonalShop_owner_long)
            "stable" -> context.getString(R.string.stable_owner_long)
            else -> ""
        }
    }

    private fun showQuestImage() {
        binding.questImageWrapper.visibility = View.VISIBLE
        binding.questImageView.loadImage("quest_" + quest?.key)
        preferences?.edit { putBoolean("boss_art_collapsed", false) }
        updateCaretImage()
    }

    private fun hideQuestImage() {
        binding.questImageWrapper.visibility = View.GONE
        preferences?.edit { putBoolean("boss_art_collapsed", true) }

        updateCaretImage()
    }

    private fun updateCaretImage() {
        if (binding.questImageWrapper.visibility == View.VISIBLE) {
            binding.caretView.setImageBitmap(HabiticaIconsHelper.imageOfCaret(quest?.colors?.extraLightColor ?: 0, true))
        } else {
            binding.caretView.setImageBitmap(HabiticaIconsHelper.imageOfCaret(quest?.colors?.extraLightColor ?: 0, false))
        }
    }
}
