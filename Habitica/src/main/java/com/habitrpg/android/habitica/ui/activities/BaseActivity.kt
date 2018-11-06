package com.habitrpg.android.habitica.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.habitrpg.android.habitica.HabiticaApplication
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.events.ShowConnectionProblemEvent
import io.reactivex.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import com.instabug.library.InstabugTrackingDelegate
import android.view.MotionEvent



abstract class BaseActivity : AppCompatActivity() {

    private var destroyed: Boolean = false

    protected abstract fun getLayoutResId(): Int

    protected var compositeSubscription = CompositeDisposable()

    private val habiticaApplication: HabiticaApplication
        get() = application as HabiticaApplication

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        InstabugTrackingDelegate.notifyActivityGotTouchEvent(ev, this)
        return super.dispatchTouchEvent(ev)
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
