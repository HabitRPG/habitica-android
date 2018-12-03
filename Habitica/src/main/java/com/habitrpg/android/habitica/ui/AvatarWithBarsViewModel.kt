package com.habitrpg.android.habitica.ui

import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.events.BoughtGemsEvent
import com.habitrpg.android.habitica.helpers.HealthFormatter
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.Avatar
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.CurrencyViews
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.ValueBar
import io.reactivex.disposables.Disposable
import org.greenrobot.eventbus.Subscribe
import java.util.*

class AvatarWithBarsViewModel(private val context: Context, view: View, userRepository: UserRepository? = null) {
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

    private var disposable: Disposable? = null

    init {
        hpBar.setIcon(HabiticaIconsHelper.imageOfHeartLightBg())
        xpBar.setIcon(HabiticaIconsHelper.imageOfExperience())
        mpBar.setIcon(HabiticaIconsHelper.imageOfMagic())

        setHpBarData(0f, 50)
        setXpBarData(0f, 1)
        setMpBarData(0f, 1)

        if (userRepository != null) {
            disposable = userRepository.getUser().subscribe { updateData(it) }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun updateData(user: Avatar) {
        userObject = user

        val stats = user.stats ?: return

        var userClass = ""

        avatarView.setAvatar(user)

        if (stats.habitClass != null) {
            userClass = stats.getTranslatedClassName(context)
        }

        mpBar.visibility = if (stats.habitClass == null || stats.lvl ?: 0 < 10 || user.preferences?.disableClasses == true) View.GONE else View.VISIBLE

        if (!user.hasClass()) {
            lvlText.text = context.getString(R.string.user_level, stats.lvl)
            lvlText.setCompoundDrawables(null, null, null, null)
        } else {
            lvlText.text = context.getString(R.string.user_level_with_class, stats.lvl, userClass.substring(0, 1).toUpperCase(Locale.getDefault()) + userClass.substring(1))
            var drawable: Drawable? = null
            when (stats.habitClass) {
                Stats.WARRIOR -> drawable = BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfWarriorDarkBg())
                Stats.ROGUE -> drawable = BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfRogueDarkBg())
                Stats.MAGE -> drawable = BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfMageDarkBg())
                Stats.HEALER -> drawable = BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfHealerDarkBg())
            }
            drawable?.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
            lvlText.setCompoundDrawables(drawable, null, null, null)
        }

        setHpBarData(stats.hp?.toFloat() ?: 0.toFloat(), stats.maxHealth ?: 0)
        setXpBarData(stats.exp?.toFloat() ?: 0.toFloat(), stats.toNextLevel ?: 0)
        setMpBarData(stats.mp?.toFloat() ?: 0.toFloat(), stats.maxMP ?: 0)

        currencyView.gold = stats.gp ?: 0.0
        if (user is User) {
            currencyView.hourglasses = user.hourglassCount?.toDouble() ?: 0.0
            currencyView.gems = user.gemCount.toDouble()
        }

        currencyView.setOnClickListener {
            MainNavigationController.navigate(R.id.gemPurchaseActivity)
        }
        avatarView.setOnClickListener {
            MainNavigationController.navigate(R.id.avatarOverviewFragment)
        }
    }

    private fun setHpBarData(value: Float, valueMax: Int) {
        if (valueMax != 0) {
            cachedMaxHealth = valueMax
        }
        hpBar.set(HealthFormatter.format(value.toDouble()), cachedMaxHealth.toDouble())
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

            val hp = stats.hp?.let { HealthFormatter.format(it) } ?: 0.0
            valueBar.set(hp, maxHP.toDouble())
        }
    }
}
