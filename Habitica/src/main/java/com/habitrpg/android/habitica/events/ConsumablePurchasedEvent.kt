package com.habitrpg.android.habitica.events

import org.solovyev.android.checkout.Purchase

class ConsumablePurchasedEvent(internal val purchase: Purchase, val recipientID: String? = null)
