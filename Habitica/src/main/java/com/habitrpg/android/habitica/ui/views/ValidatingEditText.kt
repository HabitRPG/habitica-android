package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ValidatingEditTextBinding
import com.habitrpg.android.habitica.extensions.layoutInflater

class ValidatingEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private var binding: ValidatingEditTextBinding = ValidatingEditTextBinding.inflate(context.layoutInflater, this)

    var text: String?
        get() = binding.editText.text?.toString()
        set(value) = binding.editText.setText(value)
    var errorText: String?
        get() = binding.errorText.text?.toString()
        set(value) {
            binding.errorText.text = value
        }

    var validator: ((String?) -> Boolean)? = null

    val isValid: Boolean
    get() = validator?.invoke(text) == true

    init {
        orientation = VERTICAL
        context.theme?.obtainStyledAttributes(
            attrs,
            R.styleable.ValidatingEditText,
            0, 0
        )?.let { attributes ->
            binding.inputLayout.hint = attributes.getString(R.styleable.ValidatingEditText_hint)
            binding.editText.inputType = attributes.getInt(R.styleable.ValidatingEditText_android_inputType, InputType.TYPE_CLASS_TEXT)
        }


        binding.editText.setOnFocusChangeListener { _, isEditing ->
            if (isEditing) return@setOnFocusChangeListener
            if (validator?.invoke(text) == true) {
                binding.errorText.visibility = View.GONE
            } else {
                binding.errorText.visibility = View.VISIBLE
            }
            (this.parent as? ViewGroup)?.forceLayout()
        }
    }
}