package com.habitrpg.android.habitica.ui.fragments.purchases

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
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.ProductDetails
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentGemPurchaseBinding
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.extensions.consumeWindowInsetsAbove30
import com.habitrpg.android.habitica.helpers.Analytics
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.helpers.PurchaseTypes
import com.habitrpg.android.habitica.models.promotions.PromoType
import com.habitrpg.android.habitica.ui.GemPurchaseOptionsView
import com.habitrpg.android.habitica.ui.activities.GiftGemsActivity
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.fragments.PromoInfoFragment
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.promo.BirthdayBanner
import com.habitrpg.common.habitica.extensions.isUsingNightModeResources
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.theme.HabiticaTheme
import com.habitrpg.common.habitica.views.HabiticaCircularProgressView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class GemsPurchaseFragment : BaseFragment<FragmentGemPurchaseBinding>() {
    override var binding: FragmentGemPurchaseBinding? = null

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGemPurchaseBinding {
        return FragmentGemPurchaseBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var appConfigManager: AppConfigManager

    @Inject
    lateinit var purchaseHandler: PurchaseHandler

    private var isGemSaleHappening = false

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding?.gems4View?.setOnPurchaseClickListener { purchaseGems(binding?.gems4View) }
        binding?.gems21View?.setOnPurchaseClickListener { purchaseGems(binding?.gems21View) }
        binding?.gems42View?.setOnPurchaseClickListener { purchaseGems(binding?.gems42View) }
        binding?.gems84View?.setOnPurchaseClickListener { purchaseGems(binding?.gems84View) }

        binding?.giftGemsButton?.setOnClickListener { showGiftGemsDialog() }

        if (context?.isUsingNightModeResources() == true) {
            binding?.headerImageView?.setImageResource(R.drawable.gem_purchase_header_dark)
        }

        val promo = appConfigManager.activePromo()
        if (promo != null) {
            binding?.let {
                isGemSaleHappening = true
                promo.configurePurchaseBanner(it)
                if (promo.promoType != PromoType.SUBSCRIPTION) {
                    promo.configureGemView(it.gems4View.binding, 4)
                    promo.configureGemView(it.gems21View.binding, 21)
                    promo.configureGemView(it.gems42View.binding, 42)
                    promo.configureGemView(it.gems84View.binding, 84)
                }
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
                            .padding(bottom = 20.dp)
                    )
                }
            }
            binding?.promoComposeView?.isVisible = true
        }
        loadInventory()

        binding?.supportTextView?.let {
            val paddingBottom = it.paddingBottom
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val bars = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
                )
                v.updatePadding(
                    bottom = bars.bottom + paddingBottom,
                )
                consumeWindowInsetsAbove30(insets)
            }
        }

        Analytics.sendNavigationEvent("gem screen")
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
        CoroutineScope(Dispatchers.IO).launch(ExceptionHandler.coroutine()) {
            val skus = purchaseHandler.getAllGemSKUs()
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

    private fun updateButtonLabel(sku: ProductDetails) {
        val matchingView: GemPurchaseOptionsView? =
            when (sku.productId) {
                PurchaseTypes.PURCHASE_4_GEMS -> binding?.gems4View
                PurchaseTypes.PURCHASE_21_GEMS -> binding?.gems21View
                PurchaseTypes.PURCHASE_42_GEMS -> binding?.gems42View
                PurchaseTypes.PURCHASE_84_GEMS -> binding?.gems84View
                else -> return
            }
        if (matchingView != null) {
            matchingView.setPurchaseButtonText(
                sku.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
            )
            matchingView.sku = sku
        }
    }

    private fun purchaseGems(view: GemPurchaseOptionsView?) {
        val identifier = view?.sku ?: return
        activity?.let { purchaseHandler.purchase(it, identifier, null, null, isGemSaleHappening) }
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
