package com.habitrpg.android.habitica.ui.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
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
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.forceLocale
import com.habitrpg.android.habitica.extensions.updateStatusBarColor
import com.habitrpg.android.habitica.helpers.NotificationsManager
import com.habitrpg.android.habitica.interactors.ShowNotificationInteractor
import com.habitrpg.android.habitica.ui.helpers.ToolbarColorHelper
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.extensions.isUsingNightModeResources
import com.habitrpg.common.habitica.helpers.AnalyticsManager
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.LanguageHelper
import com.habitrpg.common.habitica.helpers.launchCatching
import kotlinx.coroutines.launch
import java.util.Date
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

    protected abstract fun getLayoutResId(): Int?

    open fun getContentView(layoutResId: Int? = getLayoutResId()): View {
        return (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(layoutResId ?: 0, null)
    }

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
        getLayoutResId()?.let {
            setContentView(getContentView(it))
        }
        lifecycleScope.launchCatching {
            notificationsManager.displayNotificationEvents.collect {
                if (ShowNotificationInteractor(this@BaseActivity, lifecycleScope).handleNotification(it)) {
                    lifecycleScope.launch(ExceptionHandler.coroutine()) {
                        userRepository.retrieveUser(false, true)
                    }
                }
            }
        }
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
            if (path != null) {
                val uri = Uri.parse(path)
                sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)
            }
        }
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_using)))
    }

    fun reload() {
        finish()
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
        startActivity(intent)
    }
}
