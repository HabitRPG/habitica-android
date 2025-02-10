package com.habitrpg.android.habitica.ui.activities

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.habitrpg.android.habitica.HabiticaApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.consumeWindowInsetsAbove30
import com.habitrpg.android.habitica.extensions.forceLocale
import com.habitrpg.android.habitica.extensions.updateStatusBarColor
import com.habitrpg.android.habitica.helpers.Analytics
import com.habitrpg.android.habitica.helpers.EventCategory
import com.habitrpg.android.habitica.helpers.HitType
import com.habitrpg.android.habitica.helpers.NotificationsManager
import com.habitrpg.android.habitica.interactors.ShowNotificationInteractor
import com.habitrpg.android.habitica.ui.helpers.ToolbarColorHelper
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.extensions.isUsingNightModeResources
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.LanguageHelper
import com.habitrpg.common.habitica.helpers.launchCatching
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Date
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {

    @Inject
    lateinit var notificationsManager: NotificationsManager

    @Inject
    lateinit var userRepository: UserRepository

    private var currentTheme: String? = null
    private var isNightMode: Boolean = false
    internal var forcedTheme: String? = null
    internal var forcedIsNight: Boolean? = null
    private var destroyed: Boolean = false

    open var overrideModernHeader: Boolean? = null

    internal var toolbar: Toolbar? = null
    private var toolbarContentColor: Int? = null
    private var toolbarBackgroundColor: Int? = null

    protected abstract fun getLayoutResId(): Int?

    open fun getContentView(layoutResId: Int? = getLayoutResId()): View {
        return (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            layoutResId ?: 0,
            null
        )
    }

    private val habiticaApplication: HabiticaApplication
        get() = application as HabiticaApplication

    var isActivityVisible = false

    override fun isDestroyed(): Boolean {
        return destroyed
    }

    private val defaultNavigationBarStyle by lazy {
        SystemBarStyle.auto(ContextCompat.getColor(this, R.color.white_50_alpha),
            ContextCompat.getColor(this, R.color.black_50_alpha)
        )
    }
    internal var navigationBarStyle: SystemBarStyle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(navigationBarStyle = navigationBarStyle ?: defaultNavigationBarStyle)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val languageHelper = LanguageHelper(sharedPreferences.getString("language", "en"))
        resources.forceLocale(this, languageHelper.locale)
        delegate.localNightMode =
            when (sharedPreferences.getString("theme_mode", "system")) {
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        isNightMode = isUsingNightModeResources()
        loadTheme(sharedPreferences)

        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        habiticaApplication
        getLayoutResId()?.let {
            setContentView(getContentView(it))
        }
        lifecycleScope.launchCatching {
            notificationsManager.displayNotificationEvents.collect {
                if (ShowNotificationInteractor(
                        this@BaseActivity,
                        lifecycleScope
                    ).handleNotification(it)
                ) {
                    lifecycleScope.launch(ExceptionHandler.coroutine()) {
                        userRepository.retrieveUser(false, true)
                    }
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        findViewById<View>(R.id.appbar)?.let { appbar ->
            val paddingTop = appbar.paddingTop
            ViewCompat.setOnApplyWindowInsetsListener(appbar) { v, windowInsets ->
                val insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                    + WindowInsetsCompat.Type.displayCutout()
                )
                v.updatePadding(top = insets.top + paddingTop,
                left = insets.left,
                    right = insets.right)
                consumeWindowInsetsAbove30(windowInsets)
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

    internal open fun loadTheme(
        sharedPreferences: SharedPreferences,
        forced: Boolean = false
    ) {
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

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.navigationBarColor =
                if (forcedIsNight ?: isNightMode) {
                    ContextCompat.getColor(this, R.color.system_bars)
                } else {
                    getThemeColor(R.attr.colorPrimaryDark)
                }
            if (!(forcedIsNight ?: isNightMode)) {
                window.updateStatusBarColor(getThemeColor(R.attr.headerBackgroundColor), true)
            }
        }

        if (currentTheme != null && theme != currentTheme) {
            reload()
        } else {
            currentTheme = theme
        }
    }

    protected fun setupToolbar(toolbar: Toolbar?, iconColor: Int? = null, backgroundColor: Int? = null) {
        this.toolbar = toolbar
        this.toolbarContentColor = iconColor
        this.toolbarBackgroundColor = backgroundColor
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val ret = super.onCreateOptionsMenu(menu)
        toolbar?.let { ToolbarColorHelper.colorizeToolbar(it, this, this.toolbarContentColor, this.toolbarBackgroundColor) }
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

    open fun showConnectionProblem(
        errorCount: Int,
        title: String?,
        message: String,
        isFromUserInput: Boolean
    ) {
        val alert = HabiticaAlertDialog(this)
        alert.setTitle(title)
        alert.setMessage(message)
        alert.addButton(
            android.R.string.ok,
            isPrimary = true,
            isDestructive = false,
            function = null
        )
        alert.enqueue()
    }

    open fun hideConnectionProblem() {
    }

    fun shareContent(
        identifier: String,
        message: String?,
        image: Bitmap? = null
    ) {
        Analytics.sendEvent(
            "shared",
            EventCategory.BEHAVIOUR,
            HitType.EVENT,
            mapOf("identifier" to identifier)
        )
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "image/*"
        if (message?.isNotBlank() == true) {
            sharingIntent.putExtra(Intent.EXTRA_TEXT, message)
        }
        try {
            if (image != null) {
                val fos: OutputStream
                val uri: Uri
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = contentResolver
                    val contentValues = ContentValues()
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "${Date()}.png")
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    contentValues.put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES
                    )
                    uri =
                        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                            ?: return
                    fos = resolver.openOutputStream(uri, "wt") ?: return
                } else {
                    val imagesDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                            .toString()
                    val file = File(imagesDir, "${Date()}.png")
                    uri = file.absoluteFile.toUri()
                    fos = FileOutputStream(file)
                }
                image.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.close()
                sharingIntent.putExtra(Intent.EXTRA_STREAM, uri)
            }
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_using)))
        } catch (_: FileNotFoundException) {
        }
    }

    fun reload() {
        finish()
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
        startActivity(intent)
    }

}
