package com.habitrpg.android.habitica.ui.views.tasks.form

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.asDrawable
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import java.text.DecimalFormat

class RewardValueFormView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), TextWatcher {

    private val editText: EditText by bindView(R.id.edit_text)
    private val upButton: ImageButton by bindView(R.id.up_button)
    private val downButton: ImageButton by bindView(R.id.down_button)

    private val decimalFormat = DecimalFormat("0.#")

    var value = 0.0
    set(value) {
        field = if (value >= 0) value else 0.0
        val stringValue = decimalFormat.format(field)
        if (editText.text.toString() != stringValue) {
            editText.setText(stringValue)
        }
        downButton.isEnabled = field > 0
    }

    init {
        inflate(R.layout.task_form_reward_value, true)

        editText.setCompoundDrawablesWithIntrinsicBounds(HabiticaIconsHelper.imageOfGold().asDrawable(context.resources), null, null, null)

        upButton.setOnClickListener {
            value += 1
        }
        downButton.setOnClickListener {
            value -= 1
        }

        editText.addTextChangedListener(this)
    }

    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        value = s.toString().toDoubleOrNull() ?: 0.0
    }
}