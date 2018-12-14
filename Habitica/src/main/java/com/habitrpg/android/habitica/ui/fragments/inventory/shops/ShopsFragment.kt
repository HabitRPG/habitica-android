package com.habitrpg.android.habitica.ui.fragments.inventory.shops

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.views.CurrencyViews
import kotlinx.android.synthetic.main.fragment_viewpager.*
import javax.inject.Inject

class ShopsFragment : BaseMainFragment() {

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    private val currencyView: CurrencyViews by lazy {
        CurrencyViews(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.usesTabLayout = true
        super.onCreateView(inflater, container, savedInstanceState)
        hideToolbar()
        disableToolbarScrolling()
        return inflater.inflate(R.layout.fragment_viewpager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager.currentItem = 0
        setViewPagerAdapter()
        toolbarAccessoryContainer?.addView(currencyView)
        updateCurrencyView()
    }

    override fun onDestroyView() {
        toolbarAccessoryContainer?.removeView(currencyView)
        showToolbar()
        enableToolbarScrolling()
        inventoryRepository.close()
        super.onDestroyView()
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    private fun setViewPagerAdapter() {
        val fragmentManager = childFragmentManager

        viewPager!!.adapter = object : FragmentPagerAdapter(fragmentManager) {

            override fun getItem(position: Int): androidx.fragment.app.Fragment {

                val fragment = ShopFragment()

                fragment.shopIdentifier = when (position) {
                    0 -> Shop.MARKET
                    1 -> Shop.QUEST_SHOP
                    2 -> Shop.SEASONAL_SHOP
                    3 -> Shop.TIME_TRAVELERS_SHOP
                    else -> ""
                }
                fragment.user = this@ShopsFragment.user

                return fragment
            }

            override fun getCount(): Int = 4

            override fun getPageTitle(position: Int): CharSequence? {
                return when (position) {
                    0 -> return context?.getString(R.string.market)
                    1 -> return context?.getString(R.string.quests)
                    2 -> return context?.getString(R.string.seasonalShop)
                    3 -> return context?.getString(R.string.timeTravelers)
                    else -> ""
                }
            }
        }

        if (viewPager != null) {
            tabLayout?.setupWithViewPager(viewPager)
        }
    }


    override fun customTitle(): String {
        return if (!isAdded) {
            ""
        } else getString(R.string.sidebar_shops)
    }

    override fun updateUserData(user: User?) {
        super.updateUserData(user)
        updateCurrencyView()
    }

    private fun updateCurrencyView() {
        if (user == null) {
            return
        }
        currencyView.gold = user?.stats?.gp ?: 0.0
        currencyView.gems = user?.gemCount?.toDouble() ?: 0.0
        currencyView.hourglasses = user?.hourglassCount?.toDouble() ?: 0.0
    }
}
