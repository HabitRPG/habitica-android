package com.habitrpg.android.habitica.ui.activities

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.ui.fragments.purchases.GemsPurchaseFragment
import com.habitrpg.android.habitica.ui.fragments.purchases.SubscriptionFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GemPurchaseActivity : PurchaseActivity() {

    @Inject
    lateinit var appConfigManager: AppConfigManager

    override fun getConfigManager(): AppConfigManager {
        return appConfigManager
    }

    private var showSubscription: Boolean = false

    override fun getLayoutResId(): Int = R.layout.activity_gem_purchase

    override fun onCreate(savedInstanceState: Bundle?) {
        showSubscription =
            !(intent.extras?.containsKey("openSubscription") == true && intent.extras?.getBoolean("openSubscription") == false)
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = ""

        if (showSubscription) {
            createFragment(true)
            setTitle(R.string.subscription)
        } else {
            createFragment(false)
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
