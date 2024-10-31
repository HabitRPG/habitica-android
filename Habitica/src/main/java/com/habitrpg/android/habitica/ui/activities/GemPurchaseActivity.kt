package com.habitrpg.android.habitica.ui.activities

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.hideKeyboard
import com.habitrpg.android.habitica.extensions.updateStatusBarColor
import com.habitrpg.android.habitica.ui.fragments.purchases.GemsPurchaseFragment
import com.habitrpg.android.habitica.ui.fragments.purchases.SubscriptionFragment
import com.habitrpg.android.habitica.ui.helpers.ToolbarColorHelper
import com.habitrpg.common.habitica.extensions.getThemeColor
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GemPurchaseActivity : PurchaseActivity() {
    private var showSubscription: Boolean = false

    override fun getLayoutResId(): Int {
        return R.layout.activity_gem_purchase
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showSubscription = !(intent.extras?.containsKey("openSubscription") == true && intent.extras?.getBoolean("openSubscription") == false)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        if (showSubscription) {
            setupToolbar(toolbar, Color.WHITE, ContextCompat.getColor(this, R.color.brand_300))
        } else {
            setupToolbar(toolbar)
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = ""

        if (showSubscription) {
            createFragment(true)
            toolbar.title = getString(R.string.subscription)
        } else {
            createFragment(false)
        }
    }

    override fun onResume() {
        super.onResume()
        if (showSubscription) {
            window.updateStatusBarColor(ContextCompat.getColor(this, R.color.brand_300), false)
        }
    }

    private fun createFragment(showSubscription: Boolean) {
        val fragment =
            if (showSubscription) {
                SubscriptionFragment()
            } else {
                GemsPurchaseFragment()
            }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment as Fragment)
            .commit()
    }
}
