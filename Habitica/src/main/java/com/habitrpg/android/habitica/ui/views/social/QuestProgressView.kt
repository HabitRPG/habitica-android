package com.habitrpg.android.habitica.ui.views.social

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.bindView
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.inventory.QuestProgressCollect
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import com.habitrpg.android.habitica.ui.views.*
import io.realm.RealmList
import android.graphics.drawable.GradientDrawable
import com.habitrpg.android.habitica.extensions.backgroundCompat
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1


class QuestProgressView : LinearLayout {

    private val questImageView: SimpleDraweeView by bindView(R.id.questImageView)
    private val questImageTitle: View by bindView(R.id.questImageTitle)
    private val questImageSeparator: View by bindView(R.id.questImageSeparator)
    private val questImageCaretView: ImageView by bindView(R.id.caretView)
    private val bossNameView: TextView by bindView(R.id.bossNameView)
    private val bossHealthView: ValueBar by bindView(R.id.bossHealthView)
    private val rageMeterView: TextView by bindView(R.id.rageMeterView)
    private val bossRageView: ValueBar by bindView(R.id.bossRageView)
    private val rageStrikeDescriptionView: TextView by bindView(R.id.rageStrikeDescriptionView)
    private val rageStrikeContainer: LinearLayout by bindView(R.id.rageStrikeContainer)
    private val collectionContainer: ViewGroup by bindView(R.id.collectionContainer)
    private val questDescriptionSection: CollapsibleSectionView by bindView(R.id.questDescriptionSection)
    private val questDescriptionView: TextView by bindView(R.id.questDescription)

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
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.quest_progress, this)

        questImageCaretView.setImageBitmap(HabiticaIconsHelper.imageOfCaret(ContextCompat.getColor(context, R.color.white), true))
        questImageTitle.setOnClickListener {
            if (questImageView.visibility == View.VISIBLE) {
                hideQuestImage()
            } else {
                showQuestImage()
            }
        }

        rageStrikeDescriptionView.setOnClickListener { showStrikeDescriptionAlert() }

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
                rect.set(0.0f, 0.0f, (canvas?.width?.toFloat()
                        ?: 1.0f) / displayDensity, (canvas?.height?.toFloat()
                        ?: 1.0f) / displayDensity)
                canvas?.scale(displayDensity, displayDensity)
                HabiticaIcons.drawQuestBackground(canvas, rect, colors.darkColor, colors.mediumColor, colors.extraLightColor)
                canvas?.scale(1.0f / displayDensity, 1.0f / displayDensity)
            }
        }
        super.onDraw(canvas)
    }

    fun setData(quest: QuestContent, progress: Quest?) {
        this.quest = quest
        this.progress = progress
    }

    private fun configure() {
        val quest = this.quest
        val progress = this.progress
        if (quest == null || progress == null || !quest.isValid || !progress.isValid) {
            return
        }
        collectionContainer.removeAllViews()
        if (quest.isBossQuest) {
            bossNameView.text = quest.boss.name
            bossNameView.visibility = View.VISIBLE
            bossHealthView.visibility = View.VISIBLE
            bossHealthView.set(progress.progress?.hp ?: 0.0, quest.boss?.hp?.toDouble() ?: 0.0)

            if (quest.boss.hasRage()) {
                rageMeterView.visibility = View.VISIBLE
                bossRageView.visibility = View.VISIBLE
                rageMeterView.text = quest.boss.rage?.title
                bossRageView.set(progress.progress?.rage ?: 0.0, quest.boss?.rage?.value ?: 0.0)
                if (progress.hasRageStrikes()) {
                    setupRageStrikeViews()
                } else {
                    rageStrikeDescriptionView.visibility = View.GONE
                }
            } else {
                rageMeterView.visibility = View.GONE
                bossRageView.visibility = View.GONE
                rageStrikeDescriptionView.visibility = View.GONE
            }
        } else {
            bossNameView.visibility = View.GONE
            bossHealthView.visibility = View.GONE
            rageMeterView.visibility = View.GONE
            bossRageView.visibility = View.GONE
            rageStrikeDescriptionView.visibility = View.GONE

            val collection = progress.progress?.collect
            if (collection != null) {
                setCollectionViews(collection, quest)
            }
        }
        questDescriptionView.text = MarkdownParser.parseMarkdown(quest.notes)
        DataBindingUtils.loadImage(questImageView, "quest_"+quest.key)
        val lightColor =  quest.colors?.lightColor
        if (lightColor != null) {
            questDescriptionSection.separatorColor = lightColor
            questImageSeparator.setBackgroundColor(lightColor)

            val gradientDrawable = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(ContextCompat.getColor(context, R.color.transparent), lightColor))
            gradientDrawable.cornerRadius = 0f
            questImageView.backgroundCompat = gradientDrawable
        }
    }

    private fun setupRageStrikeViews() {
        rageStrikeDescriptionView.visibility = View.VISIBLE
        rageStrikeDescriptionView.text = context.getString(R.string.rage_strike_count, progress?.activeRageStrikeNumber, progress?.rageStrikes?.size ?: 0)

        rageStrikeContainer.removeAllViews()
        progress?.rageStrikes?.forEach { strike ->
            val iconView = ImageView(context)
            if (strike.wasHit) {
                DataBindingUtils.loadImage("rage_strike_${strike.key}", {
                    Observable.just(it)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(Action1 {
                                val displayDensity = resources.displayMetrics.density
                                val width = it.width * displayDensity
                                val height = it.height * displayDensity
                                val scaledImage = Bitmap.createScaledBitmap(it, width.toInt(), height.toInt(), false)
                                iconView.setImageBitmap(HabiticaIconsHelper.imageOfRageStrikeActive(context, scaledImage))
                                iconView.setOnClickListener {
                                    showActiveStrikeAlert(strike.key)
                                }
                            }, RxErrorHandler.handleEmptyError())
                })
            } else {
                iconView.setImageBitmap(HabiticaIconsHelper.imageOfRageStrikeInactive())
                iconView.setOnClickListener { showPendingStrikeAlert() }
            }
            val params = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val spacing = context.resources.getDimension(R.dimen.spacing_medium).toInt()
            params.setMargins(spacing, 0, spacing, 0)
            rageStrikeContainer.addView(iconView, params)
        }
    }

    private fun showActiveStrikeAlert(key: String) {
        val alert = HabiticaAlertDialog(context)
        alert.setTitle(context.getString(R.string.strike_active_title, getLocationName(key)))
        alert.setTitleBackground(R.color.orange_10)
        alert.setSubtitle(context.getString(R.string.strike_active_subtitle, getNpcName(key)))
        alert.setMessage(context.getString(R.string.strike_active_description, getLongNPCName(key), quest?.boss?.name ?: "", getLocationName(key)))
        alert.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.close), { dialog, _ ->
            dialog.dismiss()
        })
        alert.show()
    }

    private fun showPendingStrikeAlert() {
        val alert = HabiticaAlertDialog(context)
        alert.setTitle(R.string.pending_strike_title)
        alert.setTitleBackground(R.color.orange_10)
        alert.setSubtitle(R.string.pending_strike_subtitle)
        alert.setMessage(R.string.pending_strike_description)
        alert.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.close), { dialog, _ ->
            dialog.dismiss()
        })
        alert.show()
    }

    private fun showStrikeDescriptionAlert() {
        val alert = HabiticaAlertDialog(context)
        alert.setTitle(R.string.strike_description_title)
        alert.setTitleBackground(R.color.orange_10)
        alert.setSubtitle(R.string.strike_description_subtitle)
        alert.setMessage(R.string.strike_description_description)
        alert.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.close), { dialog, _ ->
            dialog.dismiss()
        })
        alert.show()
    }

    private fun setCollectionViews(collection: RealmList<QuestProgressCollect>, quest: QuestContent) {
        val inflater = LayoutInflater.from(context)
        for (collect in collection) {
            val contentCollect = quest.getCollectWithKey(collect.key) ?: continue
            val view = inflater.inflate(R.layout.quest_collect, collectionContainer, false)
            val iconView: SimpleDraweeView = view.findViewById(R.id.icon_view)
            val nameView: TextView = view.findViewById(R.id.name_view)
            val valueView: ValueBar = view.findViewById(R.id.value_view)
            DataBindingUtils.loadImage(iconView, "quest_" + quest.key + "_" + collect.key)
            nameView.text = contentCollect.text
            valueView.set(collect.count.toDouble(), contentCollect.count.toDouble())

            collectionContainer.addView(view)
        }
    }

    private fun getNpcName(key: String): String {
        return when (key) {
            "market" -> context.getString(R.string.market_owner)
            "tavern" -> context.getString(R.string.tavern_owner)
            "questShop" -> context.getString(R.string.questShop_owner)
            "seasonalShop" -> context.getString(R.string.seasonalShop_owner)
            "stable" -> context.getString(R.string.stable_owner)
            else -> ""
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
            "questShop" -> context.getString(R.string.questShop_owner)
            "seasonalShop" -> context.getString(R.string.seasonalShop_owner)
            "stable" -> context.getString(R.string.stable_owner_long)
            else -> ""
        }
    }


    private fun showQuestImage() {
        questImageCaretView.setImageBitmap(HabiticaIconsHelper.imageOfCaret(ContextCompat.getColor(context, R.color.white), true))
        questImageView.visibility = View.VISIBLE
        DataBindingUtils.loadImage(questImageView, "quest_"+quest?.key)
        val editPreferences = preferences?.edit()
        editPreferences?.putBoolean("boss_art_collapsed", false)
        editPreferences?.apply()
    }

    private fun hideQuestImage() {
        questImageCaretView.setImageBitmap(HabiticaIconsHelper.imageOfCaret(ContextCompat.getColor(context, R.color.white), false))
        questImageView.visibility = View.GONE
        val editPreferences = preferences?.edit()
        editPreferences?.putBoolean("boss_art_collapsed", true)
        editPreferences?.apply()
    }
}
