package com.habitrpg.android.habitica.ui.activities

import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.habitrpg.android.habitica.HabiticaApplication
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.events.ShowConnectionProblemEvent
import com.habitrpg.android.habitica.helpers.LanguageHelper
import io.reactivex.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*


abstract class BaseActivity : AppCompatActivity() {

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

        super.onCreate(savedInstanceState)
        habiticaApplication
        injectActivity(HabiticaBaseApplication.component)
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

    protected abstract fun injectActivity(component: AppComponent?)

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
    fun onEvent(event: ShowConnectionProblemEvent) {
        val builder = AlertDialog.Builder(this)
                .setTitle(event.title)
                .setMessage(event.message)
                .setNeutralButton(android.R.string.ok) { _, _ -> }

        if (!event.title.isEmpty()) {
            builder.setIcon(R.drawable.ic_warning_black)
        }

        builder.show()
    }
}
