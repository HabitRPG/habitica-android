package com.habitrpg.android.habitica.ui.fragments.inventory.shops

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentPagerAdapter
import com.google.firebase.analytics.FirebaseAnalytics
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.databinding.FragmentViewpagerBinding
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.views.CurrencyViews
import javax.inject.Inject


open class ShopsFragment : BaseMainFragment<FragmentViewpagerBinding>() {

    protected var lockTab: Int? = null
    @Inject
    lateinit var inventoryRepository: InventoryRepository

    private val currencyView: CurrencyViews by lazy {
        val view = CurrencyViews(context)
        view
    }

    override var binding: FragmentViewpagerBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentViewpagerBinding {
        return FragmentViewpagerBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.usesTabLayout = true
        this.hidesToolbar = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPager?.currentItem = 0
        setViewPagerAdapter()
        toolbarAccessoryContainer?.addView(currencyView)

        if (lockTab == null) {
            arguments?.let {
                val args = ShopsFragmentArgs.fromBundle(it)
                if (args.selectedTab > 0) {
                    binding?.viewPager?.currentItem = args.selectedTab
                }
            }
        } else {
            this.usesTabLayout = false
            tabLayout?.visibility = View.GONE
            binding?.viewPager?.currentItem = lockTab ?: 0
        }

        context?.let { FirebaseAnalytics.getInstance(it).logEvent("open_shop", bundleOf(Pair("shopIndex", lockTab))) }

        compositeSubscription.add(userRepository.getUser().subscribe({ updateCurrencyView(it) }, RxErrorHandler.handleEmptyError()))
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

        binding?.viewPager?.adapter = object : FragmentPagerAdapter(fragmentManager) {

            override fun getItem(position: Int): androidx.fragment.app.Fragment {

                val fragment = ShopFragment()

                fragment.shopIdentifier = when (lockTab ?: position) {
                    0 -> Shop.MARKET
                    1 -> Shop.QUEST_SHOP
                    2 -> Shop.SEASONAL_SHOP
                    3 -> Shop.TIME_TRAVELERS_SHOP
                    else -> ""
                }
                fragment.user = this@ShopsFragment.user

                return fragment
            }

            override fun getCount(): Int = if (lockTab != null) 1 else 4

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

        if (binding?.viewPager != null) {
            tabLayout?.setupWithViewPager(binding?.viewPager)
        }
    }

    private fun updateCurrencyView(user: User) {
        currencyView.gold = user.stats?.gp ?: 0.0
        currencyView.gems = user.gemCount.toDouble()
        currencyView.hourglasses = user.hourglassCount.toDouble()
    }
}
