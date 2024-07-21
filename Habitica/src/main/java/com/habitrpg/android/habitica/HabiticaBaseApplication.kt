package com.habitrpg.android.habitica

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.preference.PreferenceManager
import com.google.android.gms.wearable.Wearable
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.gu.toolargetool.TooLargeTool
import com.habitrpg.android.habitica.data.apiclient.ApiClient
import com.habitrpg.android.habitica.extensions.DateUtils
import com.habitrpg.android.habitica.helpers.AdHandler
import com.habitrpg.android.habitica.helpers.Analytics
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import com.habitrpg.android.habitica.ui.activities.BaseActivity
import com.habitrpg.android.habitica.ui.activities.LoginActivity
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.common.habitica.extensions.setupCoil
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.LanguageHelper
import com.habitrpg.common.habitica.helpers.MarkdownParser
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.HiltAndroidApp
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.coroutines.MainScope
import java.lang.ref.WeakReference
import java.util.Date
import javax.inject.Inject

class ApplicationLifecycleTracker(private val sharedPreferences: SharedPreferences) :
    DefaultLifecycleObserver {
    private var lastResumeTime = 0L

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        lastResumeTime = Date().time
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        val duration = Date().time - lastResumeTime
        addDurationToDay(duration / 1000)
    }

    private fun addDurationToDay(duration: Long) {
        var currentTotal = sharedPreferences.getLong("usage_time_total", 0L)
        currentTotal += duration
        var currentDay = Date()
        if (sharedPreferences.contains("usage_time_day")) {
            currentDay = Date(sharedPreferences.getLong("usage_time_day", 0L))
        }
        var current = sharedPreferences.getLong("usage_time_current", 0L)
        if (!DateUtils.isSameDay(currentDay, Date())) {
            var average = sharedPreferences.getLong("usage_time_daily_average", 0L)
            var observedDays = sharedPreferences.getInt("usage_time_day_count", 0)
            average = ((average * observedDays) + current) / (observedDays + 1)
            sharedPreferences.edit {
                putInt("usage_time_day_count", ++observedDays)
                putLong("usage_time_daily_average", average)
            }
            Analytics.setUserProperty("usage_time_daily_average", average)
            Analytics.setUserProperty("usage_time_total", currentTotal)
            current = 0
            currentDay = Date()
        }
        current += duration
        sharedPreferences.edit {
            putLong("usage_time_current", current)
            putLong("usage_time_total", currentTotal)
            putLong("usage_time_day", currentDay.time)
        }
    }
}

@HiltAndroidApp
abstract class HabiticaBaseApplication : Application(), Application.ActivityLifecycleCallbacks {
    @Inject
    internal lateinit var lazyApiHelper: ApiClient

    @Inject
    internal lateinit var sharedPrefs: SharedPreferences

    @Inject
    internal lateinit var pushNotificationManager: PushNotificationManager

    @Inject
    internal lateinit var authenticationHandler: AuthenticationHandler

    private lateinit var lifecycleTracker: ApplicationLifecycleTracker

    // endregion

    override fun onCreate() {
        super.onCreate()

        lifecycleTracker = ApplicationLifecycleTracker(sharedPrefs)
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleTracker)

        if (!BuildConfig.DEBUG) {
            TooLargeTool.startLogging(this)
            try {
                Analytics.initialize(this)
            } catch (ignored: Resources.NotFoundException) {
            }
            Analytics.identify(sharedPrefs)
            Analytics.setUserID(lazyApiHelper.hostConfig.userID)
        }
        registerActivityLifecycleCallbacks(this)
        setupRealm()
        setLocale()
        setupRemoteConfig()
        setupNotifications()
        setupAdHandler()
        HabiticaIconsHelper.init(this)
        MarkdownParser.setup(this)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        setupCoil()

        ExceptionHandler.init {
            Analytics.logException(it)
        }

        Analytics.setUserProperty("app_testing_level", BuildConfig.TESTING_LEVEL)

