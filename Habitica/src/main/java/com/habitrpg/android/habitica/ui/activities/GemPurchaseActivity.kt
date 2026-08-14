package com.habitrpg.android.habitica.ui.activities

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.setNavigationBarDarkIcons
import com.habitrpg.android.habitica.extensions.updateStatusBarColor
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.ui.fragments.purchases.GemsPurchaseFragment
import com.habitrpg.android.habitica.ui.fragments.purchases.SubscriptionFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GemPurchaseActivity : PurchaseActivity() {
    @Inject
    lateinit var appConfigManager: AppConfigManager

    private var showSubscription: Boolean = false

    override fun getLayoutResId(): Int = R.layout.activity_gem_purchase

    override fun onCreate(savedInstanceState: Bundle?) {
        showSubscription =
            !(intent.extras?.containsKey("openSubscription") == true && intent.extras?.getBoolean("openSubscription") == false)
        super.onCreate(savedInstanceState)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val promo = appConfigManager.activePromo()
        val color =
            if (!showSubscription && promo != null) {
                promo.screenBackgroundColor(this)
            } else {
                ContextCompat.getColor(this, R.color.brand_300)
            }
        setupToolbar(toolbar, Color.WHITE, color)

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
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightNavigationBars = false

        val promo = appConfigManager.activePromo()
        val color =
            if (!showSubscription && promo != null) {
                ContextCompat.getColor(this, R.color.gray_1)
            } else {
                ContextCompat.getColor(this, R.color.brand_300)
            }
        setupToolbar(toolbar, Color.WHITE, color)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            controller.isAppearanceLightStatusBars = false
            window.setNavigationBarDarkIcons(false)
        } else {
            window.updateStatusBarColor(color, false)
        }
        findViewById<View>(R.id.appbar).setBackgroundColor(color)
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
