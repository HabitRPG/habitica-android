package com.habitrpg.android.habitica.ui.activities

import android.os.Bundle
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import kotlinx.android.synthetic.main.activity_notifications.*

class NotificationsActivity : BaseActivity() {

    override fun getLayoutResId(): Int = R.layout.activity_notifications

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar(toolbar)
    }

    override fun injectActivity(component: AppComponent?) {
        component?.inject(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.backStackEntryCount > 0) {
            onBackPressed()
            return true
        }
        return super.onSupportNavigateUp()
    }
}
