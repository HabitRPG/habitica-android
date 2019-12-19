package com.habitrpg.android.habitica.ui.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.ActivityFixcharacterBinding
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.settings.FixValuesEditText
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import javax.inject.Inject
import javax.inject.Named

class FixCharacterValuesActivity: BaseActivity() {

    private lateinit var binding: ActivityFixcharacterBinding
    @Inject
    lateinit var repository: UserRepository

    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String

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

        repository.getUser(userId).firstElement().subscribe(Consumer {
            user = it
        }, RxErrorHandler.handleEmptyError())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_save, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId

        if (id == R.id.action_save_changes) {
            @Suppress("DEPRECATION")
            val progressDialog = ProgressDialog.show(this, getString(R.string.saving), "")
            val userInfo = HashMap<String, Any>()
            userInfo["stats.hp"] = binding.healthEditText.getDoubleValue()
            userInfo["stats.exp"] = binding.experienceEditText.getDoubleValue()
            userInfo["stats.gp"] = binding.goldEditText.getDoubleValue()
            userInfo["stats.mp"] = binding.manaEditText.getDoubleValue()
            userInfo["stats.lvl"] = binding.levelEditText.getDoubleValue().toInt()
            userInfo["achievements.streak"] = binding.streakEditText.getDoubleValue().toInt()
            repository.updateUser(user, userInfo).subscribe(Consumer {}, RxErrorHandler.handleEmptyError(), Action {
                progressDialog.dismiss()
                finish()
            })
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
        binding.healthEditText.text = user.stats?.hp.toString()
        binding.experienceEditText.text = user.stats?.exp.toString()
        binding.goldEditText.text = user.stats?.gp.toString()
        binding.manaEditText.text = user.stats?.mp.toString()
        binding.levelEditText.text = user.stats?.lvl.toString()
        binding.streakEditText.text = user.streakCount.toString()

        when (user.stats?.habitClass) {
            Stats.WARRIOR -> {
                binding.levelEditText.iconBackgroundColor = ContextCompat.getColor(this, R.color.red_500)
                binding.levelEditText.setIconBitmap(HabiticaIconsHelper.imageOfWarriorLightBg())
            }
            Stats.MAGE -> {
                binding.levelEditText.iconBackgroundColor = ContextCompat.getColor(this, R.color.blue_500)
                binding.levelEditText.setIconBitmap(HabiticaIconsHelper.imageOfMageLightBg())
            }
            Stats.HEALER -> {
                binding.levelEditText.iconBackgroundColor = ContextCompat.getColor(this, R.color.yellow_500)
                binding.levelEditText.setIconBitmap(HabiticaIconsHelper.imageOfHealerLightBg())
            }
            Stats.ROGUE -> {
                binding.levelEditText.iconBackgroundColor = ContextCompat.getColor(this, R.color.brand_500)
                binding.levelEditText.setIconBitmap(HabiticaIconsHelper.imageOfRogueLightBg())
            }
        }
    }

    fun FixValuesEditText.getDoubleValue(): Double {
        val stringValue = this.text
        return try {
            stringValue.toDouble()
        } catch (_: NumberFormatException) {
            0.0
        }
    }

}