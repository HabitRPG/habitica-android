package com.habitrpg.android.habitica.ui.views.shops

import android.app.AlertDialog
import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.events.GearPurchasedEvent
import com.habitrpg.android.habitica.events.ShowSnackbarEvent
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.CurrencyView
import com.habitrpg.android.habitica.ui.views.CurrencyViews
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.insufficientCurrency.InsufficientGemsDialog
import com.habitrpg.android.habitica.ui.views.insufficientCurrency.InsufficientGoldDialog
import com.habitrpg.android.habitica.ui.views.insufficientCurrency.InsufficientHourglassesDialog
import com.habitrpg.android.habitica.ui.views.insufficientCurrency.InsufficientSubscriberGemsDialog
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import org.greenrobot.eventbus.EventBus
import java.util.*
import javax.inject.Inject

class PurchaseDialog(context: Context, component: AppComponent?, val item: ShopItem) : AlertDialog(context) {

    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @Inject
    lateinit var configManager: AppConfigManager

    private val customView: View by lazy {
        LayoutInflater.from(context).inflate(R.layout.dialog_purchase_shopitem, null)
    }
    private val currencyView: CurrencyViews by bindView(customView, R.id.currencyView)
    private val limitedTextView: TextView by bindView(customView, R.id.limitedTextView)
    private val priceLabel: CurrencyView by bindView(customView, R.id.priceLabel)
    private val buyButton: View by bindView(customView, R.id.buyButton)
    private val closeButton: View by bindView(customView, R.id.closeButton)
    private val pinButton: ImageButton by bindView(customView, R.id.pinButton)
    private val contentContainer: ViewGroup by bindView(customView, R.id.content_container)
    private val scrollView: ScrollView by bindView(customView, R.id.scrollView)

    private var shopItem: ShopItem = item
        set(value) {
            field = value

            buyButton.visibility = View.VISIBLE

            if (shopItem.unlockCondition == null) {
                priceLabel.value = shopItem.value.toDouble()
                priceLabel.currency = shopItem.currency
            } else {
                setBuyButtonEnabled(false)
            }

            if (shopItem.isLimited) {
                //TODO: replace with correct date once API is final
                limitedTextView.text = context.getString(R.string.available_until, Date().toString())
            } else {
                limitedTextView.visibility = View.GONE
            }

            priceLabel.isLocked = shopItem.locked

            val contentView: PurchaseDialogContent
            when {
                shopItem.isTypeItem -> contentView = PurchaseDialogItemContent(context)
                shopItem.isTypeQuest -> {
                    contentView = PurchaseDialogQuestContent(context)
                    inventoryRepository.getQuestContent(shopItem.key).firstElement().subscribe(Consumer<QuestContent> { contentView.setQuestContent(it) }, RxErrorHandler.handleEmptyError())
                }
                shopItem.isTypeGear -> {
                    contentView = PurchaseDialogGearContent(context)
                    inventoryRepository.getEquipment(shopItem.key).firstElement().subscribe(Consumer<Equipment> { contentView.setEquipment(it) }, RxErrorHandler.handleEmptyError())
                    checkGearClass()
                }
                "gems" == shopItem.purchaseType -> contentView = PurchaseDialogGemsContent(context)
                else -> contentView = PurchaseDialogBaseContent(context)
            }
            contentView.setItem(shopItem)
            contentContainer.addView(contentView)

            setScrollviewSize()
        }

    private fun checkGearClass() {
        val user = user ?: return

        if (shopItem.purchaseType == "gems") {
            return
        }

        if (shopItem.habitClass != null && shopItem.habitClass != "special" && user.stats?.habitClass != shopItem.habitClass) {
            limitedTextView.text = context.getString(R.string.class_equipment_shop_dialog)
            limitedTextView.visibility = View.VISIBLE
            limitedTextView.setBackgroundColor(ContextCompat.getColor(context, R.color.gray_100))
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
                pinButton.setImageBitmap(HabiticaIconsHelper.imageOfUnpinItem())
            } else {
                pinButton.setImageBitmap(HabiticaIconsHelper.imageOfPinItem())
            }
        }
    init {
        component?.inject(this)

        setView(customView)

        shopItem = item

        compositeSubscription.add(userRepository.getUser().subscribe(Consumer<User> { this.setUser(it) }, RxErrorHandler.handleEmptyError()))

        if (!this.configManager.newShopsEnabled()) {
            pinButton.visibility = View.GONE
        }

        closeButton.setOnClickListener { dismiss() }
        buyButton.setOnClickListener { onBuyButtonClicked() }
        pinButton.setOnClickListener { inventoryRepository.togglePinnedItem(shopItem).subscribe(Consumer { isPinned = !this.isPinned }, RxErrorHandler.handleEmptyError()) }
    }

