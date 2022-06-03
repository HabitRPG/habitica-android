package com.habitrpg.android.habitica.ui.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.habitrpg.android.habitica.HabiticaApplication
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.extensions.isUsingNightModeResources
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
import com.habitrpg.android.habitica.extensions.updateStatusBarColor
import com.habitrpg.common.habitica.helpers.LanguageHelper
import com.habitrpg.android.habitica.helpers.NotificationsManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.interactors.ShowNotificationInteractor
import com.habitrpg.android.habitica.proxy.AnalyticsManager
import com.habitrpg.android.habitica.ui.helpers.ToolbarColorHelper
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.util.Date
import java.util.Locale
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {
    @Inject
    lateinit var notificationsManager: NotificationsManager
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    internal lateinit var analyticsManager: AnalyticsManager

    private var currentTheme: String? = null
    private var isNightMode: Boolean = false
    internal var forcedTheme: String? = null
    internal var forcedIsNight: Boolean? = null
    private var destroyed: Boolean = false

    open var overrideModernHeader: Boolean? = null

    internal var toolbar: Toolbar? = null

    protected abstract fun getLayoutResId(): Int

    open fun getContentView(): View {
        return (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(getLayoutResId(), null)
    }

    var compositeSubscription = CompositeDisposable()

    private val habiticaApplication: HabiticaApplication
        get() = application as HabiticaApplication

    var isActivityVisible = false

    override fun isDestroyed(): Boolean {
        return destroyed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val languageHelper = LanguageHelper(sharedPreferences.getString("language", "en"))
        resources.forceLocale(this, languageHelper.locale)
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
        compositeSubscription.add(
            notificationsManager.displayNotificationEvents.subscribe(
                {
                    if (ShowNotificationInteractor(this, lifecycleScope).handleNotification(it)) {
                        compositeSubscription.add(userRepository.retrieveUser(false, true).subscribeWithErrorHandler {})
                    }
                },
                RxErrorHandler.handleEmptyError()
            )
        )
    }

    override fun onRestart() {
        super.onRestart()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val languageHelper = LanguageHelper(sharedPreferences.getString("language", "en"))
        resources.forceLocale(this, languageHelper.locale)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    internal open fun loadTheme(sharedPreferences: SharedPreferences, forced: Boolean = false) {
        val theme = forcedTheme ?: sharedPreferences.getString("theme_name", "purple")
        if (theme != currentTheme || forced) {
            if (forcedIsNight ?: isNightMode) {
                setTheme(
                    when (theme) {
                        "maroon" -> R.style.MainAppTheme_Maroon_Dark
                        "red" -> R.style.MainAppTheme_Red_Dark
                        "orange" -> R.style.MainAppTheme_Orange_Dark
                        "yellow" -> R.style.MainAppTheme_Yellow_Dark
                        "green" -> R.style.MainAppTheme_Green_Dark
                        "teal" -> R.style.MainAppTheme_Teal_Dark
                        "blue" -> R.style.MainAppTheme_Blue_Dark
                        else -> R.style.MainAppTheme_Dark
                    }
                )
            } else {
                setTheme(
                    when (theme) {
                        "maroon" -> R.style.MainAppTheme_Maroon
                        "red" -> R.style.MainAppTheme_Red
                        "orange" -> R.style.MainAppTheme_Orange
                        "yellow" -> R.style.MainAppTheme_Yellow
                        "green" -> R.style.MainAppTheme_Green
                        "teal" -> R.style.MainAppTheme_Teal
                        "blue" -> R.style.MainAppTheme_Blue
                        "taskform" -> R.style.MainAppTheme_TaskForm
                        else -> R.style.MainAppTheme
                    }
                )
            }
        }

        window.navigationBarColor = if (forcedIsNight ?: isNightMode) {
            ContextCompat.getColor(this, R.color.system_bars)
        } else {
            getThemeColor(R.attr.colorPrimaryDark)
        }
        if (!(forcedIsNight ?: isNightMode)) {
            window.updateStatusBarColor(getThemeColor(R.attr.headerBackgroundColor), true)
        }

        if (currentTheme != null && theme != currentTheme) {
            reload()
        } else {
            currentTheme = theme
        }
    }

    protected abstract fun injectActivity(component: UserComponent?)

    protected fun setupToolbar(toolbar: Toolbar?) {
        this.toolbar = toolbar
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
        toolbar?.let { ToolbarColorHelper.colorizeToolbar(it, this) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val ret = super.onCreateOptionsMenu(menu)
        toolbar?.let { ToolbarColorHelper.colorizeToolbar(it, this) }
        return ret
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

    open fun showConnectionProblem(title: String?, message: String) {
        val alert = HabiticaAlertDialog(this)
        alert.setTitle(title)
        alert.setMessage(message)
        alert.addButton(android.R.string.ok, isPrimary = true, isDestructive = false, function = null)
        alert.enqueue()
    }

    open fun hideConnectionProblem() {
    }

    fun shareContent(identifier: String, message: String, image: Bitmap? = null) {
        analyticsManager.logEvent("shared", bundleOf(Pair("identifier", identifier)))
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "*/*"
        sharingIntent.putExtra(Intent.EXTRA_TEXT, message)
        if (image != null) {
            val path = MediaStore.Images.Media.insertImage(this.contentResolver, image, "${(Date())}", null)
            val uri = Uri.parse(path)
            sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)
        }
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_using)))
    }

    fun reload() {
        finish()
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
        startActivity(intent)
    }
}

private fun Resources.forceLocale(activity: BaseActivity, locale: Locale) {
    Locale.setDefault(locale)
    val configuration = Configuration()
    configuration.setLocale(locale)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        activity.createConfigurationContext(configuration)
    }
    updateConfiguration(configuration, displayMetrics)
}
