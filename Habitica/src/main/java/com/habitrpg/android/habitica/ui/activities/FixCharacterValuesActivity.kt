package com.habitrpg.android.habitica.ui.activities

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.databinding.ActivityFixcharacterBinding
import com.habitrpg.android.habitica.extensions.setTintWith
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel

import javax.inject.Inject

class FixCharacterValuesActivity : BaseActivity() {

    private lateinit var binding: ActivityFixcharacterBinding

    @Inject
    lateinit var userViewModel: MainUserViewModel

    override fun getLayoutResId(): Int = R.layout.activity_fixcharacter

    override fun getContentView(): View {
        binding = ActivityFixcharacterBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.fix_character_values)
        setupToolbar(binding.toolbar)

        userViewModel.user.observe(this) { user = it }

        setIconBackground(
            binding.healthIconBackgroundView,
            ContextCompat.getColor(this, R.color.red_500)
        )
        setIconBackground(
            binding.experienceIconBackgroundView,
            ContextCompat.getColor(this, R.color.yellow_500)
        )
        setIconBackground(
            binding.manaIconBackgroundView,
            ContextCompat.getColor(this, R.color.blue_500)
        )
        setIconBackground(
            binding.goldIconBackgroundView,
            ContextCompat.getColor(this, R.color.yellow_500)
        )
        setIconBackground(
            binding.streakIconBackgroundView,
            ContextCompat.getColor(this, R.color.separator)
        )

        binding.healthIconView.setImageBitmap(HabiticaIconsHelper.imageOfHeartLightBg())
        binding.experienceIconView.setImageBitmap(HabiticaIconsHelper.imageOfExperience())
        binding.manaIconView.setImageBitmap(HabiticaIconsHelper.imageOfMagic())
        binding.goldIconView.setImageBitmap(HabiticaIconsHelper.imageOfGold())
        binding.streakIconView.setImageResource(R.drawable.achievement_thermometer)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_save, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_save_changes) {
            val userInfo = HashMap<String, Any>()
            userInfo["stats.hp"] = binding.healthEditText.getDoubleValue()
            userInfo["stats.exp"] = binding.experienceEditText.getDoubleValue()
            userInfo["stats.gp"] = binding.goldEditText.getDoubleValue()
            userInfo["stats.mp"] = binding.manaEditText.getDoubleValue()
            userInfo["stats.lvl"] = binding.levelEditText.getDoubleValue().toInt()
            userInfo["achievements.streak"] = binding.streakEditText.getDoubleValue().toInt()
            userViewModel.updateUser(userInfo)
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private var user: User? = null
        set(value) {
            field = value
            if (value != null) {
                updateFields(value)
            }
        }

    private fun updateFields(user: User) {
        val stats = user.stats ?: return
        binding.healthEditText.setText(stats.hp.toString())
        binding.experienceEditText.setText(stats.exp.toString())
        binding.goldEditText.setText(stats.gp.toString())
        binding.manaEditText.setText(stats.mp.toString())
        binding.levelEditText.setText(stats.lvl.toString())
        binding.streakEditText.setText(user.streakCount.toString())

        when (stats.habitClass) {
            Stats.WARRIOR -> {
                setIconBackground(
                    binding.levelIconBackgroundView,
                    ContextCompat.getColor(this, R.color.red_500)
                )
                binding.levelIconView.setImageBitmap(HabiticaIconsHelper.imageOfWarriorLightBg())
            }
            Stats.MAGE -> {
                setIconBackground(
                    binding.levelIconBackgroundView,
                    ContextCompat.getColor(this, R.color.blue_500)
                )
                binding.levelIconView.setImageBitmap(HabiticaIconsHelper.imageOfMageLightBg())
            }
            Stats.HEALER -> {
                setIconBackground(
                    binding.levelIconBackgroundView,
                    ContextCompat.getColor(this, R.color.yellow_500)
                )
                binding.levelIconView.setImageBitmap(HabiticaIconsHelper.imageOfHealerLightBg())
            }
            Stats.ROGUE -> {
                setIconBackground(
                    binding.levelIconBackgroundView,
                    ContextCompat.getColor(this, R.color.brand_500)
                )
                binding.levelIconView.setImageBitmap(HabiticaIconsHelper.imageOfRogueLightBg())
            }
        }
    }

    private fun setIconBackground(view: View, color: Int) {
        val backgroundDrawable = ContextCompat.getDrawable(this, R.drawable.layout_rounded_bg)
        backgroundDrawable?.setTintWith(color, PorterDuff.Mode.MULTIPLY)
        backgroundDrawable?.alpha = 50
        view.background = backgroundDrawable
    }

    private fun EditText.getDoubleValue(): Double {
        val stringValue = this.text.toString()
        return try {
            stringValue.toDouble()
        } catch (_: NumberFormatException) {
            0.0
        }
    }
}
