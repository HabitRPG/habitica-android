package com.habitrpg.android.habitica.ui.views.tasks.form

import android.content.Context
import android.graphics.drawable.Drawable
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

class StepperValueFormView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val editText: EditText by bindView(R.id.edit_text)
    private val upButton: ImageButton by bindView(R.id.up_button)
    private val downButton: ImageButton by bindView(R.id.down_button)

    var onValueChanged: ((Double) -> Unit)? = null

    private val decimalFormat = DecimalFormat("0.###")
    private var editTextIsFocused = false

    var value = 0.0
    set(new) {
        var newValue = if (new >= minValue) new else minValue
        maxValue?.let {
            if (newValue > it && it > 0) {
                newValue = it
            }
        }
        val oldValue = field
        field = newValue
        if (oldValue != new) {
            valueString = decimalFormat.format(newValue)
        }
        downButton.isEnabled = field > minValue
        maxValue?.let {
            if (it == 0.0) return@let
            upButton.isEnabled = value < it
        }

        onValueChanged?.invoke(value)
    }

    var maxValue: Double? = null
    var minValue: Double = 0.0

    private var valueString = ""
    set(value) {
        val hasChanged = field != value || editText.text.toString() != field
        field = value
        if (value.isEmpty()) {
            onValueChanged?.invoke(0.0)
            return
        }

        if (hasChanged) {
            editText.setText(field)
            if (editTextIsFocused) {
                editText.setSelection(field.length)
            }
        }
        val newValue = field.toDoubleOrNull() ?: 0.0
        if (this.value != newValue || hasChanged) {
            this.value = newValue
        }
    }

    var iconDrawable: Drawable?
    get() {
        return editText.compoundDrawables.firstOrNull()
    }
    set(value) {
        editText.setCompoundDrawablesWithIntrinsicBounds(value, null, null, null)
    }

    init {
        inflate(R.layout.form_stepper_value, true)

        val attributes = context.theme?.obtainStyledAttributes(
                attrs,
                R.styleable.StepperValueFormView,
                0, 0)

        //set value here, so that the setter is called and everything is set up correctly
        maxValue = attributes?.getFloat(R.styleable.StepperValueFormView_maxValue, 0f)?.toDouble()
        minValue = attributes?.getFloat(R.styleable.StepperValueFormView_minValue, 0f)?.toDouble() ?: 0.0
        value = attributes?.getFloat(R.styleable.StepperValueFormView_defaultValue, 10.0f)?.toDouble() ?: 10.0
        iconDrawable = attributes?.getDrawable(R.styleable.StepperValueFormView_iconDrawable) ?: HabiticaIconsHelper.imageOfGold().asDrawable(context.resources)


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