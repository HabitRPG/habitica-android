package com.habitrpg.android.habitica.ui.views.shops

import android.content.Context
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.asDrawable
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.tasks.form.StepperValueFormView

internal class PurchaseDialogGemsContent(context: Context) : PurchaseDialogContent(context) {

    val notesTextView: TextView by bindView(R.id.notesTextView)
    val stepperView: StepperValueFormView by bindView(R.id.stepper_view)

    override val viewId: Int
        get() = R.layout.dialog_purchase_gems

    init {
        stepperView.iconDrawable = HabiticaIconsHelper.imageOfGem().asDrawable(context.resources)
    }

    override fun setItem(item: ShopItem) {
        super.setItem(item)

        notesTextView.text = item.notes
    }
}
