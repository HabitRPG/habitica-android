package com.habitrpg.android.habitica.ui.fragments.inventory.shops

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentPagerAdapter
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.views.CurrencyViews
import io.reactivex.functions.Consumer
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
        this.hidesToolbar = true
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_viewpager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager.currentItem = 0
        setViewPagerAdapter()
        toolbarAccessoryContainer?.addView(currencyView)

        compositeSubscription.add(userRepository.getUser().subscribe(Consumer { updateCurrencyView(it) }, RxErrorHandler.handleEmptyError()))
    }

    override fun onDestroyView() {
        toolbarAccessoryContainer?.removeView(currencyView)
        super.onDestroyView()
    }

    override fun onDestroy() {
        inventoryRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun setViewPagerAdapter() {
        val fragmentManager = childFragmentManager

        viewPager?.adapter = object : FragmentPagerAdapter(fragmentManager) {

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
                    0 -> context?.getString(R.string.market)
                    1 -> context?.getString(R.string.quests)
                    2 -> context?.getString(R.string.seasonalShop)
                    3 -> context?.getString(R.string.timeTravelers)
                    else -> ""
                }
            }
        }

        if (viewPager != null) {
            tabLayout?.setupWithViewPager(viewPager)
        }
    }

    private fun updateCurrencyView(user: User) {
        currencyView.gold = user.stats?.gp ?: 0.0
        currencyView.gems = user.gemCount.toDouble()
        currencyView.hourglasses = user.hourglassCount?.toDouble() ?: 0.0
    }
}
