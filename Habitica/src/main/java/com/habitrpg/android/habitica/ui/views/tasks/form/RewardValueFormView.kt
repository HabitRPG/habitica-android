package com.habitrpg.android.habitica.ui.views.tasks.form

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.OnChangeTextWatcher
import com.habitrpg.android.habitica.extensions.asDrawable
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import java.text.DecimalFormat

class RewardValueFormView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val editText: EditText by bindView(R.id.edit_text)
    private val upButton: ImageButton by bindView(R.id.up_button)
    private val downButton: ImageButton by bindView(R.id.down_button)

    private val decimalFormat = DecimalFormat("0.###")
    private var editTextIsFocused = false

    var value = 0.0
    set(value) {
        val newValue = if (value >= 0) value else 0.0
        val oldValue = field
        field = newValue
        if (oldValue != newValue) {
            valueString = decimalFormat.format(newValue)
        }
        downButton.isEnabled = field > 0
    }

    private var valueString = ""
    set(value) {
        field = value

        if (editText.text.toString() != field) {
            editText.setText(field)
            if (editTextIsFocused) {
                editText.setSelection(field.length)
            }
        }
        val newValue = field.toDoubleOrNull() ?: 0.0
        if (this.value != newValue) {
            this.value = newValue
        }
    }

    init {
        inflate(R.layout.task_form_reward_value, true)
        //set value here, so that the setter is called and everything is set up correctly
        value = 10.0
        editText.setCompoundDrawablesWithIntrinsicBounds(HabiticaIconsHelper.imageOfGold().asDrawable(context.resources), null, null, null)

        upButton.setOnClickListener {
            value += 1
        }
        downButton.setOnClickListener {
            value -= 1
        }

        editText.addTextChangedListener(OnChangeTextWatcher { s, _, _, _ ->
            valueString = s.toString()
        })
        editText.setOnFocusChangeListener { _, hasFocus -> editTextIsFocused = hasFocus }
    }
}