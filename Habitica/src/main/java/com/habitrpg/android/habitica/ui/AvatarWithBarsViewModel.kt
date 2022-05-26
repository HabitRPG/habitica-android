package com.habitrpg.android.habitica.ui

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.AvatarWithBarsBinding
import com.habitrpg.android.habitica.helpers.Animations
import com.habitrpg.android.habitica.helpers.HealthFormatter
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.Avatar
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.mainActivityCreatedAt
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import java.util.Date
import java.util.Locale
import kotlin.math.floor

class AvatarWithBarsViewModel(
    private val context: Context,
    private val binding: AvatarWithBarsBinding,
    viewModel: MainUserViewModel? = null
): ViewModel() {
    private var userObject: Avatar? = null

    private var cachedMaxHealth: Int = 0
    private var cachedMaxExp: Int = 0
    private var cachedMaxMana: Int = 0

    init {
        binding.hpBar.setIcon(HabiticaIconsHelper.imageOfHeartLightBg())
        binding.xpBar.setIcon(HabiticaIconsHelper.imageOfExperience())
        binding.mpBar.setIcon(HabiticaIconsHelper.imageOfMagic())

        setHpBarData(0f, 50)
        setXpBarData(0f, 1)
        setMpBarData(0f, 1)

        viewModel?.user?.observeForever {
            if (it != null) {
                updateData(it)
            }
        }
    }

    fun updateData(user: Avatar) {
        userObject = user

        val stats = user.stats ?: return

        var userClass = ""

        binding.avatarView.setAvatar(user)

        if (stats.habitClass != null) {
            userClass = stats.getTranslatedClassName(context)
        }

        binding.mpBar.visibility = if (stats.habitClass == null || stats.lvl ?: 0 < 10 || user.preferences?.disableClasses == true) View.GONE else View.VISIBLE

        if (!user.hasClass) {
            setUserLevel(context, binding.lvlTv, stats.lvl)
        } else {
            setUserLevelWithClass(
                context, binding.lvlTv, stats.lvl,
                userClass.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }, stats.habitClass
            )
        }

        setHpBarData(stats.hp?.toFloat() ?: 0.toFloat(), stats.maxHealth ?: 0)
        setXpBarData(stats.exp?.toFloat() ?: 0.toFloat(), stats.toNextLevel ?: 0)
        setMpBarData(stats.mp?.toFloat() ?: 0.toFloat(), stats.maxMP ?: 0)

        if (!stats.isBuffed) {
            binding.buffImageView.visibility = View.GONE
        }

        if (user is User) {
            binding.currencyView.gold = stats.gp ?: 0.0
            binding.currencyView.hourglasses = user.hourglassCount.toDouble()
            binding.currencyView.gems = user.gemCount.toDouble()
        }

        binding.currencyView.setOnClickListener {
            MainNavigationController.navigate(R.id.gemPurchaseActivity, bundleOf(Pair("openSubscription", false)))
        }
        binding.avatarView.setOnClickListener {
            MainNavigationController.navigate(R.id.avatarOverviewFragment)
        }

        mainActivityCreatedAt?.let {
            Log.i("LAUNCH TIME", "${Date().time - it.time}")
            mainActivityCreatedAt = null
        }
    }

    private fun setHpBarData(value: Float, valueMax: Int) {
        if (valueMax != 0) {
            cachedMaxHealth = valueMax
        }
        if (binding.hpBar.currentValue > value) {
            binding.hpBar.progressBar.startAnimation(Animations.negativeShakeAnimation())
        }
        binding.hpBar.set(HealthFormatter.format(value.toDouble()), cachedMaxHealth.toDouble())
    }

    private fun setXpBarData(value: Float, valueMax: Int) {
        if (valueMax != 0) {
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

    companion object {
        private fun setUserLevel(context: Context, textView: TextView, level: Int?) {
            textView.text = context.getString(R.string.user_level, level)
            textView.contentDescription = context.getString(R.string.level_unabbreviated, level)
            textView.setCompoundDrawables(null, null, null, null)
        }

        private fun setUserLevelWithClass(
            context: Context,
            textView: TextView,
            level: Int?,
            userClassString: String,
            habitClass: String?
        ) {
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
