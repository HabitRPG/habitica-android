package com.habitrpg.android.habitica.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import com.habitrpg.android.habitica.HabiticaApplication
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.events.ShowConnectionProblemEvent
import io.reactivex.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

abstract class BaseActivity : AppCompatActivity() {

    private var destroyed: Boolean = false

    protected abstract fun getLayoutResId(): Int

    protected var compositeSubscription = CompositeDisposable()

    private val habiticaApplication: HabiticaApplication
        get() = application as HabiticaApplication

    //Check for "Don't keep Activities" Developer setting
    //TODO: Make this check obsolete.
    internal val isAlwaysFinishActivitiesOptionEnabled: Boolean
        get() {
            var alwaysFinishActivitiesInt = 0
            alwaysFinishActivitiesInt = if (Build.VERSION.SDK_INT >= 17) {
                Settings.System.getInt(applicationContext.contentResolver, Settings.Global.ALWAYS_FINISH_ACTIVITIES, 0)
            } else {
                Settings.System.getInt(applicationContext.contentResolver, Settings.System.ALWAYS_FINISH_ACTIVITIES, 0)
            }

            return alwaysFinishActivitiesInt == 1
        }

    override fun isDestroyed(): Boolean {
        return destroyed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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

        if (compositeSubscription != null && !compositeSubscription!!.isDisposed) {
            compositeSubscription!!.dispose()
        }
        super.onDestroy()
    }

    internal fun showDeveloperOptionsScreen() {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        startActivity(intent)
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
