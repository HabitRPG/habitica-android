package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import com.habitrpg.android.habitica.HabiticaApplication
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.events.ShowConnectionProblemEvent
import com.habitrpg.android.habitica.extensions.getThemeColor
import com.habitrpg.android.habitica.helpers.LanguageHelper
import com.habitrpg.android.habitica.ui.helpers.ToolbarColorHelper
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import io.reactivex.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*


abstract class BaseActivity : AppCompatActivity() {

    private var currentTheme: String? = null
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
        loadTheme(sharedPreferences)

        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO
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

    private fun loadTheme(sharedPreferences: SharedPreferences) {
        val theme = if (forcedTheme != null) {
            forcedTheme
        } else {
            sharedPreferences.getString("theme_name", "purple")
        }
        val modernHeaderStyle = overrideModernHeader ?: sharedPreferences.getBoolean("modern_header_style", true)
        if (theme != currentTheme) {
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
        window.navigationBarColor = getThemeColor(R.attr.colorPrimaryDark)
        if (modernHeaderStyle && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getThemeColor(R.attr.headerBackgroundColor)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            window.statusBarColor = getThemeColor(R.attr.colorPrimaryDark)
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
