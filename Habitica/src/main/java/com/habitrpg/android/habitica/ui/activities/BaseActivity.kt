package com.habitrpg.android.habitica.ui.activities

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.habitrpg.android.habitica.HabiticaApplication
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.events.ShowConnectionProblemEvent
import com.habitrpg.android.habitica.extensions.getThemeColor
import com.habitrpg.android.habitica.extensions.isUsingNightModeResources
import com.habitrpg.android.habitica.helpers.LanguageHelper
import com.habitrpg.android.habitica.ui.helpers.ToolbarColorHelper
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import io.reactivex.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*


abstract class BaseActivity : AppCompatActivity() {

    private var currentTheme: String? = null
    private var isNightMode: Boolean = false
    internal var forcedTheme: String? = null
    private var destroyed: Boolean = false

    open var overrideModernHeader: Boolean? = null

    protected abstract fun getLayoutResId(): Int

    open fun getContentView(): View {
        return (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(getLayoutResId(), null)
    }

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
        delegate.localNightMode = when (sharedPreferences.getString("theme_mode", "system")) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        isNightMode = isUsingNightModeResources()
        loadTheme(sharedPreferences)

        super.onCreate(savedInstanceState)
        habiticaApplication
        injectActivity(HabiticaBaseApplication.userComponent)
        setContentView(getContentView())
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

    internal open fun loadTheme(sharedPreferences: SharedPreferences, forced: Boolean = false) {
        val theme = forcedTheme ?: sharedPreferences.getString("theme_name", "purple")
        val modernHeaderStyle = overrideModernHeader ?: sharedPreferences.getBoolean("modern_header_style", true)
        if (theme != currentTheme || forced) {
            if (isNightMode) {
                setTheme(when (theme) {
                    "maroon" -> R.style.MainAppTheme_Maroon_Dark
                    "red" -> R.style.MainAppTheme_Red_Dark
                    "orange" -> R.style.MainAppTheme_Orange_Dark
                    "yellow" -> R.style.MainAppTheme_Yellow_Dark
                    "green" -> R.style.MainAppTheme_Green_Dark
                    "teal" -> R.style.MainAppTheme_Teal_Dark
                    "blue" -> R.style.MainAppTheme_Blue_Dark
                    else -> R.style.MainAppTheme_Dark
                })
            } else {
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
            }
        }

        if (isNightMode) {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.system_bars)
        } else {
            window.navigationBarColor = getThemeColor(R.attr.colorPrimaryDark)
        }
        if (!isNightMode && modernHeaderStyle && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getThemeColor(R.attr.headerBackgroundColor)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            window.statusBarColor = getThemeColor(R.attr.statusBarBackground)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }

        if (currentTheme != null && theme != currentTheme) {
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
        toolbar?.let { ToolbarColorHelper.colorizeToolbar(it, this, overrideModernHeader) }
    }

    override fun onDestroy() {
        destroyed = true

        if (!compositeSubscription.isDisposed) {
            compositeSubscription.dispose()
        }
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val newNightMode = isUsingNightModeResources()
        if (newNightMode != isNightMode) {
            isNightMode = newNightMode
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            loadTheme(sharedPreferences, true)
        }
    }

    @Subscribe
    open fun onEvent(event: ShowConnectionProblemEvent) {
        val alert = HabiticaAlertDialog(this)
        alert.setTitle(event.title)
        alert.setMessage(event.message)
        alert.addButton(android.R.string.ok, isPrimary = true, isDestructive = false, function = null)
        alert.enqueue()
    }

    fun reload() {
        finish()
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
        startActivity(intent)
    }
}
