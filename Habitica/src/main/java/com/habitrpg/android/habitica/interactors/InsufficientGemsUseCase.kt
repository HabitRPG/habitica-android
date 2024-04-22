package com.habitrpg.android.habitica.interactors

import android.app.Activity
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.helpers.PurchaseTypes
import com.habitrpg.android.habitica.ui.activities.MainActivity
import javax.inject.Inject

class InsufficientGemsUseCase
    @Inject
    constructor(
        private val purchaseHandler: PurchaseHandler,
    ) : UseCase<InsufficientGemsUseCase.RequestValues, Unit>() {
        override suspend fun run(requestValues: RequestValues) {
            val activity = requestValues.activity as? MainActivity ?: return
            val gemSku =
                if (requestValues.gemPrice > 4) {
                    PurchaseTypes.PURCHASE_21_GEMS
                } else {
                    PurchaseTypes.PURCHASE_4_GEMS
                }
            val sku = purchaseHandler.getInAppPurchaseSKU(gemSku) ?: return
            purchaseHandler.purchase(activity, sku)
        }

        class RequestValues(val gemPrice: Int, val activity: Activity) : UseCase.RequestValues
    }
