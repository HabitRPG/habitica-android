package com.habitrpg.android.habitica.ui.activities

import android.app.ProgressDialog
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.settings.FixValuesEditText
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_fixcharacter.*
import javax.inject.Inject
import javax.inject.Named

class FixCharacterValuesActivity: BaseActivity() {

    @Inject
    lateinit var repository: UserRepository

    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String

    override fun getLayoutResId(): Int = R.layout.activity_fixcharacter

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.fix_character_values)
        setupToolbar(toolbar)

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
            userInfo["stats.hp"] = healthEditText.getDoubleValue()
            userInfo["stats.exp"] = experienceEditText.getDoubleValue()
            userInfo["stats.gp"] = goldEditText.getDoubleValue()
            userInfo["stats.mp"] = manaEditText.getDoubleValue()
            userInfo["stats.lvl"] = levelEditText.getDoubleValue().toInt()
            userInfo["achievements.streak"] = streakEditText.getDoubleValue().toInt()
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
        healthEditText.text = user.stats?.hp.toString()
        experienceEditText.text = user.stats?.exp.toString()
        goldEditText.text = user.stats?.gp.toString()
        manaEditText.text = user.stats?.mp.toString()
        levelEditText.text = user.stats?.lvl.toString()
        streakEditText.text = user.streakCount.toString()

        when (user.stats?.habitClass) {
            Stats.WARRIOR -> {
                levelEditText.iconBackgroundColor = ContextCompat.getColor(this, R.color.red_500)
                levelEditText.setIconBitmap(HabiticaIconsHelper.imageOfWarriorLightBg())
            }
            Stats.MAGE -> {
                levelEditText.iconBackgroundColor = ContextCompat.getColor(this, R.color.blue_500)
                levelEditText.setIconBitmap(HabiticaIconsHelper.imageOfMageLightBg())
            }
            Stats.HEALER -> {
                levelEditText.iconBackgroundColor = ContextCompat.getColor(this, R.color.yellow_500)
                levelEditText.setIconBitmap(HabiticaIconsHelper.imageOfHealerLightBg())
            }
            Stats.ROGUE -> {
                levelEditText.iconBackgroundColor = ContextCompat.getColor(this, R.color.brand_500)
                levelEditText.setIconBitmap(HabiticaIconsHelper.imageOfRogueLightBg())
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