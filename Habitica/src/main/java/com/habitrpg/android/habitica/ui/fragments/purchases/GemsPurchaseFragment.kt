package com.habitrpg.android.habitica.ui.fragments.purchases

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentGemPurchaseBinding
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.extensions.isUsingNightModeResources
import com.habitrpg.android.habitica.helpers.*
import com.habitrpg.android.habitica.models.promotions.PromoType
import com.habitrpg.android.habitica.ui.GemPurchaseOptionsView
import com.habitrpg.android.habitica.ui.activities.GemPurchaseActivity
import com.habitrpg.android.habitica.ui.activities.GiftGemsActivity
import com.habitrpg.android.habitica.ui.activities.GiftSubscriptionActivity
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.fragments.PromoInfoFragment
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GemsPurchaseFragment : BaseFragment<FragmentGemPurchaseBinding>(), GemPurchaseActivity.CheckoutFragment {

    override var binding: FragmentGemPurchaseBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentGemPurchaseBinding {
        return FragmentGemPurchaseBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var appConfigManager: AppConfigManager

    private var purchaseHandler: PurchaseHandler? = null

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.gems4View?.setOnPurchaseClickListener { purchaseGems(binding?.gems4View) }
        binding?.gems21View?.setOnPurchaseClickListener { purchaseGems(binding?.gems21View) }
        binding?.gems42View?.setOnPurchaseClickListener { purchaseGems(binding?.gems42View) }
        binding?.gems84View?.setOnPurchaseClickListener { purchaseGems(binding?.gems84View) }

        compositeSubscription.add(
            userRepository.getUser().subscribe(
                {
                    binding?.subscriptionPromo?.visibility = if (it.isSubscribed) View.GONE else View.VISIBLE
                },
                RxErrorHandler.handleEmptyError()
            )
        )

        binding?.giftGemsButton?.setOnClickListener { showGiftGemsDialog() }

        if (context?.isUsingNightModeResources() == true) {
            binding?.headerImageView?.setImageResource(R.drawable.gem_purchase_header_dark)
        }

        val promo = appConfigManager.activePromo()
        if (promo != null) {
            binding?.let {
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
                    .commit()
            }
        } else {
            binding?.promoBanner?.visibility = View.GONE
        }

        AmplitudeManager.sendNavigationEvent("gem screen")
    }

    override fun setupCheckout() {
        CoroutineScope(Dispatchers.IO).launch {
            val skus = purchaseHandler?.getAllGemSKUs()
            withContext(Dispatchers.Main) {
                for (sku in skus ?: emptyList()) {
                    updateButtonLabel(sku.id.code, sku.price)
                }
            }
        }
    }

    override fun setPurchaseHandler(handler: PurchaseHandler?) {
        this.purchaseHandler = handler
    }

    private fun updateButtonLabel(sku: String, price: String) {
        val matchingView: GemPurchaseOptionsView? = when (sku) {
            PurchaseTypes.Purchase4Gems -> binding?.gems4View
            PurchaseTypes.Purchase21Gems -> binding?.gems21View
            PurchaseTypes.Purchase42Gems -> binding?.gems42View
            PurchaseTypes.Purchase84Gems -> binding?.gems84View
            else -> return
        }
        if (matchingView != null) {
            matchingView.setPurchaseButtonText(price)
            matchingView.sku = sku
        }
    }

    private fun purchaseGems(view: GemPurchaseOptionsView?) {
        val identifier = view?.sku ?: return
        purchaseHandler?.purchaseGems(identifier)
    }

    private fun showGiftGemsDialog() {
        val chooseRecipientDialogView = this.activity?.layoutInflater?.inflate(R.layout.dialog_choose_message_recipient, null)

        this.activity?.let { thisActivity ->
            val alert = HabiticaAlertDialog(thisActivity)
            alert.setTitle(getString(R.string.gift_title))
            alert.addButton(getString(R.string.action_continue), true) { _, _ ->
                val usernameEditText = chooseRecipientDialogView?.findViewById<View>(R.id.uuidEditText) as? EditText
                val intent = Intent(thisActivity, GiftGemsActivity::class.java).apply {
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

    private fun showGiftSubscriptionDialog() {
        val chooseRecipientDialogView = this.activity?.layoutInflater?.inflate(R.layout.dialog_choose_message_recipient, null)

        this.activity?.let { thisActivity ->
            val alert = HabiticaAlertDialog(thisActivity)
            alert.setTitle(getString(R.string.gift_title))
            alert.addButton(getString(R.string.action_continue), true) { _, _ ->
                val usernameEditText = chooseRecipientDialogView?.findViewById<View>(R.id.uuidEditText) as? EditText
                val intent = Intent(thisActivity, GiftSubscriptionActivity::class.java).apply {
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
