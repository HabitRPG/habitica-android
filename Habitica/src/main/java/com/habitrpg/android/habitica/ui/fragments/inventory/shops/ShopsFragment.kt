package com.habitrpg.android.habitica.ui.fragments.inventory.shops

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
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
        hideToolbar()
        disableToolbarScrolling()
        super.onCreateView(inflater, container, savedInstanceState)
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

            override fun getItem(position: Int): Fragment {

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

        if (tabLayout != null && viewPager != null) {
            tabLayout!!.setupWithViewPager(viewPager)
        }
    }


    override fun customTitle(): String {
        return if (!isAdded) {
            ""
        } else getString(R.string.sidebar_shops)
    }

    override fun updateUserData(user: User) {
        super.updateUserData(user)
        updateCurrencyView()
    }

    private fun updateCurrencyView() {
        if (user == null) {
            return
        }
        currencyView.setGold(user!!.stats.getGp())
        currencyView.setGems(user!!.gemCount)
        currencyView.setHourglasses(user!!.hourglassCount)
    }
}
