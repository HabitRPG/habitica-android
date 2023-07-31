package com.habitrpg.wearos.habitica.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.core.view.postDelayed
import com.habitrpg.android.habitica.databinding.ActivityInputBinding
import com.habitrpg.wearos.habitica.ui.viewmodels.InputViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InputActivity : BaseActivity<ActivityInputBinding, InputViewModel>() {
    override val viewModel: InputViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityInputBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        binding.editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                if (binding.editText.text?.isNotEmpty() == true) {
                    returnInput(binding.editText.text.toString())
                }
            }
            false
        }

        showKeyboard()
    }

    private fun returnInput(inputString: String?) {
        val data = Intent()
        data.putExtra("input", inputString)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    private fun showKeyboard() {
        binding.editText.post {
            binding.editText.setText(viewModel.existingInput)
            binding.editText.requestFocus()
            binding.editText.postDelayed(250) {
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.editText, InputMethodManager.SHOW_FORCED)
            }
        }
    }
}
