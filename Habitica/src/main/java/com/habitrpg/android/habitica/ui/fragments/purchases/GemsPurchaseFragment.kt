package com.habitrpg.android.habitica.ui.fragments.purchases

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.ProductDetails
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentGemPurchaseBinding
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.HabiticaProduct
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.models.promotions.HabiticaPromotion
import com.habitrpg.android.habitica.models.promotions.PromoType
import com.habitrpg.android.habitica.ui.GemPurchaseOptionsView
import com.habitrpg.android.habitica.ui.activities.GiftGemsActivity
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.fragments.PromoInfoFragment
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.promo.BirthdayBanner
import com.habitrpg.common.habitica.extensions.isUsingNightModeResources
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.theme.HabiticaTheme
import com.habitrpg.common.habitica.views.HabiticaCircularProgressView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class GemsPurchaseFragment : BaseMainFragment<FragmentGemPurchaseBinding>() {
    override var binding: FragmentGemPurchaseBinding? = null

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentGemPurchaseBinding = FragmentGemPurchaseBinding.inflate(inflater, container, false)

    @Inject
    lateinit var appConfigManager: AppConfigManager

    @Inject
    lateinit var purchaseHandler: PurchaseHandler

    private var gemPromo: HabiticaPromotion? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val promo = appConfigManager.activePromo()
        if (promo?.promoType == PromoType.GEMS_AMOUNT || promo?.promoType == PromoType.GEMS_PRICE) {
            this.gemPromo = promo
        }
        this.hidesToolbar = true
        toolbarBackgroundColor =
            if (gemPromo != null) {
                gemPromo?.screenBackgroundColor(requireContext())
            } else {
                ContextCompat.getColor(
                    requireContext(),
                    R.color.brand_300,
                )
            }
        toolbarIconColor = Color.WHITE
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding?.gems4View?.setOnPurchaseClickListener { purchaseGems(binding?.gems4View) }
        binding?.gems21View?.setOnPurchaseClickListener { purchaseGems(binding?.gems21View) }
        binding?.gems42View?.setOnPurchaseClickListener { purchaseGems(binding?.gems42View) }
        binding?.gems84View?.setOnPurchaseClickListener { purchaseGems(binding?.gems84View) }

        binding?.giftGemsButton?.setOnClickListener { showGiftGemsDialog() }
        binding?.viewSubscriptionsButton?.setOnClickListener {
            MainNavigationController.navigate(
                R.id.gemPurchaseFragment,
                Bundle().apply { putBoolean("openSubscription", true) },
            )
        }

        val promo = gemPromo
        if (promo != null) {
            binding?.let {
                promo.configurePurchaseBanner(it)
                if (promo.promoType != PromoType.SUBSCRIPTION) {
                    promo.configureGemView(it.gems4View.binding, 4)
                    promo.configureGemView(it.gems21View.binding, 21)
                    promo.configureGemView(it.gems42View.binding, 42)
                    promo.configureGemView(it.gems84View.binding, 84)
                }
                binding?.root?.setBackgroundColor(promo.screenBackgroundColor(requireContext()))
                binding?.giftGemsButton?.setBackgroundColor(promo.backgroundColor(requireContext()))
                requireActivity().findViewById<View>(R.id.appbar).setBackgroundColor(promo.screenBackgroundColor(requireContext()))
            }
            binding?.promoBanner?.setOnClickListener {
                val fragment = PromoInfoFragment()
                parentFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment as Fragment)
                    .addToBackStack(null)
                    .commit()
            }
        } else {
            binding?.promoBanner?.visibility = View.GONE
        }

        val birthdayEventEnd = appConfigManager.getBirthdayEvent()?.end
        if (birthdayEventEnd != null) {
            binding?.promoComposeView?.setContent {
                HabiticaTheme {
                    BirthdayBanner(
                        endDate = birthdayEventEnd,
                        Modifier
                            .padding(horizontal = 20.dp)
                            .clip(HabiticaTheme.shapes.medium)
                            .padding(bottom = 20.dp),
                    )
                }
            }
            binding?.promoComposeView?.isVisible = true
        }
        loadInventory()
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launchCatching {
            purchaseHandler.queryPurchases()
        }
        loadInventory()
    }

    private fun loadInventory() {
        if (binding?.gems4View?.sku == null) {
            binding?.loadingIndicator?.setContent {
                HabiticaCircularProgressView()
            }
            binding?.loadingIndicator?.isVisible = true
            binding?.gemPurchaseOptions?.isVisible = false
        }
        viewLifecycleOwner.lifecycleScope.launchCatching {
            val skus = purchaseHandler.loadGemProducts()
            withContext(Dispatchers.Main) {
                if (skus.isEmpty()) {
                    binding?.loadingIndicator?.isVisible = false
                    binding?.gemPurchaseOptions?.isVisible = false
                    val dialog = HabiticaAlertDialog(requireActivity())
                    dialog.setTitle(getString(R.string.error))
                    dialog.setMessage(getString(R.string.error_loading_gems))
                    dialog.addCloseButton()
                    dialog.show()
                    return@withContext
                }
                for (sku in skus) {
                    updateButtonLabel(sku)
                }
                binding?.loadingIndicator?.isVisible = false
                binding?.gemPurchaseOptions?.isVisible = true
            }
        }
    }

    override fun onDestroy() {
        userRepository.close()
        super.onDestroy()
    }

    private fun updateButtonLabel(sku: ProductDetails) {
        val matchingView: GemPurchaseOptionsView? =
            when (HabiticaProduct.forSku(sku.productId)) {
                HabiticaProduct.PURCHASE_4_GEMS -> binding?.gems4View
                HabiticaProduct.PURCHASE_21_GEMS -> binding?.gems21View
                HabiticaProduct.PURCHASE_42_GEMS -> binding?.gems42View
                HabiticaProduct.PURCHASE_84_GEMS -> binding?.gems84View
                else -> return
            }
        if (matchingView != null) {
            matchingView.setPurchaseButtonText(
                sku.oneTimePurchaseOfferDetails?.formattedPrice ?: "",
            )
            matchingView.sku = sku
        }
    }

    private fun purchaseGems(view: GemPurchaseOptionsView?) {
        val identifier = view?.sku ?: return
        lifecycleScope.launchCatching {
            purchaseHandler.purchase(requireActivity(), identifier, null, null, gemPromo != null)
        }
    }

    private fun showGiftGemsDialog() {
        val chooseRecipientDialogView =
            this.activity?.layoutInflater?.inflate(R.layout.dialog_choose_message_recipient, null)

        this.activity?.let { thisActivity ->
            val alert = HabiticaAlertDialog(thisActivity)
            alert.setTitle(getString(R.string.gift_title))
            alert.addButton(getString(R.string.action_continue), true) { _, _ ->
                val usernameEditText =
                    chooseRecipientDialogView?.findViewById<View>(R.id.uuidEditText) as? EditText
                val intent =
                    Intent(thisActivity, GiftGemsActivity::class.java).apply {
                        putExtra("username", usernameEditText?.text.toString())
                    }
                startActivity(intent)
            }
            alert.addCancelButton { _, _ ->
                thisActivity.dismissKeyboard()
            }
            alert.setAdditionalContentView(chooseRecipientDialogView)
            alert.show()
        }
    }
}
