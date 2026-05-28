package com.habitrpg.android.habitica.extensions

import com.android.billingclient.api.ProductDetails

val ProductDetails.formattedSubscriptionPrice: String?
    get() = subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice