package com.habitrpg.android.habitica.ui

import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.AvatarWithBarsBinding
import com.habitrpg.android.habitica.events.BoughtGemsEvent
import com.habitrpg.android.habitica.helpers.HealthFormatter
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.Avatar
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import io.reactivex.rxjava3.disposables.Disposable
import org.greenrobot.eventbus.Subscribe
import java.util.*
import kotlin.math.floor

class AvatarWithBarsViewModel(private val context: Context, private val binding: AvatarWithBarsBinding, userRepository: UserRepository? = null) {
    private var userObject: Avatar? = null

    private var cachedMaxHealth: Int = 0
    private var cachedMaxExp: Int = 0
    private var cachedMaxMana: Int = 0

    private var disposable: Disposable? = null

    init {
        binding.hpBar.setIcon(HabiticaIconsHelper.imageOfHeartLightBg())
        binding.xpBar.setIcon(HabiticaIconsHelper.imageOfExperience())
        binding.mpBar.setIcon(HabiticaIconsHelper.imageOfMagic())

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

        binding.avatarView.setAvatar(user)

        if (stats.habitClass != null) {
            userClass = stats.getTranslatedClassName(context)
        }

        binding.mpBar.visibility = if (stats.habitClass == null || stats.lvl ?: 0 < 10 || user.preferences?.disableClasses == true) View.GONE else View.VISIBLE

        if (!user.hasClass()) {
            setUserLevel(context, binding.lvlTv, stats.lvl)
        } else {
            setUserLevelWithClass(context, binding.lvlTv, stats.lvl, userClass.capitalize(Locale.getDefault()), stats.habitClass)
        }

        setHpBarData(stats.hp?.toFloat() ?: 0.toFloat(), stats.maxHealth ?: 0)
        setXpBarData(stats.exp?.toFloat() ?: 0.toFloat(), stats.toNextLevel ?: 0)
        setMpBarData(stats.mp?.toFloat() ?: 0.toFloat(), stats.maxMP ?: 0)

        if (!stats.isBuffed) {
            binding.buffImageView.visibility = View.GONE
        }

        binding.currencyView.gold = stats.gp ?: 0.0
        if (user is User) {
            binding.currencyView.hourglasses = user.hourglassCount.toDouble()
            binding.currencyView.gems = user.gemCount.toDouble()
        }

        binding.currencyView.setOnClickListener {
            MainNavigationController.navigate(R.id.gemPurchaseActivity, bundleOf(Pair("openSubscription", false)))
        }
        binding.avatarView.setOnClickListener {
            MainNavigationController.navigate(R.id.avatarOverviewFragment)
        }
    }

    private fun setHpBarData(value: Float, valueMax: Int) {
        if (valueMax != 0) {
            cachedMaxHealth = valueMax
        }
        binding.hpBar.set(HealthFormatter.format(value.toDouble()), cachedMaxHealth.toDouble())
    }

    private fun setXpBarData(value: Float, valueMax: Int) {
        if (valueMax != 0)  {
            cachedMaxExp = valueMax
        }
        binding.xpBar.set(floor(value.toDouble()), cachedMaxExp.toDouble())
    }

    private fun setMpBarData(value: Float, valueMax: Int) {
        if (valueMax != 0) {
            cachedMaxMana = valueMax
        }
        binding.mpBar.set(floor(value.toDouble()), cachedMaxMana.toDouble())
    }

    @Subscribe
    fun onEvent(gemsEvent: BoughtGemsEvent) {
        var gems = userObject?.gemCount ?: 0
        gems += gemsEvent.NewGemsToAdd
        binding.currencyView.gems = gems.toDouble()
    }

    companion object {
        private fun setUserLevel(context: Context, textView: TextView, level: Int?) {
            textView.text = context.getString(R.string.user_level, level)
            textView.contentDescription = context.getString(R.string.level_unabbreviated, level)
            textView.setCompoundDrawables(null, null, null, null)
        }

        private fun setUserLevelWithClass(context: Context, textView: TextView, level: Int?, userClassString: String, habitClass: String?) {
            textView.text = context.getString(R.string.user_level_with_class, level, userClassString)
            textView.contentDescription = context.getString(R.string.user_level_with_class_unabbreviated, level, userClassString)
            var drawable: Drawable? = null
            when (habitClass) {
                Stats.WARRIOR -> drawable = BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfWarriorDarkBg())
                Stats.ROGUE -> drawable = BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfRogueDarkBg())
                Stats.MAGE -> drawable = BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfMageDarkBg())
                Stats.HEALER -> drawable = BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfHealerDarkBg())
            }
            drawable?.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
            textView.setCompoundDrawables(drawable, null, null, null)
        }
    }
}
