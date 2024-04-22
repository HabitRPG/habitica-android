package com.habitrpg.android.habitica.ui.views.promo

import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.models.promotions.HabiticaPromotion

class PromoMenuViewHolder(val promoView: PromoMenuView) : RecyclerView.ViewHolder(promoView) {
    fun bind(promo: HabiticaPromotion) {
        promo.configurePromoMenuView(promoView)
    }
}
