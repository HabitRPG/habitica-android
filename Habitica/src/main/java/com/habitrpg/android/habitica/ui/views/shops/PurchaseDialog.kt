package com.habitrpg.android.habitica.ui.views.shops

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.events.GearPurchasedEvent
import com.habitrpg.android.habitica.events.ShowSnackbarEvent
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.extensions.getShortRemainingString
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.HapticFeedbackManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.views.CurrencyView
import com.habitrpg.android.habitica.ui.views.CurrencyViews
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.insufficientCurrency.InsufficientGemsDialog
import com.habitrpg.android.habitica.ui.views.insufficientCurrency.InsufficientGoldDialog
import com.habitrpg.android.habitica.ui.views.insufficientCurrency.InsufficientHourglassesDialog
import com.habitrpg.android.habitica.ui.views.insufficientCurrency.InsufficientSubscriberGemsDialog
import com.habitrpg.android.habitica.ui.views.tasks.form.StepperValueFormView
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import java.util.*
import javax.inject.Inject
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.minutes
import kotlin.time.seconds

class PurchaseDialog(context: Context, component: UserComponent?, val item: ShopItem) : HabiticaAlertDialog(context) {

    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @Inject
    lateinit var configManager: AppConfigManager

    private val customHeader: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.dialog_purchase_shopitem_header, null)
    }
    private val currencyView: CurrencyViews
    private val limitedTextView: TextView
    private val buyButton: View
    private val priceLabel: CurrencyView
    private val buyLabel: TextView
    private var amountErrorLabel: TextView? = null
    private val pinButton: LinearLayout
    private val pinIcon: ImageView
    private val pinTextView: TextView

    private var purchaseQuantity = 1

    var purchaseCardAction: ((ShopItem) -> Unit)? = null

    private var shopItem: ShopItem = item
        set(value) {
            field = value

            if (shopItem.lockedReason(context) == null) {
                updatePurchaseTotal()
                priceLabel.currency = shopItem.currency
            } else {
                limitedTextView.text = shopItem.lockedReason(context)
            }

            priceLabel.isLocked = shopItem.locked || shopItem.lockedReason(context) != null

            val contentView: PurchaseDialogContent
            when {
                shopItem.isTypeItem -> contentView = PurchaseDialogItemContent(context)
                shopItem.isTypeQuest -> {
                    contentView = PurchaseDialogQuestContent(context)
                    inventoryRepository.getQuestContent(shopItem.key).firstElement().subscribe({ contentView.setQuestContent(it) }, RxErrorHandler.handleEmptyError())
                }
                shopItem.isTypeGear -> {
                    contentView = PurchaseDialogGearContent(context)
                    inventoryRepository.getEquipment(shopItem.key).firstElement().subscribe({ contentView.setEquipment(it) }, RxErrorHandler.handleEmptyError())
                    checkGearClass()
                }
                "gems" == shopItem.purchaseType -> contentView = PurchaseDialogGemsContent(context)
                else -> contentView = PurchaseDialogBaseContent(context)
            }

            val stepperView = contentView.findViewById<StepperValueFormView>(R.id.stepper_view)
            if (stepperView != null) {
                if (shopItem.canPurchaseBulk) {
                    stepperView.visibility = View.VISIBLE
                    stepperView.onValueChanged = {
                        purchaseQuantity = it.toInt()
                        updatePurchaseTotal()
                    }
                } else {
                    stepperView.visibility = View.GONE
                }
            }

            val purchaseImmediatelyView = contentView.findViewById<View>(R.id.purchase_immediately_view)
            if (purchaseImmediatelyView != null) {
                if (item.key == "fortify" || item.key == "potion") {
                    purchaseImmediatelyView.visibility = View.VISIBLE
                } else {
                    purchaseImmediatelyView.visibility = View.GONE
                }
            }

            amountErrorLabel = contentView.findViewById(R.id.amount_error_label)

            contentView.setItem(shopItem)
            setAdditionalContentView(contentView)
            setLimitedTextView()
        }

    private fun updatePurchaseTotal() {
        priceLabel.value = shopItem.value.toDouble() * purchaseQuantity

        if ((shopItem.currency != "gold" || shopItem.canAfford(user, purchaseQuantity)) && !shopItem.locked && purchaseQuantity >= 1) {
            buyButton.background = ContextCompat.getDrawable(context, R.drawable.button_background_primary)
            priceLabel.setTextColor(ContextCompat.getColor(context, R.color.white))
            buyLabel.setTextColor(ContextCompat.getColor(context, R.color.white))
        } else {
            buyButton.background = ContextCompat.getDrawable(context, R.drawable.button_background_offset)
            priceLabel.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            buyLabel.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
        }

        if (purchaseQuantity < 1 || (shopItem.limitedNumberLeft != null && (shopItem.limitedNumberLeft ?: 0) < purchaseQuantity)) {
            amountErrorLabel?.visibility = View.VISIBLE
        } else {
            amountErrorLabel?.visibility = View.GONE
        }
    }

    private fun checkGearClass() {
        if (shopItem.purchaseType == "gems") {
            return
        }

        setLimitedTextView()
    }

    private var limitedTextViewJob: Job? = null

    @OptIn(ExperimentalTime::class)
    private fun setLimitedTextView() {
        if (user == null) return
        if (shopItem.habitClass != null && shopItem.habitClass != "special" && user?.stats?.habitClass != shopItem.habitClass) {
            limitedTextView.text = context.getString(R.string.class_equipment_shop_dialog)
            limitedTextView.visibility = View.VISIBLE
            limitedTextView.setBackgroundColor(ContextCompat.getColor(context, R.color.inverted_background))
        } else if (shopItem.event?.end != null) {
            limitedTextViewJob?.cancel()
            limitedTextViewJob = GlobalScope.launch(Dispatchers.Main) {
                limitedTextView.visibility = View.VISIBLE
                while (shopItem.event?.end?.after(Date()) == true) {
                    limitedTextView.text = context.getString(R.string.available_for, shopItem.event?.end?.getShortRemainingString())
                    val diff = (shopItem.event?.end?.time ?: 0) - Date().time
                    delay(if (diff < (60 * 60 * 1000)) Duration.seconds(1) else Duration.minutes(1))
                }
                if (shopItem.event?.end?.before(Date()) == true) {
                    limitedTextView.text = context.getString(R.string.no_longer_available)
                    limitedTextView.background = ContextCompat.getColor(context, R.color.offset_background).toDrawable()
                    limitedTextView.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                }
            }
        } else if (shopItem.locked) {
            buyLabel.text = context.getString(R.string.locked)
            limitedTextView.visibility = View.GONE
            if (shopItem.isTypeGear && shopItem.key.last().toString().toIntOrNull() != null) {
                val previousKey = "${shopItem.key.dropLast(1)}${(shopItem.key.last().toString().toIntOrNull() ?: 1) - 1}"
                if (user?.items?.gear?.owned?.find { it.key == previousKey}?.owned != true) {
                    limitedTextView.visibility = View.VISIBLE
                    limitedTextView.text = context.getString(R.string.locked_equipment_shop_dialog)
                    limitedTextView.background = ContextCompat.getColor(context, R.color.offset_background).toDrawable()
                    limitedTextView.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                }
            }
        } else {
            limitedTextView.visibility = View.GONE
        }
    }

    private val compositeSubscription: CompositeDisposable = CompositeDisposable()
    var shopIdentifier: String? = null
    private var user: User? = null
    var isPinned: Boolean = false
        set(value) {
            field = value
            if (isPinned) {
                pinIcon.setImageDrawable(BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfUnpinItem()))
                pinIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.text_red)
                pinTextView.setTextColor(ContextCompat.getColor(context, R.color.text_red))
                pinTextView.text = context.getText(R.string.unpin)
            } else {
                pinIcon.setImageDrawable(BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfPinItem()))
                pinIcon.imageTintList = ContextCompat.getColorStateList(context, R.color.text_brand)
                pinTextView.setTextColor(ContextCompat.getColor(context, R.color.text_brand))
                pinTextView.text = context.getText(R.string.pin)
            }
        }

    init {
        component?.inject(this)

        forceScrollableLayout = true

        setCustomHeaderView(customHeader)
        currencyView = customHeader.findViewById(R.id.currencyView)
        limitedTextView = customHeader.findViewById(R.id.limitedTextView)
        pinButton = customHeader.findViewById(R.id.pin_button)
        pinIcon = customHeader.findViewById(R.id.pin_icon)
        pinTextView = customHeader.findViewById(R.id.pin_text)

        addCloseButton()
        buyButton = addButton(layoutInflater.inflate(R.layout.dialog_purchase_shopitem_button, null), autoDismiss = false) { _, _ ->
            onBuyButtonClicked()
        }
        priceLabel = buyButton.findViewById(R.id.priceLabel)
        buyLabel = buyButton.findViewById(R.id.buy_label)
        pinButton.setOnClickListener { inventoryRepository.togglePinnedItem(shopItem).subscribe({ isPinned = !this.isPinned }, RxErrorHandler.handleEmptyError()) }

        shopItem = item

        compositeSubscription.add(userRepository.getUser().subscribe({ this.setUser(it) }, RxErrorHandler.handleEmptyError()))
    }

    private fun setUser(user: User) {
        this.user = user
        currencyView.gold = user.stats?.gp ?: 0.0
        currencyView.gems = user.gemCount.toDouble()
        currencyView.hourglasses = user.hourglassCount.toDouble()

        if ("gems" == shopItem.purchaseType) {
            val maxGems = user.purchased?.plan?.totalNumberOfGems() ?: 0
            val gemsLeft = user.purchased?.plan?.numberOfGemsLeft()
            if (maxGems > 0) {
                limitedTextView.text = context.getString(R.string.gems_left_max, gemsLeft, maxGems)
            } else {
                limitedTextView.text = context.getString(R.string.gems_left_nomax, gemsLeft)
            }
            limitedTextView.visibility = View.VISIBLE
            if (gemsLeft == 0) {
                limitedTextView.setBackgroundColor(ContextCompat.getColor(context, R.color.orange_10))
            } else {
                limitedTextView.setBackgroundColor(ContextCompat.getColor(context, R.color.green_10))
            }
            val gemContent = additionalContentView as? PurchaseDialogGemsContent
            gemContent?.binding?.stepperView?.maxValue = (user.purchased?.plan?.numberOfGemsLeft() ?: 1).toDouble()
        }

        buyButton.elevation = 0f
        updatePurchaseTotal()

        if (shopItem.isTypeGear) {
            checkGearClass()
        }
        setLimitedTextView()
    }

    override fun dismiss() {
        userRepository.close()
        inventoryRepository.close()
        limitedTextViewJob?.cancel()
        if (!compositeSubscription.isDisposed) {
            compositeSubscription.dispose()
        }
        super.dismiss()
    }

    private fun onBuyButtonClicked() {
        if (shopItem.isValid && !shopItem.locked) {
            val gemsLeft = if (shopItem.limitedNumberLeft != null) shopItem.limitedNumberLeft else 0
            if ((gemsLeft == 0 && shopItem.purchaseType == "gems") || shopItem.canAfford(user, purchaseQuantity)) {
                remainingPurchaseQuantity { quantity ->
                    if (quantity >= 0) {
                        if (quantity < purchaseQuantity) {
                            displayPurchaseConfirmationDialog(quantity)
                            dismiss()
                            return@remainingPurchaseQuantity
                        }
                    }
                    buyItem(purchaseQuantity)
                }

            } else {
                when {
                    "gems" == shopItem.purchaseType -> {
                        if (shopItem.canAfford(user, purchaseQuantity)) {
                            InsufficientSubscriberGemsDialog(context)
                        } else {
                            InsufficientGoldDialog(context)
                        }
                    }
                    "gold" == shopItem.currency -> InsufficientGoldDialog(context)
                    "gems" == shopItem.currency -> InsufficientGemsDialog(context, shopItem.value)
                    "hourglasses" == shopItem.currency -> InsufficientHourglassesDialog(context)
                    else -> null
                }?.show()
                return
            }
        }
        dismiss()
    }

    private fun buyItem(quantity: Int) {
        FirebaseAnalytics.getInstance(context).logEvent("item_purchased", bundleOf(
                Pair("shop", shopIdentifier),
                Pair("type", shopItem.purchaseType),
                Pair("key", shopItem.key)
        ))
        HapticFeedbackManager.tap(contentView)
        val snackbarText = arrayOf("")
        val observable: Flowable<Any>
        if (shopIdentifier != null && shopIdentifier == Shop.TIME_TRAVELERS_SHOP || "mystery_set" == shopItem.purchaseType || shopItem.currency == "hourglasses") {
            observable = if (shopItem.purchaseType == "gear") {
                inventoryRepository.purchaseMysterySet(shopItem.key).cast(Any::class.java)
            } else {
                inventoryRepository.purchaseHourglassItem(shopItem.purchaseType, shopItem.key).cast(Any::class.java)
            }
        } else if (shopItem.purchaseType == "fortify") {
            observable = userRepository.reroll().cast(Any::class.java)
        } else if (shopItem.purchaseType == "quests" && shopItem.currency == "gold") {
            observable = inventoryRepository.purchaseQuest(shopItem.key).cast(Any::class.java)
        } else if (shopItem.purchaseType == "debuffPotion") {
            observable = userRepository.useSkill(shopItem.key, null).cast(Any::class.java)
        } else if (shopItem.purchaseType == "card") {
            purchaseCardAction?.invoke(shopItem)
            dismiss()
            return
        } else if ("gold" == shopItem.currency && "gem" != shopItem.key) {
            observable = inventoryRepository.buyItem(user, shopItem.key, shopItem.value.toDouble(), quantity).map { buyResponse ->
                if (shopItem.key == "armoire") {
                    snackbarText[0] = when {
                        buyResponse.armoire["type"] == "gear" -> context.getString(R.string.armoireEquipment, buyResponse.armoire["dropText"])
                        buyResponse.armoire["type"] == "food" -> context.getString(R.string.armoireFood, buyResponse.armoire["dropArticle"] ?: "", buyResponse.armoire["dropText"])
                        else -> context.getString(R.string.armoireExp)
                    }
                }
                buyResponse
            }
        } else {
            observable = inventoryRepository.purchaseItem(shopItem.purchaseType, shopItem.key, quantity).cast(Any::class.java)
        }
        val subscription = observable
                .doOnNext {
                    val event = ShowSnackbarEvent()
                    if (snackbarText[0].isNotEmpty()) {
                        event.text = snackbarText[0]
                    } else {
                        event.text = context.getString(R.string.successful_purchase, shopItem.text)
                    }
                    event.type = HabiticaSnackbar.SnackbarDisplayType.NORMAL
                    event.rightIcon = priceLabel.compoundDrawables[0]
                    when (item.currency) {
                        "gold" -> event.rightTextColor = ContextCompat.getColor(context, R.color.text_yellow)
                        "gems" -> event.rightTextColor = ContextCompat.getColor(context, R.color.text_green)
                        "hourglasses" -> event.rightTextColor = ContextCompat.getColor(context, R.color.text_brand)
                    }
                    event.rightText = "-" + priceLabel.text
                    EventBus.getDefault().post(event)
                }
                .flatMap { userRepository.retrieveUser(withTasks = false, forced = true) }
                .flatMap { inventoryRepository.retrieveInAppRewards() }
                .subscribe({
                    if (item.isTypeGear || item.currency == "hourglasses") {
                        EventBus.getDefault().post(GearPurchasedEvent(item))
                    }
                }) { throwable ->
                    if (throwable.javaClass.isAssignableFrom(retrofit2.HttpException::class.java)) {
                        val error = throwable as retrofit2.HttpException
                        if (error.code() == 401 && shopItem.currency == "gems") {
                            MainNavigationController.navigate(R.id.gemPurchaseActivity, bundleOf(Pair("openSubscription", false)))
                        }
                    }
                }
    }

    private fun displayPurchaseConfirmationDialog(quantity: Int) {
        if (quantity == 0) {
            displayNoRemainingConfirmationDialog()
        } else {
            displaySomeRemainingConfirmationDialog(quantity)
        }
    }

    private fun displaySomeRemainingConfirmationDialog(quantity: Int) {
        val alert = HabiticaAlertDialog(context)
        alert.setTitle(R.string.excess_items)
        alert.setMessage(context.getString(R.string.excessItemsXLeft, quantity, item.text, purchaseQuantity))
        alert.addButton(context.getString(R.string.purchaseX, purchaseQuantity), isPrimary = true, isDestructive = false) { _, _ ->
            buyItem(purchaseQuantity)
        }
        alert.addButton(context.getString(R.string.purchaseX, quantity), isPrimary = false, isDestructive = false) { _, _ ->
            buyItem(quantity)
        }
        alert.setExtraCloseButtonVisibility(View.VISIBLE)
        alert.show()
    }

    private fun displayNoRemainingConfirmationDialog() {
        val alert = HabiticaAlertDialog(context)
        alert.setTitle(R.string.excess_items)
        alert.setMessage(context.getString(R.string.excessItemsNoneLeft, item.text, purchaseQuantity, item.text))
        alert.addButton(context.getString(R.string.purchaseX, purchaseQuantity), true, false) { _, _ ->
            buyItem(purchaseQuantity)
        }
        alert.addCancelButton()
        alert.show()
    }

    private fun remainingPurchaseQuantity(onResult: (Int) -> Unit) {
        var totalCount = 20
        var ownedCount = 0
        var shouldWarn = true
        var calledResult = false
        var maybe: Maybe<out List<OwnedItem>>? = null
        if (item.purchaseType == "eggs") {
            maybe = inventoryRepository.getPets(item.key, "quest", null).firstElement().filter {
                shouldWarn = it.isNotEmpty()
                return@filter shouldWarn
            }.flatMap { inventoryRepository.getOwnedItems("eggs").firstElement() }
        } else if (item.purchaseType == "hatchingPotions") {
            totalCount = if (item.path?.contains("wacky") == true) {
                // Wacky pets can't be raised to mounts, so only need half as many
                9
            } else {
                18
            }
            maybe = inventoryRepository.getPets().firstElement().filter {
                val filteredPets = it.filter {pet ->
                    pet.animal == item.key && (pet.type == "premium" || pet.type == "wacky")
                }
                shouldWarn = filteredPets.isNotEmpty()
                return@filter shouldWarn
            }.flatMap { inventoryRepository.getOwnedItems("hatchingPotions").firstElement() }
        }
        if (maybe != null) {
            val sub = maybe.flatMap {
                for (thisItem in it) {
                    if (thisItem.key == item.key) {
                        ownedCount += thisItem.numberOwned
                    }
                }
                inventoryRepository.getOwnedMounts().firstElement()
            }.flatMap {
                for (mount in it) {
                    if (mount.key?.contains(item.key) == true) {
                        ownedCount += if (mount.owned) 1 else 0
                    }
                }
                inventoryRepository.getOwnedPets().firstElement()
            }.subscribe({
                for (pet in it) {
                    if (pet.key?.contains(item.key) == true) {
                        ownedCount += if (pet.trained > 0) 1 else 0
                    }
                }
                if (calledResult) return@subscribe
                calledResult = true
                if (!shouldWarn) {
                    onResult(-1)
                    return@subscribe
                }
                val remaining = totalCount - ownedCount
                onResult(max(0, remaining))
            }, RxErrorHandler.handleEmptyError(), {
                if (calledResult) return@subscribe
                calledResult = true
                if (!shouldWarn) {
                    onResult(-1)
                    return@subscribe
                }
                val remaining = totalCount - ownedCount
                onResult(max(0, remaining))
            })
        } else {
            onResult(-1)
        }
    }
}

