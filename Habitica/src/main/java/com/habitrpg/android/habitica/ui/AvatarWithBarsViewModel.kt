package com.habitrpg.android.habitica.ui

import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.events.BoughtGemsEvent
import com.habitrpg.android.habitica.events.commands.OpenGemPurchaseFragmentCommand
import com.habitrpg.android.habitica.events.commands.OpenMenuItemCommand
import com.habitrpg.android.habitica.extensions.bindView
import com.habitrpg.android.habitica.models.Avatar
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.fragments.NavigationDrawerFragment
import com.habitrpg.android.habitica.ui.views.CurrencyViews
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.ValueBar
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*

class AvatarWithBarsViewModel(private val context: Context, view: View) {
    private val hpBar: ValueBar by bindView(view, R.id.hpBar)
    private val xpBar: ValueBar by bindView(view, R.id.xpBar)
    private val mpBar: ValueBar by bindView(view, R.id.mpBar)
    private val avatarView: AvatarView by bindView(view, R.id.avatarView)
    private val lvlText: TextView by bindView(view, R.id.lvl_tv)
    private val currencyView: CurrencyViews by bindView(view, R.id.currencyView)

    private var userObject: Avatar? = null

    private var cachedMaxHealth: Int = 0
    private var cachedMaxExp: Int = 0
    private var cachedMaxMana: Int = 0

    init {
        hpBar.setIcon(HabiticaIconsHelper.imageOfHeartLightBg())
        xpBar.setIcon(HabiticaIconsHelper.imageOfExperience())
        mpBar.setIcon(HabiticaIconsHelper.imageOfMagic())

        setHpBarData(0f, 50)
        setXpBarData(0f, 1)
        setMpBarData(0f, 1)
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun updateData(user: Avatar) {
        userObject = user

        val stats = user.stats

        var userClass = ""

        avatarView.setAvatar(user)

        if (stats.getHabitClass() != null) {
            userClass = stats.getTranslatedClassName(context)
        }

        mpBar.visibility = if (stats.getHabitClass() == null || stats.getLvl() < 10 || user.preferences.disableClasses) View.GONE else View.VISIBLE

        if (!user.hasClass()) {
            lvlText.text = context.getString(R.string.user_level, user.stats.getLvl())
            lvlText.setCompoundDrawables(null, null, null, null)
        } else {
            lvlText.text = context.getString(R.string.user_level_with_class, user.stats.getLvl(), userClass.substring(0, 1).toUpperCase(Locale.getDefault()) + userClass.substring(1))
            var drawable: Drawable? = null
            when (stats.getHabitClass()) {
                Stats.WARRIOR -> drawable = BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfWarriorDarkBg())
                Stats.ROGUE -> drawable = BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfRogueDarkBg())
                Stats.MAGE -> drawable = BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfMageDarkBg())
                Stats.HEALER -> drawable = BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfHealerDarkBg())
            }
            if (drawable != null) {
                drawable.setBounds(0, 0, drawable.minimumWidth,
                        drawable.minimumHeight)
            }
            lvlText.setCompoundDrawables(drawable, null, null, null)
        }

        setHpBarData(stats.hp.toFloat(), stats.maxHealth)
        setXpBarData(stats.exp.toFloat(), stats.toNextLevel)
        setMpBarData(stats.mp.toFloat(), stats.maxMP)

        currencyView.gold = stats.getGp()
        if (user is User) {
            currencyView.hourglasses = user.getHourglassCount().toDouble()
            currencyView.gems = user.getGemCount().toDouble()
        }

        currencyView.setOnClickListener {
            EventBus.getDefault().post(OpenGemPurchaseFragmentCommand())
        }
        avatarView.setOnClickListener {
            val event = OpenMenuItemCommand()
            event.identifier = NavigationDrawerFragment.SIDEBAR_AVATAR
            EventBus.getDefault().post(event)
        }
    }

    private fun setHpBarData(value: Float, valueMax: Int) {
        if (valueMax == 0) {
            cachedMaxHealth = valueMax
        }
        hpBar.set(Math.ceil(value.toDouble()), cachedMaxHealth.toDouble())
    }

    private fun setXpBarData(value: Float, valueMax: Int) {
        if (valueMax != 0)  {
            cachedMaxExp = valueMax
        }
        xpBar.set(Math.floor(value.toDouble()), cachedMaxExp.toDouble())
    }

    private fun setMpBarData(value: Float, valueMax: Int) {
        if (valueMax != 0) {
            cachedMaxMana = valueMax
        }
        mpBar.set(Math.floor(value.toDouble()), cachedMaxMana.toDouble())
    }

    @Subscribe
    fun onEvent(gemsEvent: BoughtGemsEvent) {
        var gems = userObject?.gemCount ?: 0
        gems += gemsEvent.NewGemsToAdd
        currencyView.gems = gems.toDouble()
    }

    fun valueBarLabelsToBlack() {
        hpBar.setLightBackground(true)
        xpBar.setLightBackground(true)
        mpBar.setLightBackground(true)
    }

    companion object {

        fun setHpBarData(valueBar: ValueBar, stats: Stats) {
            var maxHP = stats.maxHealth
            if (maxHP == null || maxHP == 0) {
                maxHP = 50
            }

            valueBar.set(Math.ceil(stats.hp.toDouble()), maxHP.toDouble())
        }
    }
}
