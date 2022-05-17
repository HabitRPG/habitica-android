package com.habitrpg.android.habitica.ui

import android.content.Context
import android.content.DialogInterface
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.common.habitica.views.PixelArtView

class ItemDetailDialog(context: Context) : AlertDialog(context) {

    private val itemImageView = PixelArtView(context)
    private val contentTextView = TextView(context, null)
    private val priceTextView = TextView(context, null)
    private val currencyImageView = ImageView(context)

    init {

        // External ContentView
        val contentViewLayout = LinearLayout(context)
        contentViewLayout.orientation = LinearLayout.VERTICAL

        // Gear Image
        val gearImageLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )

        gearImageLayoutParams.gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
        gearImageLayoutParams.setMargins(0, 0, 0, 20)
        itemImageView.minimumWidth = 200
        itemImageView.minimumHeight = 200
        itemImageView.layoutParams = gearImageLayoutParams
        itemImageView.visibility = View.GONE

        // Gear Description
        contentTextView.setPadding(16, 0, 16, 0)
        contentTextView.visibility = View.GONE

        // GoldPrice View
        val goldPriceLayout = LinearLayout(context)
        goldPriceLayout.gravity = Gravity.CENTER_HORIZONTAL
        val goldPriceLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        goldPriceLayoutParams.setMargins(0, 0, 0, 16)
        goldPriceLayoutParams.gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL

        goldPriceLayout.orientation = LinearLayout.HORIZONTAL
        goldPriceLayout.layoutParams = goldPriceLayoutParams
        goldPriceLayout.gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL

        // Price View
        priceTextView.setPadding(10, 0, 0, 0)

        currencyImageView.minimumHeight = 50
        currencyImageView.minimumWidth = 50
        currencyImageView.setPadding(0, 0, 5, 0)

        goldPriceLayout.addView(currencyImageView)
        goldPriceLayout.addView(priceTextView)

        contentViewLayout.gravity = Gravity.CENTER_VERTICAL

        contentViewLayout.addView(itemImageView)

        contentViewLayout.addView(goldPriceLayout)

        contentViewLayout.addView(contentTextView)

        setView(contentViewLayout)

        this.setButton(BUTTON_NEGATIVE, context.getText(R.string.reward_dialog_dismiss)) { clickedDialog, _ -> clickedDialog.dismiss() }
    }

    fun setDescription(description: CharSequence) {
        contentTextView.text = description
        contentTextView.visibility = View.VISIBLE
    }

    fun setCurrency(currency: String) {
        when (currency) {
            "gold" -> currencyImageView.setImageBitmap(HabiticaIconsHelper.imageOfGold())
            "gems" -> currencyImageView.setImageBitmap(HabiticaIconsHelper.imageOfGem())
            else -> currencyImageView.setImageDrawable(null)
        }
    }

    fun setValue(value: Double?) {
        priceTextView.text = value?.toString()
    }

    fun setValue(value: Int?) {
        priceTextView.text = value?.toString()
    }

    fun setImage(imageName: String) {
        itemImageView.visibility = View.VISIBLE
        itemImageView.loadImage(imageName)
    }

    fun setBuyListener(listener: DialogInterface.OnClickListener) {
        this.setButton(DialogInterface.BUTTON_POSITIVE, context.getText(R.string.reward_dialog_buy), listener)
    }
}