    private fun setUser(user: User) {
        this.user = user
        currencyView.gold = user.stats?.gp ?: 0.0
        currencyView.gems = user.gemCount.toDouble()
        currencyView.hourglasses = user.hourglassCount?.toDouble() ?: 0.0

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
        }

        if (!shopItem.canAfford(user)) {
            priceLabel.cantAfford = true
        }

        if (shopItem.isTypeGear) {
            checkGearClass()
        }
    }

    override fun dismiss() {
        userRepository.close()
        inventoryRepository.close()
        if (!compositeSubscription.isDisposed) {
            compositeSubscription.dispose()
        }
        super.dismiss()
    }

    private fun setScrollviewSize() {
        scrollView.post {
            if (window != null) {
                val height = scrollView.getChildAt(0).height
                val displayMetrics = DisplayMetrics()
                window?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
                val screenHeight = displayMetrics.heightPixels
                val spaceRequired = (displayMetrics.density * 160).toInt()

                if (height > screenHeight - spaceRequired) {
                    val myScrollViewParams = scrollView.layoutParams
                    myScrollViewParams.height = screenHeight - spaceRequired
                    scrollView.layoutParams = myScrollViewParams

                }
            }
        }
    }

    private fun onBuyButtonClicked() {
        val snackbarText = arrayOf("")
        if (shopItem.isValid) {
            if (shopItem.locked) {
                return
            }
            val gemsLeft = if (shopItem.limitedNumberLeft != null) shopItem.limitedNumberLeft else 0
            if ((gemsLeft == 0 && shopItem.purchaseType == "gems") || shopItem.canAfford(user)) {
                val observable: Flowable<Any>
                if (shopIdentifier != null && shopIdentifier == Shop.TIME_TRAVELERS_SHOP || "mystery_set" == shopItem.purchaseType) {
                    observable = if (shopItem.purchaseType == "gear") {
                        inventoryRepository.purchaseMysterySet(shopItem.key)
                    } else {
                        inventoryRepository.purchaseHourglassItem(shopItem.purchaseType, shopItem.key)
                    }
                } else if (shopItem.purchaseType == "quests" && shopItem.currency == "gold") {
                    observable = inventoryRepository.purchaseQuest(shopItem.key)
                } else if ("gold" == shopItem.currency && "gem" != shopItem.key) {
                    observable = inventoryRepository.buyItem(user, shopItem.key, shopItem.value.toDouble()).map { buyResponse ->
                        if (shopItem.key == "armoire") {
                            snackbarText[0] = when {
                                buyResponse.armoire["type"] == "gear" -> context.getString(R.string.armoireEquipment, buyResponse.armoire["dropText"])
                                buyResponse.armoire["type"] == "food" -> context.getString(R.string.armoireFood, buyResponse.armoire["dropArticle"], buyResponse.armoire["dropText"])
                                else -> context.getString(R.string.armoireExp)
                            }
                        }
                        buyResponse
                    }
                } else {
                    observable = inventoryRepository.purchaseItem(shopItem.purchaseType, shopItem.key)
                }
                observable
                        .doOnNext {
                            val event = ShowSnackbarEvent()
                            if (snackbarText[0].isNotEmpty()) {
                                event.title = context.getString(R.string.successful_purchase, shopItem.text)
                                event.text = snackbarText[0]
                            } else {
                                event.text = context.getString(R.string.successful_purchase, shopItem.text)
                            }
                            event.type = HabiticaSnackbar.SnackbarDisplayType.NORMAL
                            event.rightIcon = priceLabel.compoundDrawables[0]
                            event.rightTextColor = priceLabel.currentTextColor
                            event.rightText = "-" + priceLabel.text
                            EventBus.getDefault().post(event)
                        }
                        .flatMap { userRepository.retrieveUser(false, true) }
                        .flatMap { inventoryRepository.retrieveInAppRewards() }
                        .subscribe({
                            if (item.isTypeGear) {
                                EventBus.getDefault().post(GearPurchasedEvent(item))
                            }
                        }) { throwable ->
                            if (throwable.javaClass.isAssignableFrom(retrofit2.HttpException::class.java)) {
                                val error = throwable as retrofit2.HttpException
                                if (error.code() == 401 && shopItem.currency == "gems") {
                                    MainNavigationController.navigate(R.id.gemPurchaseActivity)
                                }
                            }
                        }
            } else {
                when {
                    "gems" == shopItem.purchaseType -> InsufficientSubscriberGemsDialog(context)
                    "gold" == shopItem.currency -> InsufficientGoldDialog(context)
                    "gems" == shopItem.currency -> InsufficientGemsDialog(context)
                    "hourglasses" == shopItem.currency -> InsufficientHourglassesDialog(context)
                    else -> null
                }?.show()
            }
        }
        dismiss()
    }

    private fun setBuyButtonEnabled(enabled: Boolean) {
        if (enabled) {
            buyButton.alpha = 0.5f
        } else {
            buyButton.alpha = 1.0f
        }
    }
}

