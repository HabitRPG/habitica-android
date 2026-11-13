package com.habitrpg.android.habitica.ui.activities

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.setNavigationBarDarkIcons
import com.habitrpg.android.habitica.extensions.updateStatusBarColor
import com.habitrpg.android.habitica.helpers.AppConfigManager

abstract class PurchaseActivity : BaseActivity() {
    abstract fun getConfigManager(): AppConfigManager

    open var showsPromo = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val promo = getConfigManager().activePromo()
        val color =
            if (showsPromo && promo != null) {
                promo.screenBackgroundColor(this)
            } else {
                ContextCompat.getColor(this, R.color.brand_300)
            }
        setupToolbar(Color.WHITE, color)
    }

    override fun onResume() {
        super.onResume()
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightNavigationBars = false

        val promo = getConfigManager().activePromo()
        val color =
            if (showsPromo && promo != null) {
                ContextCompat.getColor(this, R.color.gray_1)
            } else {
                ContextCompat.getColor(this, R.color.brand_300)
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            controller.isAppearanceLightStatusBars = false
            window.setNavigationBarDarkIcons(false)
        } else {
            window.updateStatusBarColor(color, false)
        }
        findViewById<View>(R.id.appbar).setBackgroundColor(color)
    }
}