        checkIfNewVersion()
    }

    private fun setupAdHandler() {
        AdHandler.setup(sharedPrefs)
    }

    private fun setLocale() {
        val resources = resources
        val configuration: Configuration = resources.configuration
        val languageHelper = LanguageHelper(sharedPrefs.getString("language", "en"))
        if (
            configuration.locales.isEmpty || configuration.locales[0] != languageHelper.locale
        ) {
            configuration.setLocale(languageHelper.locale)
            resources.updateConfiguration(configuration, null)
        }
    }

    protected open fun setupRealm() {
        Realm.init(this)
        val builder =
            RealmConfiguration.Builder()
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .allowWritesOnUiThread(true)
                .compactOnLaunch { totalBytes, usedBytes ->
                    // Compact if the file is over 100MB in size and less than 50% 'used'
                    val oneHundredMB = 50 * 1024 * 1024
                    (totalBytes > oneHundredMB) && (usedBytes / totalBytes) < 0.5
                }
        try {
            Realm.setDefaultConfiguration(builder.build())
        } catch (ignored: UnsatisfiedLinkError) {
            // Catch crash in tests
        }
    }

    private fun checkIfNewVersion() {
        var info: PackageInfo? = null
        try {
            info = packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("MyApplication", "couldn't get package info!")
        }

        if (info == null) {
            return
        }

        val lastInstalledVersion = sharedPrefs.getInt("last_installed_version", 0)
        @Suppress("DEPRECATION")
        if (lastInstalledVersion < info.versionCode) {
            @Suppress("DEPRECATION")
            sharedPrefs.edit {
                putInt("last_installed_version", info.versionCode)
            }
        }
    }

    override fun openOrCreateDatabase(
        name: String,
        mode: Int,
        factory: SQLiteDatabase.CursorFactory?,
    ): SQLiteDatabase {
        return super.openOrCreateDatabase(getDatabasePath(name).absolutePath, mode, factory)
    }

    override fun openOrCreateDatabase(
        name: String,
        mode: Int,
        factory: SQLiteDatabase.CursorFactory?,
        errorHandler: DatabaseErrorHandler?,
    ): SQLiteDatabase {
        return super.openOrCreateDatabase(
            getDatabasePath(name).absolutePath,
            mode,
            factory,
            errorHandler,
        )
    }

    // endregion

    // region IAP - Specific

    override fun deleteDatabase(name: String): Boolean {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction { realm1 ->
            realm1.deleteAll()
            realm1.close()
        }
        return true
    }

    private fun setupRemoteConfig() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings =
            FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 1800)
                .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate()
        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {

            override fun onUpdate(configUpdate: ConfigUpdate) {
                remoteConfig.activate()
            }

            override fun onError(error: FirebaseRemoteConfigException) {
            }
        })
    }

    private fun setupNotifications() {
        FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("Token", "getInstanceId failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            if (BuildConfig.DEBUG) {
                Log.d("Token", "Firebase Notification Token: $token")
            }
        }
    }

    var currentActivity: WeakReference<BaseActivity>? = null

    override fun onActivityResumed(activity: Activity) {
        currentActivity = WeakReference(activity as? BaseActivity)
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = WeakReference(activity as? BaseActivity)
    }

    override fun onActivityPaused(activity: Activity) {
        if (currentActivity?.get() == activity) {
            currentActivity = null
        }
    }

    override fun onActivityCreated(
        p0: Activity,
        p1: Bundle?,
    ) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }

    override fun onActivitySaveInstanceState(
        p0: Activity,
        p1: Bundle,
    ) {
    }

    override fun onActivityStopped(p0: Activity) {
    }

    companion object {
        fun getInstance(context: Context): HabiticaBaseApplication? {
            return context.applicationContext as? HabiticaBaseApplication
        }

        fun deleteDatabase(context: Context) {
            val realm = Realm.getDefaultInstance()
            getInstance(context)?.deleteDatabase(realm.path)
            realm.close()
        }

        fun logout(context: Context) {
            MainScope().launchCatching {
                getInstance(context)?.pushNotificationManager?.removePushDeviceUsingStoredToken()
                deleteDatabase(context)
                val preferences = PreferenceManager.getDefaultSharedPreferences(context)
                val useReminder = preferences.getBoolean("use_reminder", false)
                val reminderTime = preferences.getString("reminder_time", "19:00")
                val lightMode = preferences.getString("theme_mode", "system")
                val launchScreen = preferences.getString("launch_screen", "")
                preferences.edit {
                    clear()
                    putBoolean("use_reminder", useReminder)
                    putString("reminder_time", reminderTime)
                    putString("theme_mode", lightMode)
                    putString("launch_screen", launchScreen)
                }
                getInstance(context)?.lazyApiHelper?.updateAuthenticationCredentials(null, null)
                Wearable.getCapabilityClient(context).removeLocalCapability("provide_auth")
                startActivity(LoginActivity::class.java, context)
            }
        }

        private fun startActivity(
            activityClass: Class<*>,
            context: Context,
        ) {
            val intent = Intent(context, activityClass)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
