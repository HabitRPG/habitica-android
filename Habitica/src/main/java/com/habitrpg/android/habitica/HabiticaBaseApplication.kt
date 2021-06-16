package com.habitrpg.android.habitica

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import coil.Coil
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.transition.CrossfadeTransition
import coil.util.DebugLogger
import com.amplitude.api.Amplitude
import com.amplitude.api.Identify
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.habitrpg.android.habitica.api.HostConfig
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager
import com.habitrpg.android.habitica.modules.UserModule
import com.habitrpg.android.habitica.modules.UserRepositoryModule
import com.habitrpg.android.habitica.proxy.AnalyticsManager
import com.habitrpg.android.habitica.ui.activities.IntroActivity
import com.habitrpg.android.habitica.ui.activities.LoginActivity
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import io.realm.Realm
import io.realm.RealmConfiguration
import org.solovyev.android.checkout.Billing
import org.solovyev.android.checkout.Cache
import org.solovyev.android.checkout.Checkout
import org.solovyev.android.checkout.PurchaseVerifier
import javax.inject.Inject

//contains all HabiticaApplicationLogic except dagger componentInitialisation
abstract class HabiticaBaseApplication : Application() {
    @Inject
    internal lateinit var lazyApiHelper: ApiClient
    @Inject
    internal lateinit var sharedPrefs: SharedPreferences
    @Inject
    internal lateinit var analyticsManager: AnalyticsManager
    @Inject
    internal lateinit var pushNotificationManager: PushNotificationManager
    /**
     * For better performance billing class should be used as singleton
     */
    // endregion

    var billing: Billing? = null
        private set
    /**
     * Application wide [Checkout] instance (can be used
     * anywhere in the app).
     * This instance contains all available products in the app.
     */
    var checkout: Checkout? = null
        private set

    @OptIn(ExperimentalCoilApi::class)
    override fun onCreate() {
        super.onCreate()
        setupRealm()
        setupDagger()
        setupRemoteConfig()
        setupNotifications()
        createBillingAndCheckout()
        HabiticaIconsHelper.init(this)
        MarkdownParser.setup(this)

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        if (!BuildConfig.DEBUG) {
            try {
                Amplitude.getInstance().initialize(this, getString(R.string.amplitude_app_id)).enableForegroundTracking(this)
                val identify = Identify()
                        .setOnce("androidStore", BuildConfig.STORE)
                        .set("launch_screen", sharedPrefs.getString("launch_screen", ""))
                Amplitude.getInstance().identify(identify)
            } catch (ignored: Resources.NotFoundException) {
            }

        }
        var builder = ImageLoader.Builder(this)
            .transition(CrossfadeTransition())
            .componentRegistry {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder(this@HabiticaBaseApplication))
                } else {
                    add(GifDecoder())
                }
            }
        if (BuildConfig.DEBUG) {
            builder = builder.logger(DebugLogger())
        }
        Coil.setImageLoader(builder.build())

        RxErrorHandler.init(analyticsManager)

        FirebaseAnalytics.getInstance(this).setUserProperty("app_testing_level", BuildConfig.TESTING_LEVEL)

        checkIfNewVersion()
    }

    protected open fun setupRealm() {
        Realm.init(this)
        val builder = RealmConfiguration.Builder()
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
            //Catch crash in tests
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

    private fun setupDagger() {
        component = initDagger()
        reloadUserComponent()
        component?.inject(this)
    }

    protected abstract fun initDagger(): AppComponent

    override fun openOrCreateDatabase(name: String,
                                      mode: Int, factory: SQLiteDatabase.CursorFactory?): SQLiteDatabase {
        return super.openOrCreateDatabase(getDatabasePath(name).absolutePath, mode, factory)
    }

    override fun openOrCreateDatabase(name: String,
                                      mode: Int, factory: SQLiteDatabase.CursorFactory?, errorHandler: DatabaseErrorHandler?): SQLiteDatabase {
        return super.openOrCreateDatabase(getDatabasePath(name).absolutePath, mode, factory, errorHandler)
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

    private fun createBillingAndCheckout() {
        billing = Billing(this, object : Billing.DefaultConfiguration() {
            override fun getPublicKey(): String {
                return "DONT-NEED-IT"
            }

            override fun getCache(): Cache {
                return Billing.newCache()
            }

            override fun getPurchaseVerifier(): PurchaseVerifier {
                return HabiticaPurchaseVerifier(this@HabiticaBaseApplication, lazyApiHelper)
            }
        })

        billing?.let { checkout = Checkout.forApplication(it) }
    }

    private fun setupRemoteConfig() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3600)
                .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate()
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

    companion object {

        var component: AppComponent? = null
            private set

        var userComponent: UserComponent? = null

        fun getInstance(context: Context): HabiticaBaseApplication? {
            return context.applicationContext as? HabiticaBaseApplication
        }

        fun logout(context: Context) {
            getInstance(context)?.pushNotificationManager?.removePushDeviceUsingStoredToken()
            val realm = Realm.getDefaultInstance()
            getInstance(context)?.deleteDatabase(realm.path)
            realm.close()
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
            reloadUserComponent()
            getInstance(context)?.lazyApiHelper?.updateAuthenticationCredentials(null, null)
            startActivity(LoginActivity::class.java, context)
        }

        fun reloadUserComponent() {
            userComponent = component?.plus(UserModule(), UserRepositoryModule())
        }

        fun checkUserAuthentication(context: Context, hostConfig: HostConfig?): Boolean {
            if (hostConfig?.apiKey == null || hostConfig.apiKey == "" || hostConfig.userID == "") {
                startActivity(IntroActivity::class.java, context)
                return false
            }

            return true
        }

        private fun startActivity(activityClass: Class<*>, context: Context) {
            val intent = Intent(context, activityClass)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
