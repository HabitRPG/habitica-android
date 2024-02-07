package com.habitrpg.android.habitica.ui.activities

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.ActivityGiftGemsBinding
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.ui.fragments.purchases.GiftBalanceGemsFragment
import com.habitrpg.android.habitica.ui.fragments.purchases.GiftPurchaseGemsFragment
import com.habitrpg.android.habitica.ui.views.CurrencyView
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.lang.reflect.InvocationTargetException
import javax.inject.Inject

@AndroidEntryPoint
class GiftGemsActivity : PurchaseActivity() {

    private lateinit var binding: ActivityGiftGemsBinding

    internal val currencyView: CurrencyView by lazy {
        val view = CurrencyView(this, "gems", true)
        view
    }

    @Inject
    lateinit var socialRepository: SocialRepository

    @Inject
    lateinit var appConfigManager: AppConfigManager

    @Inject
    lateinit var purchaseHandler: PurchaseHandler

    private var giftedUsername: String? = null
    private var giftedUserID: String? = null
    private var giftedMember: Member? = null

    private var purchaseFragment: GiftPurchaseGemsFragment? = null
    private var balanceFragment: GiftBalanceGemsFragment? = null

    override fun getLayoutResId(): Int {
        return R.layout.activity_gift_gems
    }

    override fun getContentView(layoutResId: Int?): View {
        binding = ActivityGiftGemsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTitle(R.string.gift_gems)
        setSupportActionBar(binding.toolbar)
        binding.toolbarAccessoryContainer.addView(currencyView)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        giftedUserID = intent.getStringExtra("userID")
        giftedUsername = intent.getStringExtra("username")
        if (giftedUserID.isNullOrBlank()) {
            try {
                giftedUserID = navArgs<GiftSubscriptionActivityArgs>().value.userID
            } catch (_: InvocationTargetException) {
                // user ID wasn't passed as nav arg
            }
        }
        if (giftedUsername.isNullOrBlank()) {
            try {
                giftedUsername = navArgs<GiftSubscriptionActivityArgs>().value.username
            } catch (_: InvocationTargetException) {
                // username wasn't passed as nav arg
            }
        }

        if (giftedUsername.isNullOrBlank() && giftedUserID.isNullOrBlank()) {
            showMemberLoadingErrorDialog()
        }

        setViewPagerAdapter()

        lifecycleScope.launch(ExceptionHandler.coroutine {
            showMemberLoadingErrorDialog()
        }) {
            val member = socialRepository.retrieveMember(giftedUsername ?: giftedUserID)
            if (member == null) {
                showMemberLoadingErrorDialog()
                return@launch
            }
            giftedMember = member
            giftedUserID = member.id
            giftedUsername = member.username
            purchaseFragment?.giftedMember = member
            balanceFragment?.giftedMember = member

            val user = userRepository.getUser().firstOrNull()
            currencyView.value = user?.gemCount?.toDouble() ?: 0.0
        }
    }

    private fun showMemberLoadingErrorDialog() {
        val dialog = HabiticaAlertDialog(this)
        dialog.setTitle(R.string.error_loading_member)
        dialog.setMessage(R.string.error_loading_member_body)
        dialog.addCloseButton(isPrimary = true) { _, _ -> finish() }
        dialog.show()
    }

    private fun setViewPagerAdapter() {
        val statePagerAdapter = object : FragmentStateAdapter(supportFragmentManager, lifecycle) {
            override fun createFragment(position: Int): Fragment {
                return if (position == 0) {
                    val fragment = GiftPurchaseGemsFragment()
                    fragment.setPurchaseHandler(purchaseHandler)
                    fragment.setupCheckout()
                    purchaseFragment = fragment
                    purchaseFragment?.giftedMember = giftedMember
                    fragment
                } else {
                    val fragment = GiftBalanceGemsFragment()
                    balanceFragment = fragment
                    balanceFragment?.giftedMember = giftedMember
                    fragment
                }
            }

            override fun getItemCount(): Int {
                return 2
            }
        }
        binding.viewPager.adapter = statePagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.purchase)
                1 -> getString(R.string.from_balance)
                else -> ""
            }
        }.attach()
        statePagerAdapter.notifyDataSetChanged()
    }
}
