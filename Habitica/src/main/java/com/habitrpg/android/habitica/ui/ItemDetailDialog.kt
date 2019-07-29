package com.habitrpg.android.habitica.ui

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper

class ItemDetailDialog(context: Context) : AlertDialog(context) {

    private val itemImageView: SimpleDraweeView
    private val contentTextView: TextView
    private val priceTextView: TextView
    private val currencyImageView: ImageView

    init {

        // External ContentView
        val contentViewLayout = LinearLayout(context)
        contentViewLayout.orientation = LinearLayout.VERTICAL

        // Gear Image
        itemImageView = SimpleDraweeView(context)
        val gearImageLayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        gearImageLayoutParams.gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL
        gearImageLayoutParams.setMargins(0, 0, 0, 20)
        itemImageView.minimumWidth = 200
        itemImageView.minimumHeight = 200
        itemImageView.layoutParams = gearImageLayoutParams
        itemImageView.visibility = View.GONE

        // Gear Description
        contentTextView = TextView(context, null)
        contentTextView.setPadding(16, 0, 16, 0)
        contentTextView.visibility = View.GONE

        // GoldPrice View
        val goldPriceLayout = LinearLayout(context)
        goldPriceLayout.gravity = Gravity.CENTER_HORIZONTAL
        val goldPriceLayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        goldPriceLayoutParams.setMargins(0, 0, 0, 16)
        goldPriceLayoutParams.gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL

        goldPriceLayout.orientation = LinearLayout.HORIZONTAL
        goldPriceLayout.layoutParams = goldPriceLayoutParams
        goldPriceLayout.gravity = Gravity.CENTER_HORIZONTAL or Gravity.CENTER_VERTICAL

        // Price View
        priceTextView = TextView(context)
        priceTextView.setPadding(10, 0, 0, 0)

        currencyImageView = ImageView(context)
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
        DataBindingUtils.loadImage(itemImageView, imageName)
    }

    fun setBuyListener(listener: DialogInterface.OnClickListener) {
        this.setButton(DialogInterface.BUTTON_POSITIVE, context.getText(R.string.reward_dialog_buy), listener)
    }
}
