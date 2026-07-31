package com.habitrpg.android.habitica.ui.fragments.purchases

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.ProductDetails
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentSubscriptionBinding
import com.habitrpg.android.habitica.databinding.FragmentSubscriptionContentBinding
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.extensions.consumeWindowInsetsAbove30
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.GiftSubscriptionActivity
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.fragments.PromoInfoFragment
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.promo.BirthdayBanner
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.theme.HabiticaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SubscriptionFragment : BaseFragment<FragmentSubscriptionBinding>(), CommonSubscriptionFragment {
    override var binding: FragmentSubscriptionBinding? = null

    override val content: FragmentSubscriptionContentBinding?
        get() = binding?.content

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentSubscriptionBinding = FragmentSubscriptionBinding.inflate(inflater, container, false)

    @Inject
    override lateinit var userRepository: UserRepository
    @Inject
    lateinit var appConfigManager: AppConfigManager
    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @Inject
    override lateinit var purchaseHandler: PurchaseHandler

    override var selectedSubscriptionSku: ProductDetails? = null
    override var skus: List<ProductDetails> = emptyList()

    override var user: User? = null
    override var hasLoadedSubscriptionOptions: Boolean = false

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupView(requireActivity())
        binding?.content?.giftSegmentSubscribed?.giftSubscriptionButton?.setOnClickListener {
            showGiftSubscriptionDialog(requireContext())
        }
        binding?.content?.giftSegmentUnsubscribed?.giftSubscriptionButton?.setOnClickListener {
            showGiftSubscriptionDialog(requireContext())
        }

        val promo = appConfigManager.activePromo()
        if (promo != null) {
            binding?.let {
                promo.configurePurchaseBanner(it)
            }
            binding?.content?.promoBanner?.setOnClickListener {
                val fragment = PromoInfoFragment()
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment as Fragment)
                    .addToBackStack(null)
                    .commit()
            }
        } else {
            binding?.content?.promoBanner?.visibility = View.GONE
        }

        val birthdayEventEnd = appConfigManager.getBirthdayEvent()?.end
        if (birthdayEventEnd != null) {
            binding?.content?.promoComposeView?.setContent {
                HabiticaTheme {
                    BirthdayBanner(
                        endDate = birthdayEventEnd,
                        Modifier
                            .padding(horizontal = 20.dp)
                            .clip(HabiticaTheme.shapes.medium)
                            .padding(bottom = 10.dp),
                    )
                }
            }
            binding?.content?.promoComposeView?.isVisible = true
        }

        binding?.refreshLayout?.setOnRefreshListener { refresh() }

        binding?.content?.bottomSpacing?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val bars =
                    insets.getInsets(
                        WindowInsetsCompat.Type.systemBars()
                            or WindowInsetsCompat.Type.displayCutout(),
                    )
                v.updateLayoutParams {
                    height = bars.bottom
                }
                consumeWindowInsetsAbove30(insets)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launchCatching {
            purchaseHandler.queryPurchases()
        }
        refresh()
        loadInventory()
    }

    override fun purchaseSubscription() {
        selectedSubscriptionSku?.let { sku ->
            lifecycleScope.launchCatching {
                purchaseHandler.purchase(requireActivity(), sku)
            }
        }
    }

    override fun refresh() {
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.retrieveUser(false, true)
            binding?.refreshLayout?.isRefreshing = false
        }
    }

    companion object {
        fun showGiftSubscriptionDialog(context: Context) {
            val chooseRecipientDialogView =
                context.layoutInflater.inflate(R.layout.dialog_choose_message_recipient, null)

            val alert = HabiticaAlertDialog(context)
            alert.setTitle(context.getString(R.string.gift_title))
            alert.addButton(context.getString(R.string.action_continue), true) { _, _ ->
                val usernameEditText =
                    chooseRecipientDialogView?.findViewById<View>(R.id.uuidEditText) as? EditText
                val intent =
                    Intent(context, GiftSubscriptionActivity::class.java).apply {
                        putExtra("username", usernameEditText?.text.toString())
                    }
                context.startActivity(intent)
            }
            alert.addCancelButton { _, _ ->
            }
            alert.setAdditionalContentView(chooseRecipientDialogView)
            alert.show()
        }
    }
}
