package com.habitrpg.android.habitica.ui.activities

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.fragments.purchases.GemsPurchaseFragment
import com.habitrpg.android.habitica.ui.fragments.purchases.SubscriptionFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GemPurchaseActivity : PurchaseActivity() {
    override fun getLayoutResId(): Int {
        return R.layout.activity_gem_purchase
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = ""

        if (intent.extras?.containsKey("openSubscription") == true) {
            if (intent.extras?.getBoolean("openSubscription") == false) {
                createFragment(false)
            } else {
                createFragment(true)
            }
        } else {
            createFragment(true)
        }
    }

    private fun createFragment(showSubscription: Boolean) {
        val fragment = if (showSubscription) SubscriptionFragment()
             else GemsPurchaseFragment()

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment as Fragment)
            .commit()
    }
}
