package com.habitrpg.android.habitica.ui.activities

import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.habitrpg.android.habitica.HabiticaApplication
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.events.ShowConnectionProblemEvent
import com.habitrpg.android.habitica.extensions.getThemeColor
import com.habitrpg.android.habitica.helpers.LanguageHelper
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import io.reactivex.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*


abstract class BaseActivity : AppCompatActivity() {

    private var currentTheme: String? = null
    internal var forcedTheme: String? = null
    private var destroyed: Boolean = false

    protected abstract fun getLayoutResId(): Int

    protected var compositeSubscription = CompositeDisposable()

    private val habiticaApplication: HabiticaApplication
        get() = application as HabiticaApplication

    var isActivityVisible = false

    override fun isDestroyed(): Boolean {
        return destroyed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val languageHelper = LanguageHelper(sharedPreferences.getString("language", "en"))
        Locale.setDefault(languageHelper.locale)
        val configuration = Configuration()
        configuration.setLocale(languageHelper.locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
        loadTheme(sharedPreferences)

        delegate.setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        habiticaApplication
        injectActivity(HabiticaBaseApplication.userComponent)
        setContentView(getLayoutResId())
        compositeSubscription = CompositeDisposable()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onResume() {
        super.onResume()
        isActivityVisible = true
        loadTheme(PreferenceManager.getDefaultSharedPreferences(this))
    }

    override fun onPause() {
        isActivityVisible = false
        super.onPause()
    }

    override fun onStop() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        super.onStop()
    }

    private fun loadTheme(sharedPreferences: SharedPreferences) {
        val theme = if (forcedTheme != null) {
            forcedTheme
        } else {
            sharedPreferences.getString("theme_name", "purple")
        }
        if (theme == currentTheme) return
        setTheme(when (theme) {
            "maroon" -> R.style.MainAppTheme_Maroon
            "red" -> R.style.MainAppTheme_Red
            "orange" -> R.style.MainAppTheme_Orange
            "yellow" -> R.style.MainAppTheme_Yellow
            "green" -> R.style.MainAppTheme_Green
            "teal" -> R.style.MainAppTheme_Teal
            "blue" -> R.style.MainAppTheme_Blue
            else -> R.style.MainAppTheme
        })
        window.navigationBarColor = getThemeColor(R.attr.colorPrimaryDark)
        window.statusBarColor = getThemeColor(R.attr.colorPrimaryDark)

        if (currentTheme != null) {
            reload()
        } else {
            currentTheme = theme
        }
    }

    protected abstract fun injectActivity(component: UserComponent?)

    protected fun setupToolbar(toolbar: Toolbar?) {
        if (toolbar != null) {
            setSupportActionBar(toolbar)

            val actionBar = supportActionBar
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true)
                actionBar.setDisplayShowHomeEnabled(true)
                actionBar.setDisplayShowTitleEnabled(true)
                actionBar.setDisplayUseLogoEnabled(false)
                actionBar.setHomeButtonEnabled(true)
            }
        }
    }

    override fun onDestroy() {
        destroyed = true

        if (!compositeSubscription.isDisposed) {
            compositeSubscription.dispose()
        }
        super.onDestroy()
    }

    @Subscribe
    open fun onEvent(event: ShowConnectionProblemEvent) {
        val alert = HabiticaAlertDialog(this)
        alert.setTitle(event.title)
        alert.setMessage(event.message)
        alert.addButton(android.R.string.ok, isPrimary = true, isDestructive = false, function = null)
        alert.show()
    }

    fun reload() {
        finish()
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
        startActivity(intent)
    }
}
